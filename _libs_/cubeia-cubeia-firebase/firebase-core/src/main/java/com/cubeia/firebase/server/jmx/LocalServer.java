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
package com.cubeia.firebase.server.jmx;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.server.game.GameNode;
import com.cubeia.firebase.server.gateway.GatewayNode;
import com.cubeia.firebase.server.instance.ServerInstance;
import com.cubeia.firebase.server.manager.ManagerNode;
import com.cubeia.firebase.server.node.Node;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;

public class LocalServer implements LocalServerMBean {

	private final ServerInstance instance;

	public LocalServer(ServerInstance instance) {
		Arguments.notNull(instance, "instance");
		this.instance = instance;
	}
	
	@Override
	public String getServerStatus() {
		return instance.getServerStatus();
	}

	public void addNode(String nodeType, final String id) {
		final ClusterRole role = parseNodeType(nodeType);
		if(role != null) {
			try {
				Classes.switchContextClassLoaderForInvocation(new InvocationFacade<Exception>() {
					public Object invoke() throws Exception {
						instance.addNode(id, role);
						return null;
					}	
				}, ServerInstance.class.getClassLoader());
			} catch (Exception e) {
				// TODO Exception
				e.printStackTrace();
			}
		}
	} 
	
	public void removeNode(final String id) {
		try {
			Classes.switchContextClassLoaderForInvocation(new InvocationFacade<Exception>() {
				public Object invoke() throws Exception {
					instance.removeNode(id);
					return null;
				}
			}, ServerInstance.class.getClassLoader());
		} catch (Exception e) {
			// TODO Exception
			e.printStackTrace();
		}
	}
	
	public String[] getLiveNodes() {
		Node<?>[] nodes = instance.getLiveNodes();
		String[] arr = new String[nodes.length];
		for(int i = 0; i < arr.length; i++) {
			String s = "master";
			Node<?> node = nodes[i];
			if(node instanceof GatewayNode) s = "client";
			else if(node instanceof GameNode) s = "game";
			else if(node instanceof ManagerNode) s = "manager";
			arr[i] = s + ":" + node.getId();
		}
		return arr;
	}
	
	
	/// --- PRIVATE METHODS --- ///
	
    private ClusterRole parseNodeType(String nodeType) {
		return ClusterRole.parse(nodeType);
	}
}
