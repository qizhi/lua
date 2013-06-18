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

import com.cubeia.firebase.server.deployment.DeploymentImpl;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.UnifiedDeploymentCallback;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.deployment.resources.FileSetResource;

public class UnifiedArchiveDeployment extends DeploymentImpl {

	private final Map<Integer, UnifiedArchiveRevision> revs = new ConcurrentHashMap<Integer, UnifiedArchiveRevision>();
	
	private ClassLoader parentLoader;
	private final UnifiedDeploymentCallback deploymentManager;
	 
	public UnifiedArchiveDeployment(String name, ClassLoader parentLoader, UnifiedDeploymentCallback deploymentManager) {
		super(name, DeploymentType.UNIFIED_ARCHIVE);
		this.parentLoader = parentLoader;
		this.deploymentManager = deploymentManager;
		if(parentLoader == null) {
			this.parentLoader = getClass().getClassLoader();
		}
	}
	
	@Override
	public String getArtifactName() {
		return getIdentifier();
	}
	
	@Override
	public String getArtifactVersion() {
		return String.valueOf(getLatestVersion());
	}
	
	/**
	 * This method is called the first time a resource is deployed. The deployment
	 * will attempt to re-use existing clones (which may have been created when the server
	 * started and looked for available services).
	 */
	public UnifiedArchiveRevision firstRevision(DeploymentResource res) {
		FileSetResource clone = res.getLatestClone();
		if(clone == null) { 
			return newRevision(res);
		} else {
			int version = clone.getDeploymentVersion();
			return newRevision(version, clone);
		}
	}

	public UnifiedArchiveRevision newRevision(DeploymentResource res) {
		int version = incrementVersion();
		FileSetResource clone = res.clone(version, true);
		return newRevision(version, clone);
	}
	
	public Map<Integer, UnifiedArchiveRevision> getAllRevisions() {
		return revs;
	}
	
	public UnifiedArchiveRevision getArchiveRevision(int version) {
		return revs.get(version);
	}
	
	public UnifiedArchiveRevision getLatestRevision() {
		return revs.get(latestVersion.get());
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private UnifiedArchiveRevision newRevision(int version, FileSetResource clone) {
		UnifiedArchiveRevision rev = new UnifiedArchiveRevision(version, clone, parentLoader);
		Logger.getLogger(getClass()).info("New unified archive revision created: " + version);
		rev.deploy(deploymentManager);
		revs.put(version, rev);
		return rev;
	}
	
	private int incrementVersion() {
		return latestVersion.incrementAndGet();
	}
}
