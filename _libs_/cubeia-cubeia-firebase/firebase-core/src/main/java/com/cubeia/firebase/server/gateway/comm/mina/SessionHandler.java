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

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoSession;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.comm.crypto.CryptoProvider;
import com.cubeia.firebase.server.node.ClientNodeContext;

/**
 * {@link IoHandler} implementation.
 * 
 * Handles client sessions.
 * 
 * 
 */
public class SessionHandler extends IoHandlerAdapter {
	
	private transient Logger log = Logger.getLogger(this.getClass());
	
	private final ClientNodeContext con;
	private final MServer parent;
	
	public SessionHandler(ClientNodeContext con, MServer parent) {
		this.con = con;
		this.parent = parent;
	}
	
	/**
	 * Create a new GameClient object and attach it to the session.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
    public void sessionCreated( IoSession session ) throws Exception {
		
		// Check if max number of allowed sessions has been reached
		if ( countServiceSessions() >= parent.getMaxNumberOfSessions() ) {
			log.warn("Max number of sessions reached, closing session");
			session.close();
			return;
		}
		
        super.sessionCreated(session);        
        GameClient client = new GameClient(con, session);
        session.setAttachment(client);
        if (log.isDebugEnabled()) {
        	log.debug("Client Connected.: "+session);
        }
        
        // Enable encryption filter?
        if (parent.getCryptoFilterInstance() != null) {
	        // set up encryption filter
	        String cryptoProviderClassName = parent.getCryptoFilterInstance().getCryptoProviderClassName();
	        Class<CryptoProvider> cryptoProviderClass =	(Class<CryptoProvider>) SessionHandler.class.getClassLoader().loadClass(cryptoProviderClassName);
			CryptoProvider cryptoProviderInstance = cryptoProviderClass.newInstance();
			cryptoProviderInstance.createSessionKey();
			session.setAttribute("crypto", cryptoProviderInstance);
        }
	}

	@Override
    public void exceptionCaught( IoSession session, Throwable cause ) {
		if (!(cause instanceof IOException)) {
			log.info("Exception caught for session. Closing session: "+session, cause);
		}
        session.close();
    }

	/**
	 * Dispatch it to the attached GameClient
	 */
	@SuppressWarnings("unchecked")
	@Override
    public void messageReceived( IoSession session, Object message ) {
        List<ProtocolObject> packets = (List<ProtocolObject>) message;
        GameClient client = (GameClient) session.getAttachment();
        try {
            if (log.isTraceEnabled()) { log.trace("Client["+client+"] received packets: "+packets); }
        	client.dispatch(packets);
        } catch (Exception e) {
        	log.error("Failed to dispatch Client packet: "+e, e);
        }
    }
    
	

	@Override
    public void sessionOpened( IoSession session ) throws Exception {
		
	}

	@Override
    public void sessionClosed( IoSession session ) throws Exception {
		if (log.isDebugEnabled()) {
		    GameClient client = (GameClient)session.getAttachment();
			log.debug("Session closed: "+session.getAttachment()+" SessionId["+client.getSessionId()+"]");
        }
		Client client = (Client) session.getAttachment();
		handleDisconnect(client);
    }

	@Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		GameClient client = (GameClient)session.getAttachment();
		if(status == IdleStatus.READER_IDLE) {
			client.getPingSession().reportIdle();
		} else {
			log.warn("Received unexpected IDLE state from session; Client: " + client.getId() + "; Session: " + client.getSessionId());
		}
		
		
		/*if (log.isDebugEnabled()) {
			log.debug("Session idle (Will Remove): "+session.getAttachment()+" Status: "+status);
        }
		Client client = (Client)session.getAttachment();
		handleDisconnect(client);*/
    }
    
	@Override
    public void messageSent( IoSession session, Object message ) throws Exception {
	    if (log.isTraceEnabled()) { 
	        GameClient client = (GameClient) session.getAttachment();
	        log.trace("Sent message to Client["+client+"]: "+message); 
	    }
    }
	
	private int countServiceSessions() {
		/*
		 * This is in-efficient and will create a lot of garbage, but it'll
		 * have to work until we refactor and cleanup this class. /LJN
		 */
		int count = 0;
		IoService service = parent.getIoService();
		for (SocketAddress a : service.getManagedServiceAddresses()) {
			count += service.getManagedSessions(a).size();
		}
		return count;
	}
	
	/**
	 * A Client has left us.
	 * 
	 * @param client
	 */
	private void handleDisconnect(Client client) {
		client.disconnected();
	}
}