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
package com.cubeia.firebase.service.config.cluster;
 
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.commands.Config;
import com.cubeia.firebase.server.commands.ConfigRequest;
import com.cubeia.firebase.server.conf.Configuration;
import com.cubeia.firebase.server.conf.ConfigurationAdapter;
import com.cubeia.firebase.server.conf.MapConfiguration;
import com.cubeia.firebase.server.gateway.comm.ConfigDeltaListenerImpl;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.server.master.RealConfig;
import com.cubeia.firebase.server.statistics.StatisticsLevel;
import com.cubeia.firebase.server.statistics.StatisticsLevelConfig;
import com.cubeia.firebase.server.util.CommandUtils;
import com.cubeia.firebase.service.config.ConfigurationDetails;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;

/**
 * This is the cluster config provider service. It has the following pre-requisite:
 * if the service is the first member on the cluster communication channel, ie. if no
 * other servers have been started in the cluster, it *must* be able to find a "cluster.props" file,
 * either in the config directory or in the class path. It also depends on the comm channel service.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 29
 * @see ClusterConfigProviderContract
 */

/*
 * 1) Open cluster connection
 * 2) If alone: read config from sys
 *    Else: Send config request and wait
 */
public class ConfigProviderService implements Service, ClusterConfigProviderContract {
	
	private static final long CONFIG_MAX_WAIT = 1200000;
	private static final String CLUSTER_CONF = "cluster.props";
	
	
	// --- INSTANCE MEMBERS --- //

	private final Object configLock = new Object();
    private final Logger log = Logger.getLogger(getClass());

	private File configDir;
	private ConnectionServiceContract connServ;
	private ClusterConnection conn;

	private boolean haveLocalConf = false;
	private Configuration clusterConfig;
	private ConfigurationAdapter config;
	private ServiceContext con;
	// private String myId;

	public void destroy() {
		destroyJmx();
		if(connServ != null) {
			// Do not destroy shared connection
			// connServ.closeConnection(conn);
		}
		connServ = null;
	}

	public void init(ServiceContext con) throws SystemException {
		this.con = con;
		// myId = "config:" + con.getServerId();
		configDir = con.getServerConfigDirectory();
		// get hold of the shared comm connection
		setupClusterConnection(con);
		// start listening
		setupListener();
		if(alone()) {
			// we're the first, read config
			readConfig();
		} else {
			// the coordinator will send config, wait
			waitForConfig();
		}
		// add JMX beans
		initJmx();
		// set statistics level according to config
		initStatisticsLevel();
	}

	

	public void start() { }

	public void stop() { }
	
	public ConfigProperty[] getAllProperties() {
		return cloneProperties();
	}
	
	public <T extends Configurable> T getConfiguration(Class<T> cl, Namespace ns) {
		return config.implement(cl, ns);
	}

	
	// --- PRIVATE METHODS --- //
	
