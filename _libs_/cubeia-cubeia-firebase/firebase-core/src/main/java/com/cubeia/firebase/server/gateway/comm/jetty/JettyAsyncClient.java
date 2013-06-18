/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cubeia.firebase.server.gateway.comm.jetty;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.comm.AbstractClient;
import com.cubeia.firebase.server.gateway.util.CompilationCache;
import com.cubeia.firebase.server.node.ClientNodeContext;
import com.cubeia.firebase.util.FirebaseLockFactory;

// TODO Configure timeout on "take"
public class JettyAsyncClient extends AbstractClient implements AsyncSession {

	private final BlockingQueue<ProtocolObject> queue = new LinkedBlockingQueue<ProtocolObject>();
	private final AtomicReference<AsyncSessionListener> listener = new AtomicReference<AsyncSessionListener>();
	private final InetSocketAddress remoteAddress;
	
	// private final AtomicBoolean isClosed = new AtomicBoolean(false);
	
	/*
	 * This lock is used to block outgoing packets when the listener is set
	 * so the no packets can be dropped that way
	 */
	private final ReadWriteLock listenerLock = FirebaseLockFactory.createLock();

	public JettyAsyncClient(ClientNodeContext context, InetSocketAddress address) {
		super(context);
		this.remoteAddress = address;
	}
	
	
	// --- ASYNC SESSION --- //
	
	public void setAsyncSessionListener(AsyncSessionListener listener) {
		listenerLock.writeLock().lock();
		try {
			this.listener.set(listener);
			if(listener != null) {
				/*
				 * Drain the existing queue to make sure we're not missing any
				 * packets delivered before the listener.
				 */
				List<ProtocolObject> list = new LinkedList<ProtocolObject>();
				queue.drainTo(list);
				if(list.size() > 0) {
					listener.onMessage(list);
				}
			}
		} finally {
			listenerLock.writeLock().unlock();
		}
	}
	
	public List<ProtocolObject> pollOutgoing() {
		List<ProtocolObject> list = new LinkedList<ProtocolObject>();
		queue.drainTo(list);
		return list;
	}
	
	public List<ProtocolObject> takeOutgoing() {
		List<ProtocolObject> list = new LinkedList<ProtocolObject>();
		// if(isClosed.get()) return list; // SANITY CHECK
		try {
			ProtocolObject o = queue.poll(1, SECONDS); // TODO Configuration...
			if(o != null) {
				list.add(o);
				// Pick up all we can whilst we're at it...
				queue.drainTo(list);
			}
		} catch (InterruptedException e) { }
		return list;
	};

	@Override
	public void handleIncoming(List<ProtocolObject> list) {
		super.dispatch(list);
	}
	
	
	// --- FIREBASE CLIENT --- //
	
	@Override
	public void close() {
		super.close();
		AsyncSessionListener list = this.listener.get();
		if(list != null) {
			list.close();
		}
	}
	
	@Override
	public void sendClientPackets(CompilationCache packets) {
		sendClientPackets(packets.getObjects());
	}
	
	@Override
	public void sendClientPackets(List<ProtocolObject> packets) {
		if(packets == null || packets.size() == 0) return; // EARLY RETURN
		listenerLock.readLock().lock();
		try {
			AsyncSessionListener l = listener.get();
			if(l != null) {
				if(log.isDebugEnabled()) {
					log.debug("Forwarding packets: " + packets);
				}
				l.onMessage(packets);
			} else {
				if(log.isDebugEnabled()) {
					log.debug("Queueing packets: " + packets);	
				}
				queue.addAll(packets);
			}
		} finally {
			listenerLock.readLock().unlock();
		}	
	}

	@Override
	public void sendClientPacket(ProtocolObject packet) {
		sendClientPackets(singletonList(packet));
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public int getWriteQueueSize() {
		return queue.size();
	}

	@Override
	public int getWriteQueueBytes() {
		return -1;
	}	
}
