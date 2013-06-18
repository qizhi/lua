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
package com.cubeia.firebase.service.deploy.tournament.impl;

import com.cubeia.firebase.api.server.NodeInfo;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.server.deployment.Deployment;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.DeploymentHandlerService;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

public class ServiceImpl implements Service, DeploymentHandlerService {
	
	private TournamentDeploymentHandler handler;

	public void init(ServiceContext con) throws SystemException { 
		this.handler = new TournamentDeploymentHandler(con.getParentRegistry());
	}

	public void start() { }
	
	@Override
	public DeploymentType getDeploymentType() {
		return DeploymentType.TOURNAMENT_ARCHIVE;
	}
	
	@Override
	public Deployment handle(DeploymentResource resource, ClassLoader parentLoader) throws DeploymentFailedException {
		return handler.handle(resource, parentLoader);
	}
	
	@Override
	public void registerNodeDown(NodeInfo info) {
		handler.registerNodeDown(info);
	}
	
	@Override
	public void registerNodeUp(NodeInfo info) {
		handler.registerNodeUp(info);
	}

	public void stop() {}
	
	public void destroy() {}

}