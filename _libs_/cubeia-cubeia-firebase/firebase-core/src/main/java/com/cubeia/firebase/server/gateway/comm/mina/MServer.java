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

import static com.cubeia.firebase.server.gateway.GatewayNode.CLIENT_GATEWAY_NAMESPACE;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoService;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.integration.jmx.IoServiceManager;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.api.util.StringList;
import com.cubeia.firebase.server.gateway.ServerConfig;
import com.cubeia.firebase.server.gateway.comm.Server;
import com.cubeia.firebase.server.gateway.comm.config.GatewayClusterConfig;
import com.cubeia.firebase.server.gateway.comm.crypto.CryptoFilter;
import com.cubeia.firebase.server.gateway.comm.mina.protocol.HandshakeFilter;
import com.cubeia.firebase.server.gateway.comm.mina.protocol.StyxProtocolFactory;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.server.node.ClientNodeContext;
import com.cubeia.firebase.server.service.crypto.SystemCryptoProvider;
import com.cubeia.firebase.server.service.crypto.SystemKeyStore;
import com.cubeia.firebase.server.statistics.Level;
import com.cubeia.firebase.server.statistics.StatisticsLevel;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;


/**
 * Client Gateway socker server implementation that uses Apache MINA.
 * 
 * @author Fredrik
 *
 */
public class MServer implements Server {
	
	/** The logger */
	private transient Logger log = Logger.getLogger(this.getClass());
	
	/** Current state */
	private State state = State.STOPPED;
	
	/** The MINA NIO-acceptor */
	private IoAcceptor acceptor;

	/** Node context */
	private ClientNodeContext con;
	private CryptoFilter cryptoFilterInstance;
	private int maxNumberOfSessions;
	
	CryptoFilter getCryptoFilterInstance() {
		return cryptoFilterInstance;
	}
	
	int getMaxNumberOfSessions() {
		return maxNumberOfSessions;
	}

	// verify packet size flag
	public static boolean verifyPacketSize = true;
	public static int maxPacketSize = 32768;
	
	
	private final int ioThreads;
	private final ServerConfig conf;
	private final SystemCryptoProvider prov;
	
	/*
	 * Store should be null if no ssl is configured
	 */
	public MServer(ServerConfig conf, int ioThreads, SystemCryptoProvider prov) throws SystemException {
		this.conf = conf;
		this.ioThreads = ioThreads;
		this.prov = prov;
	}
	
	/**
	 * Initialize with context injection
	 */
	public void init(ClientNodeContext con) throws SystemException {
		this.con = con;
	}

	public String getStateDescription() {
		return state.toString();
	}

	/**
	 * Starts the socket listener
	 */
	public void start() {
		try {
			setupClientRegistry();
			setupServer();
			initJMX();
			state = State.STARTED;
		} catch (Exception e) {
			log.fatal("Could not start Server: "+e, e);
		}
	}

	public void stop() {
		acceptor.unbindAll();
		state = State.STOPPED;
		destroyJMX();
	}

	public void destroy() {
		acceptor = null;
		con = null;
	}
	
	IoService getIoService() {
		return acceptor;
	}
	
	
	/**
	 * Setup the client registry with node id information.
	 * 
	 * @param con2
	 */
	private void setupClientRegistry() {
		ClientRegistry clientRegistry = con.getServices().getServiceInstance(ClientRegistryServiceContract.class).getClientRegistry();
		if (clientRegistry != null) {
			clientRegistry.addNodeId(con.getNodeRouter().getId());
		} else {
			log.warn("Could not set node-id to client registry since client registry was not provided");
		}
	}
	
	/**
	 * Setup and start the socket acceptor (server listener)
	 * 
	 * @throws IOException
	 * @throws Exception 
	 */
	
