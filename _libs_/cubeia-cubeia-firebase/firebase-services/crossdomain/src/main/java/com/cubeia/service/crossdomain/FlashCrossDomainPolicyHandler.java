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
package com.cubeia.service.crossdomain;

import java.io.File;
import java.io.FileInputStream;
import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;

/**
 * Handler for flash domain policy file requests
 * 
 * @author peter
 * 
 */
public class FlashCrossDomainPolicyHandler extends IoHandlerAdapter {
	private Logger log = Logger.getLogger(this.getClass());
	// private long timestamp;
	private static final long CACHE_TTL = 900000;

	/**
	* Constructor 
	* @param crossDomainPolicyFile - file to feed to flash client
	*/
	public FlashCrossDomainPolicyHandler(File crossDomainPolicyFile) {
		super();
        cacheCrossDomainPolicyFile(crossDomainPolicyFile);
    }

	public void exceptionCaught(IoSession session, Throwable t)
			throws Exception {
		t.printStackTrace();
		session.close();
	}

	/**
	 * messageSent - called after mina has sent the message
	 */
	public void messageSent(IoSession session, Object message) throws Exception {
		// close session - this terminates the tcp session
		session.close();
	}

	/**
	 * messageReceived
	 * 
	 * called when a message has been decoded
	 */
	public void messageReceived(IoSession session, Object msg) throws Exception {
		String str = msg.toString();
		// is this a policy file request?
		if (str.trim().equalsIgnoreCase("<policy-file-request/>")) {
			log.info("Flash cross domain policy file request received from "
					+ session.getRemoteAddress().toString());
			serveCrossDomainPolicyFile(session);
			return;
		} else {
			log.warn("Invalid Flash cross domain policy file request received from "
					+ session.getRemoteAddress().toString());
			// terminate session
			session.close();
		}
	}

	private void serveCrossDomainPolicyFile(IoSession session) {
	  if(cacheHasExpired())  {
            cacheCrossDomainPolicyFile(crossDomainPolicyFile);
        }
        session.write(cachedCrossDomainPolicy);
    }

	private boolean cacheHasExpired() {
        long currentTime = System.currentTimeMillis();
        return currentTime > cacheTimestamp + CACHE_TTL;
    }

	/**
	* sessionCreated
	* 
	* called when a flash client has connected to the listening socket
	*/
	public void sessionCreated(IoSession session) throws Exception {
		if( session.getTransportType() == TransportType.SOCKET ) {
			((SocketSessionConfig) session.getConfig() ).setReceiveBufferSize( 2048 );
		}
		session.setIdleTime( IdleStatus.BOTH_IDLE, 10 );
	}

    private void cacheCrossDomainPolicyFile(File crossDomainPolicyFile) {
        try {
            this.crossDomainPolicyFile = crossDomainPolicyFile;
            FileInputStream input = new FileInputStream(crossDomainPolicyFile);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            // convert to string
            cachedCrossDomainPolicy = new String(buffer);
        } catch ( Exception e) {
            cachedCrossDomainPolicy = "<?xml version=\"1.0\"?><cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"4123\"/></cross-domain-policy>";
        }
        cacheTimestamp = System.currentTimeMillis();
    }

	private File crossDomainPolicyFile;
	private String cachedCrossDomainPolicy;
    private long cacheTimestamp;
}