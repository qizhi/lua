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
package com.cubeia.firebase.server.gateway.comm.mina;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.comm.AbstractClient;
import com.cubeia.firebase.server.gateway.util.CompilationCache;
import com.cubeia.firebase.server.node.ClientNodeContext;

public class GameClient extends AbstractClient {

	private final IoSession session;

	public GameClient(ClientNodeContext context, IoSession session) {	
		super(context);
		this.session = session;
		// use Mina for reporting idle status
        long millis = pingSession.getMaxIdleTime();
        if(millis != -1) {
        	int secs = (int)(millis / 1000);
        	if(secs == 0) {
        		secs = 1;
        	}
        	session.setIdleTime(IdleStatus.READER_IDLE, secs);
        }
	}

	public SocketAddress getLocalAddress() {
		return session.getLocalAddress();
	}

	public void sendClientPacket(ProtocolObject packet) {
	    if (log.isTraceEnabled()) { 
            log.trace("sendClientPacket Client["+getId()+":"+getSessionId()+"]: "+packet); 
        }
		session.write(packet);
	}
	
	@Override
	public void sendClientPackets(CompilationCache packets) {
		Collection<byte[]> packs = packets.getSyxBinary();
		for (byte[] arr : packs) {
			session.write(arr);
		}
	}
	
	@Override
	public void sendClientPackets(List<ProtocolObject> packets) {
		for (ProtocolObject o : packets) {
			sendClientPacket(o);
		}
	}
	
	public void close() {
		super.close();
		session.close();
	}
	
	public InetSocketAddress getRemoteAddress() {
		SocketAddress remoteAddress = session.getRemoteAddress();
		if (remoteAddress instanceof InetSocketAddress) {
			InetSocketAddress inetAddress = (InetSocketAddress) remoteAddress;
			return inetAddress;
		} else {
			return null;
		}
	}

    public int getWriteQueueSize() {
        return session.getScheduledWriteRequests();
    }

    public int getWriteQueueBytes() {
        return session.getScheduledWriteBytes();
    }
    
    public void pingDisconnect() {
    	super.pingDisconnect();
    	session.close();
    }
    
    
    // --- PACKAGE METHODS --- //
    
	@Override
	protected void dispatch(List<ProtocolObject> packets) {
		super.dispatch(packets);
	}
}