	@SuppressWarnings("unchecked")
	private void setupServer() throws Exception {
		log.debug("Constructing MINA server; Io threads: " + ioThreads);
		acceptor = new SocketAcceptor(ioThreads, Executors.newCachedThreadPool());
		acceptor.getDefaultConfig().setThreadModel(ThreadModel.MANUAL);
		
        // Prepare the configuration
        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getSessionConfig().setReuseAddress(true);
        cfg.setReuseAddress(true);

        // Get the cluster wide configuration
		ClusterConfigProviderContract clusterConfigService = con.getServices().getServiceInstance(ClusterConfigProviderContract.class);

		// Get the client gateway configuration from cluster.props
		GatewayClusterConfig config = clusterConfigService.getConfiguration(GatewayClusterConfig.class, CLIENT_GATEWAY_NAMESPACE);

		// MINA logging enabled?
		if ( config.isMinaLogginEnabled() ) {
			enableMinaLogging(cfg);
		}
		
		// Verify packet size?
		verifyPacketSize = config.isValidatePacketSize();
		
		// User handshake?
		if ( config.isHandshakeEnabled() ) {
	       	enableHandshake(cfg, config);
	    }
	    
		boolean forceEncryption = false;
		Class<CryptoFilter> filter = prov.getMinaEncryptionFilter();
		SystemKeyStore store = prov.getSystemKeyStore();
		
		if (store != null) {
			if(filter != null) {
				log.warn("Both SSL (server configuration) and encryption (cluster configuration) are enabled, will prefer SSL and disable encryption.");
			} else {
				log.info("SSL encryption (server configuration) enabled.");
			}
		} else if(filter != null) {
			forceEncryption = prov.isEncryptionMandatory();
			log.info("Native firebase encryption (cluster configuration) enabled, participation " + (forceEncryption ? "mandatory" : "optional") + ".");
			enableEncryption(cfg, filter);
		}
		
		// Max packet size
		maxPacketSize = config.getMaxPacketSize();
		
		// Max number of sessions
		maxNumberOfSessions = config.getMaxNumberOfSessions();
		
		// Custom filter chain
		StringList customFilterChain = config.getCustomFilterChain();
		
		for ( int i = 0; i < customFilterChain.size(); i ++) {
			try {
				String customFilterClassName = customFilterChain.get(i);
				if ( !customFilterClassName.equals("null") ) {
					Class<IoFilterAdapter> customFilter = (Class<IoFilterAdapter>) MServer.class.getClassLoader().loadClass(customFilterClassName );
					cfg.getFilterChain().addLast( customFilterClassName, customFilter.newInstance() );
					log.info("Custom gateway filter installed: " + customFilterClassName);
				}
			} catch ( ClassNotFoundException e) {
				log.warn("Can't find custom filter class: " + customFilterChain.get(i));
			}
		}
		
		if(store != null) {
			cfg.getFilterChain().addFirst("ssl", new SSLFilter(store.createSSLContext()));
		}
		
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new StyxProtocolFactory(forceEncryption) ) );

        SocketAddress sa = conf.getClientBindAddress();
        InetSocketAddress ia = new InetSocketAddress(sa.getHost(), sa.getPort());
        
        // Bind
        acceptor.bind(
                ia,
                new SessionHandler(con, this), cfg );
        
        SystemLogger.info("NIO Server started at: " + ia);
        
//		System.out.println("----------------------------");
//		System.out.println(" Listener: NIO Socket Server");
//		System.out.println(" IFace:    " + ia);
//		System.out.println("----------------------------");
	}

	/**
	 * Enable extensive Mina logging
	 * Note: this produces a lot of output
	 * @param cfg
	 */
	private void enableMinaLogging(SocketAcceptorConfig cfg) {
		cfg.getFilterChain().addLast( "logger", new LoggingFilter() );
	}


	/**
	 * Enable handshake
	 * @param cfg
	 * @param config
	 */
	private void enableHandshake(SocketAcceptorConfig cfg, GatewayClusterConfig config) {
		cfg.getFilterChain().addLast( "handshake", new HandshakeFilter(config.getHandshakeSignature()) );
	}

	private void enableEncryption(SocketAcceptorConfig cfg, Class<CryptoFilter> filter) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// String cryptoFilterClass;
		// get class name for crypto filter
		// cryptoFilterClass = config.getEncryptionFilter();
		    
		if ( filter != null ) {
			// load class
			// Class<CryptoFilter> filter =	(Class<CryptoFilter>) MServer.class.getClassLoader().loadClass(cryptoFilterClass);
			// create instance
			cryptoFilterInstance = filter.newInstance();
			// Note: Will NOT add the crypto filter to the chain. The coding is done by the StyxCodec instead.
			//cfg.getFilterChain().addLast( "crypto", cryptoFilterInstance);
		}
	}
	
	
	private void initJMX() {
		try {
			if (StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {
				IoServiceManager serviceManager = new IoServiceManager( acceptor );
				serviceManager.startCollectingStats(1000);
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				ObjectName name = new ObjectName("com.cubeia.firebase.gateway:type=MServer");
				mbs.registerMBean(serviceManager, name);
				
				//ClientServerStats stats = new ClientServerStats(acceptor);
				//mbs.registerMBean(stats, new ObjectName("com.cubeia.firebase.gateway:type=ServerStats"));
				
			}
		} catch (Exception e) {
			log.warn("Could not bind MServer to JMX", e);
		}
	}

	private void destroyJMX() {
		try {
			if (StatisticsLevel.getInstance().isEnabled(Level.PROFILING)) {
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				ObjectName name = new ObjectName("com.cubeia.firebase.gateway:type=MServer");
				if(mbs.isRegistered(name)) {
					mbs.unregisterMBean(name);
				}
			}
		} catch (Exception e) {
			log.warn("Could not bind MServer to JMX", e);
		}
	}	
}
