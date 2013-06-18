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
package com.cubeia.firebase.server.routing.impl;

import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;

/*
 * UNCOMMENTED: This sender adds halt functionality to an underlying sender. It will use
 * a simple unbounded queue for the halt duration. Also, it will look out for a 
 * jgroups specialty: When a node goes down jgroups may send an illegal argument 
 * exception, which can be caught and the event may be possible to re-send.
 */
class WrappedSender<T extends Event<?>> implements Sender<T>, Haltable {

	/*private static final long RETRY_SLEEP = 50;
	private static final int RETRY_TIMES = 20;
	
	private final Queue<Event> pending;
	
	private boolean isHalted = false;
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private final Logger log = Logger.getLogger(getClass());*/
	
	private final Sender<Event<?>> sender;
	
	WrappedSender(Sender<Event<?>> sender) {
		// this.pending = new ConcurrentLinkedQueue<Event>();
		this.sender = sender;
	}
	
	public void destroy() {
		sender.destroy();
	}
	
	public void dispatch(T event) throws ChannelNotFoundException {
		sender.dispatch(event);
	}
	
	public String getOwnerId() {
		return sender.getOwnerId();
	}
	
	public void halt() { }
	
	public boolean isHalted() {
		return false;
	}
	
	public void resume() { }
	
	
	/*public void halt() {
		lock.writeLock().lock();
		try {
			isHalted = true;
			// doLockedHalt();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public boolean isHalted() {
		lock.readLock().lock();
		try {
			return isHalted;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public void resume() {
		lock.writeLock().lock();
		try {
			isHalted = false;
			doLockedResume();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void destroy() { 
		sender.destroy();
	}

	public void dispatch(Event event) throws ChannelNotFoundException {
		for (int i = 0; i < RETRY_TIMES; i++) {
			if(tryDispatch(event)) {
				return; // DONE!
			} else {
				attemptRetrySleep();
			}
		}
		throw new IllegalStateException("Failed to send message after " + RETRY_TIMES + " retry attempts!");
	}

	public String getOwnerId() {
		return sender.getOwnerId();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void attemptRetrySleep() {
		if(RETRY_SLEEP > 0) {
			try {
				Thread.sleep(RETRY_SLEEP);
			} catch (InterruptedException e) { }
		}
	}

	private boolean tryDispatch(Event event) throws ChannelNotFoundException {
		lock.readLock().lock();
		try {
			return tryLockedDispatch(event);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	private boolean tryLockedDispatch(Event event) throws ChannelNotFoundException {
		try {
			if(isHalted) {
				pending.add(event);
			} else {
				doDispatch(event);
			}
			return true;
		} catch(IllegalArgumentException e) {
			*
			 * We'll assume that this is because a node down, ie. that jgroups
			 * sends this error when we try to single-cast to a non-member. /LJN
			 *
			if(log.isTraceEnabled()) {
				log.trace("Failed to send message, retry possible.", e);
			}
			return false;
		}
	}
	
	private void doDispatch(Event e) throws ChannelNotFoundException {
		sender.dispatch(e);
	}
	
	private void doLockedResume() {
		dispatchPending();
		clearPending();
	}

	private void clearPending() {
		pending.clear();
	}

	private void dispatchPending() {
		for (Event e : pending) {
			try {
				doPendingDispatch(e);
			} catch (ChannelNotFoundException e1) {
				log.error("Failed to deliver pending event on resume!", e1);
			}
		}
	}

	private void doPendingDispatch(Event e) throws ChannelNotFoundException {
		for (int i = 0; i < RETRY_TIMES; i++) {
			if(tryLockedDispatch(e)) {
				return; // DONE!
			} else {
				attemptRetrySleep();
			}
		}	
		log.error("Failed to send pending message after " + RETRY_TIMES + " retry attempts!");
	}*/
}