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
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.server.NodeListener;
import com.cubeia.firebase.api.util.ResourceLocator;

/**
 * <b>NB: </b> This class is within the public API because of build reasons,
 * it should only be used for testing! It will be moved shortly. See Trac issue
 * #417.
 * 
 * <p>This adapter has the following attributes:
 * 
 * <ul>
 *  <li>It uses the platform mbean server
 *  <li>It has a "set-able" service registry
 * </ul>
 * 
 * @author Larsan
 */
//FIXME: Move to test, if you can get Maven to support it, see Trac issue #417
public class ServiceContextAdapter implements ServiceContext {

	protected ServiceRegistry reg;

	public ClassLoader getDeploymentClassLoader() {
		return null;
	}

	public MBeanServer getMBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}

	public String getName() {
		return null;
	}

	public ServiceRegistry getParentRegistry() {
		return reg;
	}
	
	public void setParentRegistry(ServiceRegistry reg) {
		this.reg = reg;
	}

	public String getPublicId() {
		return null;
	}

	public ResourceLocator getResourceLocator() {
		return null;
	}

	public File getServerConfigDirectory() {
		return null;
	}

	public File getServerGameDirectory() {
		return null;
	}

	public File getServerHomeDirectory() {
		return null;
	}

	public String getServerId() {
		return null;
	}

	public File getServerLogDirectory() {
		return null;
	}

	public boolean isSingletonServer() {
		return false;
	}

	public void setNodeListener(NodeListener listener) { }

}
