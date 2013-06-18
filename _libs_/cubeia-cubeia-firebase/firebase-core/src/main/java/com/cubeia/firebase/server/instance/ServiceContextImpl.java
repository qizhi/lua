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

import com.cubeia.firebase.api.server.NodeListener;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.ResourceLocator;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.service.ServiceArchive;
import com.cubeia.firebase.server.service.ServiceResourceLocator;

class ServiceContextImpl implements ServiceContext {
	
	private final File home;
	private final ServiceRegistry parent;
	private final File configDir;
	private final MBeanServer serv;
	private final File gameDir;
	private final boolean isSingleton;
	private final ResourceLocator locator;
	private final File logDir;
	private final ServiceArchive arch;
	private final ClassLoader depLoader;
	private final String id;
	private final NodeListenerHandler listeners;
	
	ServiceContextImpl(String id, MBeanServer serv, File configDir, File gameDir, File logDir, ServiceRegistry parent, NodeListenerHandler listeners, boolean isSingleton, ServiceArchive arch, ClassLoader depLoader) {
		this.id = id;
		this.logDir = logDir;
		this.listeners = listeners;
		this.isSingleton = isSingleton;
		this.arch = arch;
		this.depLoader = depLoader;
		locator = new ServiceResourceLocator(arch);
		home = new File(Constants.FIREBASE_HOME);
		this.gameDir = gameDir;
		this.serv = serv;
		this.configDir = configDir;
		this.parent = parent;
	}
	
	public void setNodeListener(NodeListener listener) {
		if(listener != null) {
			listeners.addNodeListener(id, listener);
		} else {
			listeners.removeNodeListener(id);
		}
	}
	
	public String getServerId() {
		return id;
	}
	
	public String getName() {
		return arch.getServiceInfo().getName();
	}
	
	public ClassLoader getDeploymentClassLoader() {
		return depLoader;
	}
	
	public String getPublicId() {
		return arch.getPublicId();
	}
	
	public File getServerLogDirectory() {
		return logDir;
	}
	
	public ResourceLocator getResourceLocator() {
		return locator;
	}
	
	public boolean isSingletonServer() {
		return isSingleton;
	}
	
	public File getServerGameDirectory() {
		return gameDir;
	}
	
	public MBeanServer getMBeanServer() {
		return serv;
	}
	
	public File getServerHomeDirectory() {
		return home;
	}
	
	public File getServerConfigDirectory() {
		return configDir;
	}
	
	public ServiceRegistry getParentRegistry() {
		return parent;
	}
}