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
package com.cubeia.firebase.server.mtt.processor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.mtt.MTTLogic;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.deployment.mtt.TournamentDeployment;
import com.cubeia.firebase.server.deployment.mtt.TournamentRevision;
import com.cubeia.firebase.server.service.depman.DeploymentServiceContract;

public class MttRegistry {
	
	private static transient Logger log = Logger.getLogger(MttRegistry.class);
	
	private final ServiceRegistry services;
	
	/**
	 * Cache all created instances.
	 */
	private final ConcurrentMap<Integer, MTTLogic> instances = new ConcurrentHashMap<Integer, MTTLogic>();

	public MttRegistry(ServiceRegistry services) {
		this.services = services;
	}
	
	/**
	 * Get cached instance if found, otherwise a new instance is created and
	 * returned.
	 * 
	 * @param mttDefId
	 * @return
	 */
	public MTTLogic getMttInstance(int mttDefId) {
		synchronized (String.valueOf(mttDefId).intern()) {
			if (!instances.containsKey(mttDefId)) {
				MTTLogic mtt = createMttInstance(mttDefId);
				instances.put(mttDefId, mtt);
			}
		}
		return instances.get(mttDefId); 
	}
	
	
	private MTTLogic createMttInstance(int mttDefId) {
		log.debug("Get MTT Logic Instance for deployment id: "+mttDefId);
		DeploymentServiceContract deployment = services.getServiceInstance(DeploymentServiceContract.class);
		TournamentDeployment dep = deployment.getDeploymentManager().getAllTournamentDeployments().get(mttDefId);
		
		try {
			TournamentRevision revision = dep.getLatestRevision();
			MTTLogic logic = revision.newTournamentInstance();
			return logic;
			
		} catch (Exception e) {
			log.error("Could not create MTT instance", e);
		} 
		
		// ERROR RETURN
		return null;
	}
	
	
}
