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
package com.cubeia.firebase.server.master;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cubeia.firebase.api.command.CommandMessage;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.commands.Config;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.CommandListener;
import com.cubeia.firebase.service.conn.CommandReceiver;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;

public class MasterCommProxy implements Initializable<MasterContext> {

	// private ServerConfig clusterConfig;
	private ClusterConnection cluster;
	private MasterContext con;
	private final AtomicBoolean hasConfiguration;
	// private final MapConfiguration config;
	// private final Logger log = Logger.getLogger(getClass());
	// private ConfigurationAdapter adapter;
	// private final String id;
	
	public MasterCommProxy(String id) { 
		hasConfiguration = new AtomicBoolean();
		// config = new MapConfiguration();
		// this.id = id;
	}
	
	public ClusterConnection getClusterConnection() {
		return cluster;
	}
	
	public void destroy() {
		destroyComm();
		con = null;
	}
	
	public void init(MasterContext con) throws SystemCoreException {
		// clusterConfig = con.getClusterConfig();
		this.con = con;
		initComm();
	}
	
	public boolean isPrimaryMaster(ClusterParticipant[] nodes) {
		ClusterParticipant[] arr = (nodes == null ? null : nodes);
		Set<SocketAddress> masters = extractMasters(arr);
		if(masters.size() == 0) return true; // SANITY CHECK
		else if(cluster == null) throw new IllegalStateException("not initialized"); // TODO Check if this is the correct behaviour, or if we should ignore it
		else {
			/*
			 * I'm a primary master if 1) I am the first master appearing
			 * in the network order; or 2) there's no other master
			 */
			for(SocketAddress node : cluster.getMembersInNetworkOrder()) {
				if(node.equals(cluster.getLocalAddress())) return true; // I'm first in priority order
				else if(masters.contains(node)) return false; // Someone else is the primary
			}
			return true;
		}
	}

	public SocketAddress getNodeAddress() {
		return cluster.getLocalAddress();
	}
	
	public boolean hasConfiguration() {
		return hasConfiguration.get();
	}
	
	/*public synchronized <T extends Configurable> boolean hasConfiguration(Class<T> iface) {
		if(iface == null) return (config != null && config.size() > 0);
		else {
			try {
				return getConfiguration(iface) != null;
			} catch (ConfigurationException e) {
				log.warn("failed to get configuration", e);
				return false;
			}
		}
	}
	
	public synchronized <T extends Configurable> T getConfiguration(Class<T> iface) throws ConfigurationException {
		if(config == null || config.size() == 0) return null; // SANITY CHECK
		if(adapter == null) adapter = new ConfigurationAdapter(config);
		return adapter.implement(iface, NodeRoles.getNodeNamespace(ClusterRole.MASTER_NODE, id));
	}
	
	public synchronized <T extends Configurable> T getSubConfiguration(Class<T> iface, Namespace ns) throws ConfigurationException {
		Arguments.notNull(ns, "namespace");
		if(config == null || config.size() == 0) return null; // SANITY CHECK
		if(adapter == null) adapter = new ConfigurationAdapter(config);
		String str = ns.toString();
		Namespace root = NodeRoles.getNodeNamespace(ClusterRole.MASTER_NODE, id);
		Namespace next = (str.length() == 0 ? root : new Namespace(root.toString() + "." + str));
		return adapter.implement(iface, next);
	}*/
	
	
	/// --- PRIVATE METHODS --- //
	
	/*private ConfigDeltaListener getConfigDeltaListener() {
		return new ConfigDeltaListenerImpl(config);
	}*/
	
	private Set<SocketAddress> extractMasters(ClusterParticipant[] nodes) {
		Set<SocketAddress> set = new HashSet<SocketAddress>();
		if(nodes == null || nodes.length == 0) return set; // SANITY CHECK
		for (ClusterParticipant node : nodes) {
			if(node.isMaster()) {
				set.add(node.getServerId().address);
			}
		}
		return set;
	}
	
	private void initComm() throws SystemCoreException { 
		ConnectionServiceContract serv = con.getServices().getServiceInstance(ConnectionServiceContract.class);
		//try {
			cluster = serv.getSharedConnection();
			CommandReceiver rec = cluster.getCommandReceiver();
			rec.addCommandListener(new ListenerImpl());
		/*} catch(ClusterException e) {
			throw new SystemCoreException("Master communication proxy failed to open connection to cluster; Received message: " + e.getMessage(), e);
		}*/
	}
	
	private void destroyComm() { 
		//ConnectionServiceContract serv = con.getServices().getServiceInstance(ConnectionServiceContract.class);
		//serv.closeConnection(cluster);
	}
	
	
	/// --- INNER CLASSES --- ///
	
	private class ListenerImpl implements CommandListener {
		
		public Object commandReceived(CommandMessage c) {
			if(c.command instanceof Config) {
				hasConfiguration.set(true);
			} 
			return null;
		}
	}
}
