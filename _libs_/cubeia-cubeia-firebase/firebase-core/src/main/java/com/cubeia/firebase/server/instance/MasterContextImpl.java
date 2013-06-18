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

import java.io.File;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.master.MasterContext;
import com.cubeia.firebase.server.master.MasterNode;
import com.cubeia.firebase.server.master.ServerId;
import com.cubeia.firebase.server.node.NodeContext;
import com.cubeia.firebase.server.routing.MasterNodeRouter;
import com.cubeia.firebase.server.routing.impl.MasterRouterImpl;
import com.cubeia.firebase.server.routing.impl.RouterContext;
import com.cubeia.firebase.service.messagebus.MBusContract;

class MasterContextImpl implements MasterContext, Initializable<NodeContext>, HaltingMember {
	
	// private final MasterNode node;
	private final ServerConfig config;
	// protected final MasterCommProxy proxy;
	private final ServerInstance inst;
	private final MasterRouterImpl router;
	
	MasterContextImpl(MasterNode node, ServerInstance inst) {
		//this.node = node;
		this.inst = inst;
		router = new MasterRouterImpl(node.getId(), "Maste Node " + node.getId());
		//proxy = new MasterCommProxy(node.getId());
		config = inst.getServerConfiguration(new Namespace("master"));
	}
	
	public ClassLoader getDeploymentClassLoader() {
		return inst.getDeploymentClassLoader();
	}
	
	public void halt() {
		router.halt();
	}
	
	public void resume() {
		router.resume();
	}
	
	public MasterNodeRouter getNodeRouter() {
		return router;
	}
	
	/*public boolean isPrimary(ClusterNode[] nodes) {
		return proxy.isPrimaryMaster(nodes);
	}*/
	
	public ServiceRegistry getServices() {
		return inst.getServiceRegistry();
	}

	public ServerConfig getClusterConfig() {
		return config;
	}
	
	public ServerId getServerId() {
		return inst.getServerId();
	}
	
	public void destroy() {
		router.destroy();
		// proxy.destroy();
	}
	
	/*public boolean isSingleton() {
		return inst.getIsSingleton();
	}*/
	
	public void init(final NodeContext con) throws SystemException {
		router.init(new RouterContext() {
			
			public ServiceRegistry getServices() {
				return con.getServices();
			}
			
			public String getServerId() {
				return con.getServerId().id;
			}
			
			public MBusContract getMessageBus() {
				return inst.getServiceRegistry().getServiceInstance(MBusContract.class);
			}
		
			public MBeanServer getMBeanServer() {
				return inst.getMBeanServer();
			}
		});
		
		
		/*proxy.init(new MasterProxyContext<MasterNode>() {
			
			// private int commBase = commPortCount.incrementAndGet();
			
			public int getClusterCommPort() {
				return ServerInstance.this.getClusterCommPort(NodeRoles.getRoleNamespace(NodeRole.MASTER, node.getId()), node.getId());
			}

			public ServerConfig getClusterConfig() {
				return config;
			}
			
			public ServiceRegistry getServices() {
				return reg;
			}
			
			public ClusterConnection getCluster() {
				return proxy.getClusterConnection();
			}
			
			public MBeanServer getMBeanServer() {
				return ServerInstance.this.getMBeanServer();
			}
			
			public MasterNode getNode() {
				return node;
			}
			
			public void shutdown(String msg, boolean emergency) {
				ServerInstance.this.shutdown(msg, emergency);
			}
		});*/
	}
	
	public MBeanServer getMBeanServer() {
		return inst.getMBeanServer();
	}
	
	public File getConfigDirectory() {
		return inst.getConfigDirectory();
	}
	
	/*public ClusterConnection getCluster() {
		return proxy.getClusterConnection();
	}*/
	
	/*public MasterProxy getMasterProxy() {
		return proxy;
	}*/
	
	public void shutdown(String msg, boolean emergency) {
		inst.shutdown(msg, emergency);
	}
}