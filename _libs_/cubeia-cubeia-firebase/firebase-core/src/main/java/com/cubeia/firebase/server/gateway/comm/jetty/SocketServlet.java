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

import static com.cubeia.firebase.server.gateway.GatewayNode.CLIENT_GATEWAY_NAMESPACE;
import static com.cubeia.firebase.server.gateway.comm.jetty.HttpUtil.checkHandshake;
import static com.cubeia.firebase.server.gateway.comm.jetty.HttpUtil.createSocketAddress;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.server.gateway.comm.config.GatewayClusterConfig;
import com.cubeia.firebase.server.node.ClientNodeContext;

/**
 * This is a web socket client layer. It uses the JSON wire format 
 * of Styx packets exclusively.
 * 
 * <p>This servlet accepts both single JSON encoded protocol objects as well as a list of the same, 
 * and it will always write a list of objects.
 * 
 * <p>The upgrade request must provide a handshake signature if the server
 * is configured with one. This is done via the "X-Cubeia-Firebase-Handshake" 
 * request header (alternatively if can be set in a cookie, or a request parameter, 
 * with the same name).
 * 
 * @author Lars J. Nilsson
 */
public class SocketServlet extends WebSocketServlet {

	private static final long serialVersionUID = 33977554975636417L;

	private final Logger log = Logger.getLogger(getClass());
	private final ClientNodeContext context;
	private String handShake; // handshake signature, null if not used
	
	public SocketServlet(ClientNodeContext context) {
		this.context = context;
		setupHandshake();
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest req, String protocol) {
		InetSocketAddress address = createSocketAddress(req);
		log.debug("Creating socket for remote IP: " + address);
		return new Socket(new JettyAsyncClient(context, address), address);
	}


	
	@Override
	public boolean checkOrigin(HttpServletRequest request, String origin) {
		boolean b = checkHandshake(request, handShake);
		if(b) {
			log.trace("Correct handshake signature received from: " + createSocketAddress(request));
		} else {
			log.debug("Incorrect handshake signature received from: " + createSocketAddress(request));
		}
		return b;
	}


	

	// --- PRIVATE METHODS --- //
	
	private void setupHandshake() {
        ClusterConfigProviderContract clusterConfigService = context.getServices().getServiceInstance(ClusterConfigProviderContract.class);
		GatewayClusterConfig config = clusterConfigService.getConfiguration(GatewayClusterConfig.class, CLIENT_GATEWAY_NAMESPACE);
		if(config.isHandshakeEnabled()) {
			log.info("SocketServlet enabling handshake with signature: " + config.getHandshakeSignature());
			handShake = String.valueOf(config.getHandshakeSignature());
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Socket extends BaseSocketConnection implements WebSocket, OnTextMessage {

		private Connection connection;

		public Socket(AsyncSession session, InetSocketAddress ia) {
			super(session, ia, true);
		}
		
		@Override
		public void forceClose() {
			connection.close();
		}

		@Override
		public void doSend(String msg) throws IOException {
			connection.sendMessage(msg);
		}
		
		@Override
		public void onOpen(Connection connection) {
			this.connection = connection;
			super.doOpen();
		}
		
		@Override
		public void onMessage(String data) {
			super.doMessage(data);
		}

		@Override
		public void onClose(int closeCode, String message) {
			super.doClose();
		}
	}
}
