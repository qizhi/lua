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
package com.game.server.service.depman;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.server.deployment.DeploymentManager;
import com.cubeia.firebase.server.deployment.ResourceManager;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.service.depman.DeploymentServiceContract;

public class DeploymentService implements Service, DeploymentServiceContract {
	
	// --- INSTANCE MEMBERS --- //

	private DeploymentManager man;
	private ServiceContext context;
	
	public void destroy() {
		context.setNodeListener(null);
		man = null;
	}

	public void init(ServiceContext con) throws SystemException {
		this.context = con;
		ResourceManager resources = InternalComponentAccess.getDeploymentResources();
		man = new DeploymentManager(resources, con);
		con.setNodeListener(man);
		man.start();
	}
	
	public void start() {
		
	}
	
	public DeploymentManager getDeploymentManager() {
		return man;
	}
	
	public void stop() {
		man.stop();
	}
}