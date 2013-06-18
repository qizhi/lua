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
package com.cubeia.firebase.server.gateway;

import java.util.LinkedList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.config.ServerConfigProviderContract;
import com.cubeia.firebase.api.service.dosprotect.DosProtector;
import com.cubeia.firebase.api.service.dosprotect.FrequencyRule;
import com.cubeia.firebase.api.service.dosprotect.Rule;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.gateway.comm.Server;
import com.cubeia.firebase.server.gateway.comm.jetty.CrossOriginConfig;
import com.cubeia.firebase.server.gateway.comm.jetty.JettyServer;
import com.cubeia.firebase.server.gateway.comm.mina.MServer;
import com.cubeia.firebase.server.gateway.event.ReceivingChatEventDaemon;
import com.cubeia.firebase.server.gateway.event.ReceivingClientEventDaemon;
import com.cubeia.firebase.server.gateway.jmx.CGWMonitor;
import com.cubeia.firebase.server.gateway.jmx.CGWMonitorMBean;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.node.BaseNode;
import com.cubeia.firebase.server.node.ClientNodeContext;
import com.cubeia.firebase.server.service.crypto.SystemCryptoProvider;
import com.cubeia.firebase.server.util.NodeRoles;
import com.cubeia.firebase.service.clientreg.ClientReaper;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.util.Lists;



/**
 * Service for starting a Client Gateway node.
 * 
 * The Gateway serves as a gateway (duh) to all cluster wide service.
 * Some services will execute locally (e.g. login) and some will propagate
 * to distributed services (e.g. game logic)
 * 
 * @author fredrik.johansson
 *
 */

public class GatewayNode extends BaseNode<ClientNodeContext> implements GatewayNodeMBean {
	
	private static final long MAX_MBUS_WAIT = 2000;

	// private static final String DEFAULT_SERVER_CONF = "com/cubeia/firebase/server/gateway/server.xml";
	public static final String GATEWAY_DOS_KEY = "_gateway";
	
	public static final Namespace CLIENT_GATEWAY_NAMESPACE = new Namespace("client.gateway");


	/// --- INSTANCE MEMBERS --- ///

	private Server minaServer;
	private Initializable<ClientNodeContext> jettyServer;
	//private ClientEventDaemon eventConsumer;
	private ReceivingClientEventDaemon clientDaemon;
	private ReceivingChatEventDaemon chatDaemon;
	private GatewayConfig conf;
	private ServerConfig sConf;

	/**
	 * Will only be instantiated on the coordinator
	 * (promoted GatewayNode).
	 */
	private ClientReaper reaper;
	

	public GatewayNode(String id) {
		super(id, ClusterRole.CLIENT_NODE);
		clientDaemon = new ReceivingClientEventDaemon(id);
	}

	public void destroy() { 
		if(reaper != null) {
			reaper.destroy();
		}
		if(clientDaemon != null) {
			clientDaemon.destroy();
		}
		if(chatDaemon != null) {
			chatDaemon.destroy();
		}
		if(jettyServer != null) {
			jettyServer.destroy();
		}
		destroyJmx();
		super.destroy();
	}

	public void init(ClientNodeContext con) throws SystemException {
		super.init(con);
		log.info("Init Client Gateway");
		setupConf();
		waitForMBus();
		// @SuppressWarnings("unused")
		// int port = sConf.getClientBindAddress().getPort();
		initLocalDosProtect();
		
		chatDaemon = new ReceivingChatEventDaemon(getId());
		chatDaemon.init(con);
		
		clientDaemon.init(con);
		
		SystemCryptoProvider cr = con.getServices().getServiceInstance(SystemCryptoProvider.class);
		// SystemKeyStore store = cr.getSystemKeyStore();
		
		/* MINA Server */
		minaServer = new MServer(sConf, conf.getAcceptorIoThreads(), cr);
		
		/* JETTY */
		if(conf.enableHttpServer()) {
			CrossOriginConfig crossConfig = readCrossConfig();
			jettyServer = new JettyServer(conf, sConf, crossConfig, cr);
			jettyServer.init(con);
		}
		
		initJmx();
	}

