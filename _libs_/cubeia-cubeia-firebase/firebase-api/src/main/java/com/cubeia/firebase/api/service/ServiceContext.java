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
package com.cubeia.firebase.api.service;

import java.io.File;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.server.Context;
import com.cubeia.firebase.api.server.NodeListener;
import com.cubeia.firebase.api.util.ResourceLocator;

/**
 * Context for services. This context is what the server published towards
 * the services during initialization.
 * 
 * @author lars.j.nilsson
 * @see Context
 * @see Service
 */
public interface ServiceContext extends Context {
	
	/**
	 * @return The server id, never null
	 */
	public String getServerId();
	
	/**
	 * @return The configured public id of the service, never null
	 */
	public String getPublicId();
	
	/**
	 * @return The configured name of the service, or null if not configured
	 */
	public String getName();
	
	/**
	 * @return True if the service reside on a singleton server, false otherwise
	 */
	public boolean isSingletonServer();
	
	
	/**
	 * @return The current server home directory, never null
	 */
	public File getServerHomeDirectory();
	
	
	/**
	 * @return The specified server configuration directory, never null
	 */
	public File getServerConfigDirectory();
	
	
	/**
	 * @return The specified server log directory, never null
	 */
	public File getServerLogDirectory();
	
	
	/**
	 * This method provides access to the registry to which the service belongs. It
	 * can be noted that services should not use each other during the initialization phase. If
	 * they do depend on each other for startup they must declare so in their descriptors, 
	 * please refer to the developers manual for more information.
	 * 
	 * @return The registry to which the service belongs, never null
	 */
	public ServiceRegistry getParentRegistry();
	
	
	/**
	 * @return The mbean server used by the server, never null
 	 */
	public MBeanServer getMBeanServer();


	/**
	 * @return The game deployment directory, never null
	 */
	public File getServerGameDirectory();
	
	
	/**
	 * The resource locator is for reading resources from the
	 * game archive. This can be used to load configuration etc.
	 * 
	 * @return A resource locator for the game archive, never null
	 */
	public ResourceLocator getResourceLocator();

	
	/**
	 * Get the class loader used by the server as a parent for specific
	 * deployments. In other words, this class loader will be the parent 
	 * to all deployed game, service and tournament class loaders.
	 * 
	 * @return The class loader used as a parent for deployment loaders, never null
	 */
	public ClassLoader getDeploymentClassLoader();
	
	
	/**
	 * This method can be called to register the service as a node listener. A
	 * null argument removed the listener.
	 * 
	 * @param listener Listener to use, or null to remove
	 */
	public void setNodeListener(NodeListener listener);

}
