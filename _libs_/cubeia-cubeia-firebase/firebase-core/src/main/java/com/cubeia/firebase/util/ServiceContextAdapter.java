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
package com.cubeia.firebase.util;

import java.io.File;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.server.NodeListener;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.ResourceLocator;

//FIXME: Trac issue #417
public class ServiceContextAdapter implements ServiceContext {

	public ClassLoader getDeploymentClassLoader() {
		return null;
	}

	public MBeanServer getMBeanServer() {
		return null;
	}

	public String getName() {
		return null;
	}

	public ServiceRegistry getParentRegistry() {
		return null;
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
