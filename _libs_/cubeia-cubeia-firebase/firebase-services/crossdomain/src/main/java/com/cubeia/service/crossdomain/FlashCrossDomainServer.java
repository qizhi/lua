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
import java.io.IOException;
import java.net.InetSocketAddress;



import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

/**
 * FlashCrossDomainServer 
 * 
 * starts a mina server instance
 * @author peter
 *
 */
public class FlashCrossDomainServer {

	private Logger log = Logger.getLogger(this.getClass());
	
	 /**
	  * start server instance
	  * @param crossDomainPolicyFile - policy file to send to flash client 
	  * @param port - TCP port to use
	  * @throws IOException
	  */
	 public void start(File crossDomainPolicyFile, int port, boolean useMinaLogging) throws IOException {
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        IoAcceptor acceptor = new SocketAcceptor();

        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getSessionConfig().setReuseAddress( true );
        if ( useMinaLogging ) {
        	cfg.getFilterChain().addLast( "logger", new LoggingFilter() );
        }
        cfg.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new FlashCrossDomainCodecFactory( )));

        acceptor.bind( new InetSocketAddress(port), new FlashCrossDomainPolicyHandler(crossDomainPolicyFile), cfg);
        log.info("Flash cross domain policy server listening on port " + port);
        
    }
}