	@Override
	public boolean forceCleanupDisconnects() {
		if(reaper != null) {
			reaper.forceDisconnects();
			return true;
		} else {
			return false;
		}
	}

	/*private SSLKeyStore checkSsl() throws SystemException{
		if(sConf.isSslEnabled()) {
			return new SSLKeyStore(sConf);
		} else {
			return null;
		}
	}*/

	/**
	 * Service Interface 
	 *
	 */
	public void start() {
		log.info("Starting Client Gateway ("+con.getNodeRouter().getId()+") ...");
		
		
		// Init & Start the EventConsumers
		// eventConsumer = new ClientEventDaemon(getId(), con, mbs);
		// eventConsumer.start();
		clientDaemon.start();
		chatDaemon.start();
		
		// Init & Start the server
		log.debug("Setting context to server: "+con);
		try {
			minaServer.init(con);
			minaServer.start();
			bindStartLevelJMX();
			super.start();
		} catch (SystemException e) {
			log.fatal("Could not start the Client Gateway node. Reason: " + e.getMessage(), e);
			throw new IllegalStateException("Failed to start the client gateway node!");
		}
	}


	/**
	 * Service Interface 
	 *
	 */
	public void stop() {
		unbindStartLevelJMX();
		if(minaServer != null) {
			minaServer.stop();
			minaServer.destroy();
		}	
		/*if(eventConsumer != null) {
			eventConsumer.stop();
		}*/
		if(clientDaemon != null) {
			clientDaemon.stop();
		}
		if(chatDaemon != null) {
			chatDaemon.stop();
		}
		if(reaper != null) {
			reaper.stop();
		}
		super.stop();
	}

	@Override
	protected boolean listenForPromotions() {
		return true;
	}

	@Override
	protected void promoted() {
		try {
			setupConf();
			log.info("This node has been promoted to coordinator. Starting ClientReaper.");
			// Start a client reaper
			ClientRegistryServiceContract serviceInstance = con.getServices().getServiceInstance(ClientRegistryServiceContract.class);
			reaper = serviceInstance.getClientRegistry().createReaper();
			reaper.setClientReconnectTimeout(conf.getClientReconnectTimeout());
			reaper.start();
			
		} catch (SystemException e) {
			log.fatal("I was promoted but failed to setup the Client Reaper", e);
		}
			
	}


	/// --- PROTECTED METHODS --- ///


	/// --- PRIVATE METHODS --- ///
	
	/*
	 * This method is a major hack. It forcibly halts the 
	 * startup channel to make sure the message bus, which is updating
	 * asynchronously, is in the right state.
	 */
	private void waitForMBus() throws SystemException {
		Partition p = null;
		MBusContract serv = con.getServices().getServiceInstance(MBusContract.class);
		long next = System.currentTimeMillis() + MAX_MBUS_WAIT;
		while(next > System.currentTimeMillis()) {
			p = serv.getCurrentPartitionMap().getPartition(getId());
			if(p != null) {
				break; // LOOP BREAK
			} else {
				try {
					Thread.sleep(10);
				} catch(InterruptedException e) {
					break; // LOOP BREAK
				}
			}
		}
		if(p == null) {
			throw new SystemCoreException("Cannot contiue Client node startup; The message bus has not been updated correctly.");
		}
	}
	
	private void initLocalDosProtect() {
		DosProtector dos = con.getServices().getServiceInstance(DosProtector.class);
		List<Rule> rules = new LinkedList<Rule>();
		checkFixedRule(rules);
		checkIntervalRule(rules);
		if(rules.size() == 0) {
			log.warn("Gateway unprotected; Consider setting access frequency protection in the configuration.");
		} else {
			dos.config(GATEWAY_DOS_KEY, Lists.toArray(rules, Rule.class));
		}
	}
	

