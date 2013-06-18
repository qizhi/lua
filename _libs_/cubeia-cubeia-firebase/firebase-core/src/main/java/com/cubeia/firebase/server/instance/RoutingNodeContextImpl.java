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
/**
 * 
 */
package com.cubeia.firebase.server.instance;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.manager.ManagerContext;
import com.cubeia.firebase.server.master.ServerId;
import com.cubeia.firebase.server.node.Node;
import com.cubeia.firebase.server.node.NodeContext;

abstract class RoutingNodeContextImpl<N extends Node<?>> implements ManagerContext, NodeContext, Initializable<NodeContext>, HaltingMember {
	
	// private final N node;
	// private final ServerConfig config;
	// protected final MasterProxyImpl<N> proxy;
	// private final ClusterRole role;
	protected final ServerInstance inst;
	
	//private final Logger log = Logger.getLogger(getClass());
	//private final boolean doRouting;
	
	RoutingNodeContextImpl(ClusterRole role, N node, ServerInstance inst/*, boolean doRouting*/) {
		//this.role = role;
		//this.node = node;
		this.inst = inst;
		//this.doRouting = doRouting;
		//proxy = new MasterProxyImpl<N>(node.getId(), role, this);
		/* try {
			config = inst.getServerConfiguration(NodeRoles.getContextNamespace(role));
		} catch(Exception e) {
			// FIXME: Illegal state? Fix exception properly...
			throw new IllegalStateException("failed to implement node config", e);
		} */
	}
	
	public ClassLoader getDeploymentClassLoader() {
		return inst.getDeploymentClassLoader();
	}
	
	public ServerId getServerId() {
		return inst.getServerId();
	}
	
	public void halt() { }
	
	public void resume() { }
	
	public ServiceRegistry getServices() {
		return inst.getServiceRegistry();
	}
	
	public void destroy() {
		// proxy.destroy();
	}
	
	public void init(NodeContext con) throws SystemException {
		/*proxy.init(new MasterProxyContext<N>() {
			
			// private int commBase = commPortCount.incrementAndGet();
			
			public ServerConfig getClusterConfig() {
				return config;
			}
			
			public boolean useSharedClusterConnection() {
				return (role.equals(ClusterRole.MANAGER_NODE));
			}
			
			public ServiceRegistry getServices() {
				return inst.getServiceRegistry();
			}
			
			public MBeanServer getMBeanServer() {
				return inst.getMBeanServer();
			}
			
			public N getNode() {
				return node;
			}
			
			public void shutdown(String msg, boolean emergency) {
				inst.shutdown(msg, emergency);
			}
		});*/
	}
	
	public void shutdown(String msg, boolean emergency) {
		inst.shutdown(msg, emergency);
	}

	/*public ClusterConnection getCluster() {
		return proxy.getCluster();
	}
	
	public MasterProxy getMasterProxy() {
		return proxy;
	}*/
	
	public MBeanServer getMBeanServer() {
		return inst.getMBeanServer();
	}
}