	private void initJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.service:type=ClusterConfigProvider");
	        mbs.registerMBean(new ConfigurationDetails(this), monitorName);
		} catch(Exception e) {
			log.error("failed to start mbean", e);
		}
    }
	
	private void destroyJmx() {
		try {
			MBeanServer mbs = con.getMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.service:type=ClusterConfigProvider");
	        if(mbs.isRegistered(monitorName)) {
	        	mbs.unregisterMBean(monitorName);
	        }
		} catch(Exception e) {
			log.error("failed to stop mbean", e);
		}
	}
	
	// Attempt to establish a new connection with the cluster
	private void setupClusterConnection(ServiceContext con) throws SystemCoreException {
		connServ = con.getParentRegistry().getServiceInstance(ConnectionServiceContract.class);
		if(connServ == null) throw new SystemCoreException("Failed service dependencies; Could not find server connection service '" + Constants.CONNECTION_SERVICE_NS + "'.");
		//try {
			conn = connServ.getSharedConnection();
		/*} catch (ClusterException e) {
			throw new SystemCoreException("Failed to open new cluster connection; Received message: " + e.getMessage(), e);
		}*/
	}
	
	// Count members in cluster
	private boolean alone() {
		return conn.countMembers() <= 1;
	}

	// Read configuraiton file
	private void readConfig() throws SystemCoreException {
		// FIXME: Better exception messages here please
		File tmp = new File(configDir, CLUSTER_CONF);
		if(!tmp.exists()) throw new SystemCoreException("Cluster config provider cannot find file '" + CLUSTER_CONF + "' in config directory '" + configDir + "'; Aborting.");
		try {
			clusterConfig = new RealConfig("cluster", tmp);
			config = new ConfigurationAdapter(clusterConfig);
			haveLocalConf = true;
		} catch(IOException e) {
			throw new SystemCoreException("Cluster config provider failed to read file '" + tmp + "' due to an IO error; Aborting.", e);
		}
	}

	// Send config request and wait for answer
	private void waitForConfig() throws SystemCoreException {
		sendConfigRequest();
		boolean done = false;
		try {
			doWaitForConfig();
			done = true;
		} finally {
			if(!done) {
				//connServ.closeConnection(conn);
			}
		}
	}
	
	private void sendConfigRequest() {
		try {
			conn.getCommandDispatcher().dispatch(new ConfigRequest());
		} catch (ClusterException e) {
			log.error("Cluster config provider failed to send configuration request", e);
		}
	}
	
    private void setupListener() {
		conn.getCommandReceiver().addCommandListener(new CommandListener() {
			// process commands
			public Object commandReceived(CommandMessage c) {				
				if(c.command instanceof Config && !haveLocalConf) {
					/*
					 * We do not have a local config, which means that this command
					 * should be processed here. First check if we need to create a new 
					 * configuration object, then forward the command to a delta listener
					 * helt util.
					 */
					synchronized(configLock) {
						if(config == null) {
							clusterConfig = new MapConfiguration();
							config = new ConfigurationAdapter(clusterConfig);
						}
						CommandUtils.forward((Config)c.command, new ConfigDeltaListenerImpl((MapConfiguration)clusterConfig));
					}
				} else if(c.command instanceof ConfigRequest) {
					/*
					 * At this point, we should answer only if we're the current "master" 
					 * of the comm channel, ie. if we do have a config and we're first in
					 * the comm channel network order.
					 */
					if(isMaster()) {
						if(haveConfiguration()) {
							sendConfigReply((ConfigRequest)c.command, c.sender);
						}
						else {
							String msg = "Cluster config provider is first in network order but does not have an instantiated confiuration to send!";
							SystemLogger.error(msg);
							log.error(msg);
						}
					}
				}
				return null;
			}
		});
	}
    
    private void sendConfigReply(ConfigRequest req, SocketAddress sender) {
		Config con = new Config(Config.Type.DELTA_INIT);
		con.setAttachment(cloneProperties());
		try {
			conn.getCommandDispatcher().dispatch(con, sender);
		} catch (ClusterException e) {
			String msg = "Failed to respond to configuration request from sender '" + sender + "'; Recevied message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		}
	}
    
    // Check if we're "master" over the cluster config
	private boolean isMaster() {
		SocketAddress local = conn.getLocalAddress();
		SocketAddress[] order = conn.getMembersInNetworkOrder();
		return (order.length > 0 && order[0].equals(local));
	}
    
	private ConfigProperty[] cloneProperties() {
		int count = 0;
		Map<PropertyKey, String> map = clusterConfig.cloneProperties();
		ConfigProperty[] arr = new ConfigProperty[map.size()];
		for (Entry<PropertyKey, String> e : map.entrySet()) {
			arr[count++] = new ConfigProperty(e.getKey(), e.getValue());
		}
		return arr;
	}

	private void doWaitForConfig() throws SystemCoreException {
		long next = System.currentTimeMillis() + CONFIG_MAX_WAIT;
		while(!haveConfiguration() && next > System.currentTimeMillis()) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) { break; }
		}
		if(!haveConfiguration()) {
			throw new SystemCoreException("Missing cluster configuration; Cluster configuration provider service failed to respond within " + CONFIG_MAX_WAIT + " milliseconds; Network members: " + toString(conn.getMembersInNetworkOrder()));	
		}
	}

	private String toString(SocketAddress[] membs) {
		StringBuilder b = new StringBuilder("{ ");
		for (int i = 0; i < membs.length; i++) {
			b.append(membs[i].toString());
			if(i + 1 < membs.length) {
				b.append(", ");
			}
		}
		return b.append(" }").toString();
	}

	private boolean haveConfiguration() {
		synchronized(configLock) {
			return config != null;
		}
	}
	
	private void initStatisticsLevel() {
		try {
			StatisticsLevelConfig configuration = getConfiguration(StatisticsLevelConfig.class, null);
			StatisticsLevel.getInstance().setLevel(configuration.getLevel());
		} catch (Exception e) {
			log.warn("Could not read statistics level configuration:"+e, e);
		}
	}
}