	private void checkIntervalRule(List<Rule> rules) {
		long millis = conf.getLocalPacketIntervalAccessFrequencyLength();
		int count = conf.getLocalPacketIntervalAccessFrequency();
		if(millis == -1 || count == -1) {
			log.info("Gateway ingoring interval frequency protection");
		} else {
			log.info("Gateway configured for max " + count + " local requests every " + millis + " millisecond interval");
			rules.add(new FrequencyRule(count, millis));
		}
	}

	private void checkFixedRule(List<Rule> rules) {
		long millis = conf.getLocalPacketMaxFixedAccessFrequency();
		if(millis == -1) {
			log.info("Gateway ingoring minimum frequency protection");
		} else {
			log.info("Gateway configured for max 1 local request every " + millis + " millisecond");
			rules.add(new FrequencyRule(1, millis));
		}
	}

	private void initJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=ClientNode");
	        mbs.registerMBean(this, monitorName);
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }
	
	private void destroyJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase:type=ClientNode");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to start mbean server", e);
		}
    }

	private void bindStartLevelJMX() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			CGWMonitorMBean monitor = new CGWMonitor(con);
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.gateway:type=Monitor");
			mbs.registerMBean(monitor, monitorName);
		} catch (Exception ex) {
			log.warn("Could not bind ClientGateway Node to JMX: "+ex);
		}
	}
	
	private void unbindStartLevelJMX() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.gateway:type=Monitor");
			if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch (Exception ex) {
			log.warn("Could not unbind ClientGateway Node to JMX: "+ex);
		}
	}

	/*private File setupServerConfig() throws SystemCoreException {
		try {
			File file = getTempFile();
			InputStream in = getClass().getClassLoader().getResourceAsStream(DEFAULT_SERVER_CONF);
			if(in == null) throw new FileNotFoundException("did not find default server config '" + DEFAULT_SERVER_CONF + "' in class path");
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			FileOutputStream out = new FileOutputStream(file);
			Writer write = new OutputStreamWriter(out, "UTF-8");
			String line = null;
			while((line = read.readLine()) != null) {
				line = doReplace(line) + "\r\n";
				write.write(line);
			}
			read.close();
			write.flush();
			return file;
		} catch(Exception e) {
			throw new SystemCoreException("failed to setup configuration for server", e);
		}
	}*/

	/*private String doReplace(String line) {
		SocketAddress ad = sConf.getClientBindAddress();
		line = line.replace("${address}", ad.getHost().getHostAddress());
		line = line.replace("${port}", String.valueOf(ad.getPort()));
		//line = line.replace("${bind-address}", host);
		return line;
	}*/

	/*private File getTempFile() throws IOException {
		File f = File.createTempFile("gameServer" + System.currentTimeMillis(), null, null);
		f.deleteOnExit();
		return f;
	}*/
	
	private CrossOriginConfig readCrossConfig() throws SystemException {
		// Namespace ns1 = NodeRoles.getNodeNamespace(ClusterRole.CLIENT_NODE, getId());
		Namespace ns2 = new Namespace(NodeRoles.getContextNamespace(ClusterRole.CLIENT_NODE) + ".http.cross-origin." + getId());
		ClusterConfigProviderContract contr = con.getServices().getServiceInstance(ClusterConfigProviderContract.class);
		return contr.getConfiguration(CrossOriginConfig.class, ns2);
	}

	private synchronized void setupConf() throws SystemException {
		if (conf == null) {
			ClusterConfigProviderContract contr = con.getServices().getServiceInstance(ClusterConfigProviderContract.class);
			conf = contr.getConfiguration(GatewayConfig.class, NodeRoles.getNodeNamespace(ClusterRole.CLIENT_NODE, getId()));
			log.info("Configuration found for node: " + getId() + " (" + conf.getName() + ")");
		}
		if (sConf == null) {
			ServerConfigProviderContract contr = con.getServices().getServiceInstance(ServerConfigProviderContract.class);
			sConf = contr.getConfiguration(ServerConfig.class, NodeRoles.getNodeNamespace(ClusterRole.CLIENT_NODE, getId()));
		}
	}
}