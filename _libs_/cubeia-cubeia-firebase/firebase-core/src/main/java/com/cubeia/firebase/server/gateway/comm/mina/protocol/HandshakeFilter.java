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
package com.cubeia.firebase.server.gateway.comm.mina.protocol;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

/**
 * Apache MINA filter for basic DOS attack protection
 * 
 * @author peter
 *
 */
public class HandshakeFilter extends IoFilterAdapter {

	private static transient Logger log = Logger.getLogger(HandshakeFilter.class);
	
	/**
	 * Constructor
	 * @param signature - signature to be checked, defined in cluster.props
	 */
	public HandshakeFilter(int signature) {
		this.signature = signature;
	}
	

	/**
	 *  Called when mina has received something on the socket
	 *  
	 *  @param nextFilter - reference to the next filter/codec in the chain
	 *  @param session - Session object
	 *  @param message - buffer from MINA, it should be a ByteBuffer otherwise
	 *                   there is problem with the filter chain.
	 */
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) {
		try {
			if ( session.containsAttribute("handshakeDone") ) {
				nextFilter.messageReceived(session, message);
			} else {
				// message should be a ByteBuffer or there is a filter chain problem
				if (message instanceof ByteBuffer) {
					ByteBuffer in = (ByteBuffer) message;
					// check for initial empty buffer
					if(!in.hasRemaining()) {
						nextFilter.messageReceived(session, message); // GOTCHA, this could actually be valid
						return; 
					}
					// check signature, close session if mismatch
					if ( in.getInt() != signature ) {
						log.debug("Invalid handshake received from " + session.getRemoteAddress());
						session.close();
						return;
					}
					session.setAttribute("handshakeDone");
					log.debug("Handshake received from " + session.getRemoteAddress());
					// log.debug("Remaining: " + in.remaining());
					// Ooops, we need to forward the rest...
					nextFilter.messageReceived(session, message);
				} else {
					log.warn("Dont know how to decode:" + message.getClass());
					log.warn("Is the protocol filter chain correct?");
				}
			}
		} catch (Exception e) {
			log.error("Exception in HandshakeFilter");
			e.printStackTrace();
		}
	}
	
	private final int signature;
	
}

