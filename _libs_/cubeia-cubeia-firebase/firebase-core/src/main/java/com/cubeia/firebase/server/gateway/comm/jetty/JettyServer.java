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

import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.gateway.GatewayConfig;
import com.cubeia.firebase.server.gateway.ServerConfig;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.server.node.ClientNodeContext;
import com.cubeia.firebase.server.service.crypto.SystemCryptoProvider;

/**
 * This is a server for a Jetty instance. It starts and stop the embedded
 * Jetty on init/destroy. It mounts two contexts:
 * 
 * <ul>
 * 	<li>/cometd - for CometD style Bayeux communication</li>
 *  <li>/socket - for Web Socket communication</li>
 * </ul>
 * 
 * @author Lars J. Nilsson
 */
public class JettyServer implements Initializable<ClientNodeContext> {

	public static final String HANDSHAKE_HTTP_HEADER = "X-Cubeia-Firebase-Handshake";
	
	private Server server;
	private ClientNodeContext con;
	
	private final Logger log = Logger.getLogger(getClass());
	private final ServerConfig serverConfig;
	private final SystemCryptoProvider cr;
	private final GatewayConfig nodeConf;
	private final CrossOriginConfig crossConfig;
	
	public JettyServer(GatewayConfig conf, ServerConfig config, CrossOriginConfig crossConfig, SystemCryptoProvider cr) {
		this.nodeConf = conf;
		this.serverConfig = config;
		this.crossConfig = crossConfig;
		this.cr = cr;
	}
	
	@Override
	public void init(ClientNodeContext con) throws SystemException {
		this.con = con;
		try {
			SocketAddress sa = serverConfig.getWebClientBindAddress();
			server = new Server();
			
			// Setup Jetty JMX
			MBeanContainer mbContainer=new MBeanContainer(con.getMBeanServer());
			server.getContainer().addEventListener(mbContainer);
			server.addBean(mbContainer);
			
			// Regular connector
			Connector connector = new SelectChannelConnector();
			connector.setHost(sa.getHost().getHostAddress());
			connector.setPort(sa.getPort());
			
			String sslConfig = "Not Configured";
			
			if (cr.getSystemKeyStore() == null) {
				
				// NO SSL
				server.setConnectors(new Connector[] { connector });
				
			} else {
				
				// Setup SSL Connector
				SocketAddress sslSa = serverConfig.getWebClientSslBindAddress();
				SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
				ssl_connector.setPort(sslSa.getPort());
				SslContextFactory cf = ssl_connector.getSslContextFactory();
				SSLContext sslContext = cr.getSystemKeyStore().createSSLContext();
				cf.setSslContext(sslContext);
				
				sslConfig = sslSa.getHost() + ":" + sslSa.getPort();
				
				server.setConnectors(new Connector[] { connector, ssl_connector });
			}
			
			// TODO More configuration
			setupServlets();
			server.start();
			
			SystemLogger.info("HTTP Server starting at: " + connector.getHost() + ":" + connector.getPort() + "; SSL configuration: " + sslConfig);
			
//	        System.out.println("----------------------------");
//	        System.out.println(" Listener: HTTP Socket Server");
//	        System.out.println(" IFace:    /" + connector.getHost()+":"+connector.getPort());
//	        System.out.println(" SSL:      " + sslConfig);
//	        System.out.println("----------------------------");
			
		} catch (Exception e) {
			throw new SystemCoreException("Failed to start jetty", e);
		}
	}

	@Override
	public void destroy() {
		try {
			server.stop();
		} catch (Exception e) {
			log.error("Failed to stop Jetty", e);
		}
	}

	
	// --- PRIVATE METHODS --- //
	
	private void setupServlets() {
		ServletContextHandler context1 = createWsContext();
		if(this.nodeConf.disableStaticHttpContent()) {
			log.debug("Static content is disabled.");
			// only non-static content
			server.setHandler(context1);
		} else {
			log.info("Static content is served from: " + nodeConf.getStaticWebDirectory().getAbsolutePath());
			// add static handler
			ServletContextHandler context2 = createStaticContext();
			// combine and add to server
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[] { context1, context2 });
	        server.setHandler(contexts);
		}
	}

	private ServletContextHandler createStaticContext() {
		ServletContextHandler context = new ServletContextHandler();
		context.setClassLoader(getClass().getClassLoader());
		context.setContextPath("/static");
		ServletHolder holder = new ServletHolder(new DefaultServlet());
		holder.setInitParameter("resourceBase", nodeConf.getStaticWebDirectory().getAbsolutePath());
		holder.setInitParameter("dirAllowed", String.valueOf(nodeConf.allowStaticWebDirectoryListing()));
		context.addServlet(holder, "/*");
		return context;
	}

	private ServletContextHandler createWsContext() {
		// context
		ServletContextHandler context = new ServletContextHandler(SESSIONS);
		context.setClassLoader(getClass().getClassLoader());
		context.setContextPath("/");
		// add comet
		// context.addServlet(new ServletHolder(new CometServlet(this.con)), "/http");
		// add web socket
		ServletHolder hold = new ServletHolder(new SocketServlet(this.con));
		hold.setInitParameter("maxTextMessageSize", String.valueOf(nodeConf.getJsonMaxTextMessageSize()));
		hold.setInitParameter("maxIdleTime", String.valueOf(nodeConf.getWebSocketMaxIdleTimeout()));
        context.addServlet(hold, "/socket");
		// add cometd
        hold = new ServletHolder(new BayeuxServlet(this.con));
        hold.setInitParameter("logLevel", "3");
        hold.setInitParameter("maxInterval", String.valueOf(nodeConf.getCometIdleTimeout()));
        hold.setInitParameter("timeout", String.valueOf(nodeConf.getCometPollTimeout()));
        hold.setInitParameter("jsonContext", CometdJsonContext.class.getName());
        context.addServlet(hold, "/cometd/*");
        if(nodeConf.enableHttpCrossOriginFilter()) {
	        // add cross-origin filter
	        FilterHolder filter = new FilterHolder(CrossOriginFilter.class);
	        filter.setInitParameter("allowedOrigins", crossConfig.getAllowedOrigins().toString());
	        filter.setInitParameter("allowedMethods", crossConfig.getAllowedMethods().toString());
	        filter.setInitParameter("allowedHeaders", crossConfig.getAllowedHeaders().toString());
	        filter.setInitParameter("preflightMaxAge", String.valueOf(crossConfig.getPreflightMaxAge()));
	        filter.setInitParameter("allowCredentials", String.valueOf(crossConfig.getAllowCredentials()));
	        log.debug("Cross-Origin filter is enabled; " +
	        		"allowed origins: " + crossConfig.getAllowedOrigins().toString() + ";" +
	        		"allowed methods: " + crossConfig.getAllowedMethods().toString() + ";" +
	        		"allowed headers: " + crossConfig.getAllowedHeaders().toString());
	        		
	        
	        context.addFilter(filter, "/*", null);
        }
        // return
        return context;
	}
}

