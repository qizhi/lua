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
import static com.cubeia.firebase.server.gateway.comm.jetty.ClientService.REMOTE_ATTR;
import static com.cubeia.firebase.server.gateway.comm.jetty.ClientService.SESSION_ATTR;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.BayeuxServer.SessionListener;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.CometdServlet;

import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.comm.config.GatewayClusterConfig;
import com.cubeia.firebase.server.node.ClientNodeContext;

/**
 * An extension of the standard CometD servlet that captures remote IP 
 * addresses, listens for destroyed sessions, and checks for an initial
 * handshake. Configuration is done in the {@link JettyServer}. The channel
 * to use for clients is "/service/client".
 * 
 * <p>If the server requires a Firebase handshake this should be put
 * in the extension of a CometD handshake as the sole property "handshakeSignature",
 * like so:
 * 
 * <pre>
 * 	cometd.handshake({
 *    ext: {
 *      handshakeSignature: 666
 *    }
 *  });
 * </pre>
 * 
 * This server also denies access to any other channels than "/service/client" which
 * is where clients should connect.
 * 
 * @author Lars J. Nilsson
 */
public class BayeuxServlet extends CometdServlet {

	private static final long serialVersionUID = 1L;
	
	private final Logger log = Logger.getLogger(getClass());
	private final ThreadLocal<String> currentRemote = new ThreadLocal<String>();
	
	private final ClientNodeContext context;
	private String handShake; // handshake signature, null if not used

	public BayeuxServlet(ClientNodeContext context) {
		this.context = context;
		setupHandshake();
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		log.debug("CometD Servlet Init");
		new ClientService(getBayeux(), context); // ADD CLIENT SERVICE
		getBayeux().addListener(new Listener());
		getBayeux().setSecurityPolicy(new Policy());
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Add remote IP and port to a thread local in order to have
		 * them handy for the client creation.
		 */
		captureRemoteAddress(request);
		try {
			super.service(request, response);
		} finally {
			currentRemote.set(null);
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void setupHandshake() {
        ClusterConfigProviderContract clusterConfigService = context.getServices().getServiceInstance(ClusterConfigProviderContract.class);
		GatewayClusterConfig config = clusterConfigService.getConfiguration(GatewayClusterConfig.class, CLIENT_GATEWAY_NAMESPACE);
		if(config.isHandshakeEnabled()) {
			log.info("BayeuxServlet enabling handshake with signature: " + config.getHandshakeSignature());
			handShake = String.valueOf(config.getHandshakeSignature());
		}
	}

	private void captureRemoteAddress(HttpServletRequest request) {
		/*
		 * Capture as string as we don't want any host name lookups
		 * until actual client creation
		 */
		String ip = request.getRemoteAddr();
		int port = request.getRemotePort();
		currentRemote.set(ip + ":" + port);
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	/**
	 * This policy denies channel creation outright, checks for handshake signatures
	 * on the CometD handshake, and limits publish/subscribe to the /service/client channel.
	 */
	private class Policy implements SecurityPolicy {

		@Override
		public boolean canCreate(BayeuxServer serv, ServerSession ses, String channel, ServerMessage msg) {
			log.debug("Denying creation of channel " + channel  + " for session: " + ses);
			return false;
		}

		@Override
		public boolean canHandshake(BayeuxServer serv, ServerSession ses, ServerMessage msg) {
			if(handShake != null) {
				/*
				 * Check that handshake exists in EXT and matches...
				 */
				Map<String, Object> ext = msg.getExt();
				Object check = (ext == null ? null : ext.get("handshakeSignature"));
				String theirs = (check == null ? null : check.toString());
				if(handShake.equals(theirs)) {
					log.trace("Correct handshake received from: " + currentRemote.get());
					return true;
				} else {
					log.debug("Invalid handshake (" + check + ") received from: " + currentRemote.get());
					return false;
				}
			} else {
				return true;
			}
		}

		@Override
		public boolean canPublish(BayeuxServer serv, ServerSession ses, ServerChannel channel, ServerMessage msg) {
			boolean b = channel.getId().equals(ClientService.CHANNEL);
			if(!b) {
				log.debug("Denying publish on channel " + channel .getId() + " for session: " + ses);
			}
			return b;
		}

		@Override
		public boolean canSubscribe(BayeuxServer serv, ServerSession ses, ServerChannel channel, ServerMessage msg) {
			boolean b = channel.getId().equals(ClientService.CHANNEL);
			if(!b) {
				log.debug("Denying subscribe on channel " + channel .getId() + " for session: " + ses);
			}
			return b;
		}
	}
	
	
	/**
	 * This session listener creation a remote address and sets on the initial session (needed
	 * in order to create the client), and disconnects the client when a session is removed.
	 */
	private class Listener implements SessionListener {
		
		@Override
		public void sessionAdded(ServerSession session) {
			InetSocketAddress address = createRemoteAddress();
			log.trace("Creating session for remote IP: " + address );
			session.setAttribute(REMOTE_ATTR, address); 
		}
		
		@Override
		public void sessionRemoved(ServerSession session, boolean timedout) {
			AsyncSession ses = (AsyncSession) session.getAttribute(SESSION_ATTR);
			if(ses != null) {
				log.trace("Session destroyed for remote IP: " + ((Client) ses).getRemoteAddress());
				ses.setAsyncSessionListener(null);
				ses.disconnected();
			}
		}
		
		
		// --- PRIVATE METHODS --- //
		
		private InetSocketAddress createRemoteAddress() {
			/*
			 * If we have captured the remote address...
			 */
			String s = currentRemote.get();
			if(s != null) {
				int index = s.indexOf(":");
				String host = s.substring(0, index);
				int port = Integer.parseInt(s.substring(index + 1));
				return new InetSocketAddress(host, port);
			} else {
				return null;
			}
		}
	}
}
