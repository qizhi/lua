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
package com.cubeia.firebase.server.deployment.activation;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.NodeInfo;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.server.deployment.Deployment;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.DeploymentHandler;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.activation.ActivationConfigManager;
import com.cubeia.firebase.service.activation.ActivationType;

public abstract class ActivationConfigDeploymentHandler implements DeploymentHandler {

	private final Logger log = Logger.getLogger(getClass());	
	private final ActivationConfigManager manager;	

	public ActivationConfigDeploymentHandler(ServiceRegistry registry) throws SystemCoreException {
		this.manager = registry.getServiceInstance(ActivationConfigManager.class);
	}
		
	public void setDeploymentClassLoader(ClassLoader parent) { }
	
	public void registerNodeDown(NodeInfo info) { }
	
	public void registerNodeUp(NodeInfo info) { }
	
	// public abstract String getFilter();
	
	public abstract String getTail();
	
	public abstract ActivationType getType();

	public Deployment handle(DeploymentResource resource, ClassLoader parentLoader) throws DeploymentFailedException {
		log.info("Deploying activation config '" + resource + "'");
		String name = resource.getName().substring(0, resource.getName().length() - getTail().length());
		ActivationConfigDeployment d = new ActivationConfigDeployment(name, getType());
		try {
			d.configure(resource);
		} catch(IOException e) {
			throw new DeploymentFailedException("Deployement of activator config for game '" + name + "' failed; Received message: " + e.getMessage(), e);
		}
		ConfigSource configSource = d.getConfigSource();
		manager.registerConfigSource(configSource, getType());
		return d;
	}

	/*public void fileChanged(String fileName) {
		try {
			handle(new File(fileName));
		} catch (DeploymentFailedException e) {
			log.error("The redeployment of resource '" + fileName + "' failed. Reported error: "+e, e);
		}
	}*/
}
