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
package com.cubeia.firebase.server.node;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.master.ServerId;

public class NodeContextAdapter implements NodeContext {

	private final NodeContext parent;
	
	public NodeContextAdapter(NodeContext parent) {
		Arguments.notNull(parent, "parent");
		this.parent = parent;
	}
	
	public ClassLoader getDeploymentClassLoader() {
		return parent.getDeploymentClassLoader();
	}

	public MBeanServer getMBeanServer() {
		return parent.getMBeanServer();
	}
	
	public void shutdown(String msg, boolean emergency) {
		parent.shutdown(msg, emergency);
	}
	
	public ServiceRegistry getServices() {
		return parent.getServices();
	}
	
	public ServerId getServerId() {
		return parent.getServerId();
	}
}
