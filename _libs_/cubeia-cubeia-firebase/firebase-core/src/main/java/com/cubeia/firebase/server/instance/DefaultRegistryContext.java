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
package com.cubeia.firebase.server.instance;

import java.io.File;

import javax.management.MBeanServer;

import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.server.service.DefaultServiceRegistryContext;
import com.cubeia.firebase.server.service.ServiceArchive;
import com.game.server.bootstrap.SharedClassLoader;

/**
 * Trivial service context which relies entirely on the server
 * instance class for its fulfillment.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 29
 */
class DefaultRegistryContext implements DefaultServiceRegistryContext {

	private final ServerInstance inst;
	private final NodeListenerHandler listeners;
	
	DefaultRegistryContext(ServerInstance inst, NodeListenerHandler listeners) {
		this.listeners = listeners;
		this.inst = inst;
	}
	
	public ServiceContext newServiceContext(ServiceArchive archive) {
		return new ServiceContextImpl(inst.getServerStringId(), inst.getMBeanServer(), inst.getConfigDirectory(), inst.getGameDirectory(), inst.getLogDirectory(), inst.getServiceRegistry(), listeners, inst.isSingleton(), archive, inst.getDeploymentClassLoader());
	}
	
	public String getServerId() {
		return inst.getServerStringId();
	}
	
	public MBeanServer getMBeanServer() {
		return inst.getMBeanServer();
	}
	
	public ClassLoader getDeploymentClassLoader() {
		return inst.getDeploymentClassLoader();
	}
		
	public File[] getTrustedSarLocations() {
		return inst.getTrustedSarLocations();
	}
		
	public File[] getIsolatedSarLocations() {
		return new File[] { new File(inst.getLibDirectory(), "services/") };
	}
	
	public File[] getDeployedSarLocations() {
		return new File[] { new File(inst.getGameDirectory(), "deploy/") };
	}
		
	public SharedClassLoader getSharedSpace() {
		return inst.getSharedClassLoader();
	}
		
	public File getWorkDirectory() {
		return inst.getWorkDirectory();
	}
}
