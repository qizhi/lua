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
package com.cubeia.firebase.server.deployment.ua;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.server.deployment.Deployment;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.DeploymentHandlerAdapter;
import com.cubeia.firebase.server.deployment.UnifiedDeploymentCallback;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

public class UnifiedArchiveDeploymentHandler extends DeploymentHandlerAdapter {

	private final Map<String, UnifiedArchiveDeployment> deps;
	// private final ClassLoader parentLoader;
	private final UnifiedDeploymentCallback deploymentManager;
	
	public UnifiedArchiveDeploymentHandler(/*ClassLoader parentLoader, */UnifiedDeploymentCallback deploymentManager) {
		this.deploymentManager = deploymentManager;
		this.deps = new ConcurrentHashMap<String, UnifiedArchiveDeployment>();
		// this.parentLoader = parentLoader;
	}
	
	public Deployment handle(DeploymentResource res, ClassLoader parentLoader) throws DeploymentFailedException {
		String name = res.getName().substring(0, res.getName().lastIndexOf('.'));
		Deployment dep = doHandle(res, name, parentLoader);
		return dep;
	}
	
	
	// --- PRIVATE METHODS --- //

	private Deployment doHandle(DeploymentResource res, String name, ClassLoader parentLoader) {
		if(deps.containsKey(name)) {
			return handleRedeploy(res, name, parentLoader);
		} else {
			return handleNewDeploy(res, name, parentLoader);
		}
	}

	private Deployment handleRedeploy(DeploymentResource res, String name, ClassLoader parentLoader) {
		Logger.getLogger(getClass()).info("Re-deploying Unified Archive: " + name);
		UnifiedArchiveDeployment dep = deps.get(name);
		dep.newRevision(res);
		return dep;
	}

	private Deployment handleNewDeploy(DeploymentResource res, String name, ClassLoader parentLoader) {
		UnifiedArchiveDeployment dep = new UnifiedArchiveDeployment(name, parentLoader, deploymentManager);
		deps.put(dep.getName(), dep);
		dep.firstRevision(res);
		return dep;
	}
}
