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
import static com.cubeia.firebase.server.gateway.comm.jetty.HttpUtil.getRequestHandshake;
import static com.cubeia.firebase.server.gateway.comm.jetty.JsonUtil.pack;
import static com.cubeia.firebase.server.gateway.comm.jetty.JsonUtil.unpack;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.comm.config.GatewayClusterConfig;
import com.cubeia.firebase.server.node.ClientNodeContext;

/**
 * This is a comet-style, long-poll servlet for HTTP communication. It uses
 * the JSON wire format of Styx packets exclusively. It uses two methods:
 * 
 * <ul>
 *   <li>POST - This is used to post incoming events, and will always return an empty body</li>
 *   <li>GET - This is used to poll for new outgoing events, may return null on timeout</li>
 * </ul>
 * 
 * Clients need to accept the JSESSIONID cookie in order to track their session. For this reason it
 * is recommended the first call is a login POST. However, should a GET arrive to a new session it will
 * return immediately (as opposed to long-poll) for the cookie to be returned. 
 * 
 * <p>This servlet accepts both single JSON encoded protocol objects as well as a list of the same, 
 * and it will always write a list of objects on GET.
 * 
 * <p>The first request, which sets up the session must provide a handshake signature if the server
 * is configured with one. This is done via the "X-Cubeia-Firebase-Handshake" request header (alternatively
 * if can be set in a cookie, or a request parameter, with the same name).
 * 
 * @author Lars J. Nilsson
 */
@Deprecated
public class CometServlet extends HttpServlet {

	private static final long serialVersionUID = 3340004841079443148L;
	private static final String ASYNC_SESSION = "async_session";

	private final Logger log = Logger.getLogger(getClass());
	
	private final ClientNodeContext context;
	private String handShake; // handshake signature, null if not used
	
	public CometServlet(ClientNodeContext context) {
		this.context = context;
		setupHandshake();
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		/*
		 * TODO Do we need this?
		 */
		ClassLoader mine = getClass().getClassLoader();
		ClassLoader old = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(mine);
		try {
			super.service(req, resp);
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.trace("GET");
		/*
		 * If the session is new we return immediately
		 * to establish the session cookie at the client.
		 */
		if(req.getSession().isNew()) {
			checkGetSession(req);
			return;
		}
		/*
		 * Get hold of the client session and take (wait) 
		 * for outgoing events. Pack as JSON and return.
		 */
		AsyncSession session = checkGetSession(req);
		if(session == null) {
			// Missing handshake
			resp.setStatus(SC_FORBIDDEN);
			return; // EARLY RETURN
		}
		List<ProtocolObject> list = session.takeOutgoing(); // TODO Add timeout
		if(list.size() > 0) {
			String json = pack(list);
			log.trace("RETURN: " + json);
			// TODO Set headers and stuff...
			resp.getWriter().write(json);
		} else {
			log.trace("EMPTY");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		log.trace("POST");
		/*
		 * Get hold of the client session and post incoming
		 * data (if any).
		 */
		AsyncSession session = checkGetSession(req);
		if(session == null) {
			// Missing handshake
			resp.setStatus(SC_FORBIDDEN);
			return; // EARLY RETURN
		}
		// TODO Check headers and stuff
		String data = readPost(req).trim();
		if(data.length() > 0) {
			log.trace("Incoming http message: " + data);
			unpack(data, session);
		}
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
	
	/*
	 * Check if a client session exists, and if it don't create
	 * it and store in HTTP session. Return NULL on denied.
	 */
	private AsyncSession checkGetSession(HttpServletRequest req) {
		HttpSession session = req.getSession();
		AsyncSession ses = (AsyncSession) session.getAttribute(ASYNC_SESSION);
		if(ses == null) {
			String ip = req.getRemoteAddr();
			int port = req.getRemotePort();
			InetSocketAddress address = new InetSocketAddress(ip, port);
			String theirs = getRequestHandshake(req);
			if(handShake != null && handShake.equals(theirs)) { // match on string to avoid errors on non-integers
				log.debug("Invalid handshake received from: " + address);
				return null; // DENY!!!
			} else {
				log.debug("Creating session for remote IP: " + address + "; Using handshake: " + (handShake != null));
				ses = new JettyAsyncClient(context, address);
				session.setAttribute(ASYNC_SESSION, ses);
			}
		}
		return ses;
	}
	
	/*
	 * Read entire POST body as a string.
	 */
	private String readPost(HttpServletRequest req) throws IOException {
		StringBuilder b = new StringBuilder();
		BufferedReader reader = req.getReader();
		String s = null;
		while((s = reader.readLine()) != null) {
			b.append(s);
		}
		return b.toString();
	}
}
