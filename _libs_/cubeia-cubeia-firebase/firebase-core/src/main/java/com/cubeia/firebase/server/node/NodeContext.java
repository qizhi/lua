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

import com.cubeia.firebase.api.server.Context;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.master.ServerId;

public interface NodeContext extends Context {
	
	public ServiceRegistry getServices();

	public MBeanServer getMBeanServer();
	
	// public MasterProxy getMasterProxy();
	
	// public ClusterConnection getControlConnection();
	
	public ServerId getServerId();
	
	public ClassLoader getDeploymentClassLoader();
	
	/**
	 * This method can be used by sub-contexts to attempt to shutdown
	 * the node. This can for example be used if the master shuns
	 * a node on startup.
	 * 
	 * @param msg Optional message, may be null
	 * @param emergency True if this is an emergency shutdown due to a fatal error
	 */
	
	// TODO: Remove this from the generic node context?
	public void shutdown(String msg, boolean emergency);
	
}
