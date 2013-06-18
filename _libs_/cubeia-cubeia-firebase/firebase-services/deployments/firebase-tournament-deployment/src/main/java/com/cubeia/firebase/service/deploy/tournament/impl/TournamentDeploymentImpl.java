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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.mtt.TournamentDefinition;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.mtt.state.trans.TransactionalMttState;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.DeploymentImpl;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.mtt.TournamentDeployment;
import com.cubeia.firebase.server.deployment.mtt.TournamentRevision;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;


/**
 * This class models a deployment for a Tournament Archive.
 * 
 * @author Fredrik
 */
public class TournamentDeploymentImpl extends DeploymentImpl implements TournamentDeployment {

	private Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Holds all version revisions of the archive.
	 */
	private ConcurrentMap<Integer, TournamentRevision> revisions = new ConcurrentHashMap<Integer, TournamentRevision>();
	
	/* Inject this before using */
	// private File workFolder;
	private ClassLoader parentLoader; // never null

	private final ServiceRegistry reg;
	
	public TournamentDeploymentImpl(String name, DeploymentType type, ClassLoader parentLoader, ServiceRegistry reg) {
		super(name, type);
		this.parentLoader = parentLoader;
		this.reg = reg;
		if(parentLoader == null) {
			this.parentLoader = getClass().getClassLoader();
		}
	}
	
	public TournamentDeploymentImpl(String name, DeploymentType type, ServiceRegistry reg) {
		this(name, type, null, reg);
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#getArtifactId()
	 */
	@Override
	public String getArtifactId() {
		TournamentRevision rev = getLatestRevision();
		if(rev == null) {

			return super.getArtifactId();
		} else {
			return String.valueOf(rev.getTournamentDefinition().getId());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#getArtifactName()
	 */
	@Override
	public String getArtifactName() {
		TournamentRevision rev = getLatestRevision();
		if(rev == null) {
			return getIdentifier();
		} else {
			return rev.getTournamentDefinition().getName();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#getArtifactVersion()
	 */
	@Override
	public String getArtifactVersion() {
		TournamentRevision rev = getLatestRevision();
		if(rev == null) {
			return String.valueOf(getLatestVersion());
		} else {
			return rev.getTournamentDefinition().getVersion();
		}
	}
	
	private int incrementVersion() {
		return latestVersion.incrementAndGet();
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#unregisterClassLoaders()
	 */
	@Override
	public void unregisterClassLoaders() {
		for (TournamentRevision rev : revisions.values()) {
			unregisterLoader(rev);
		}
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#registerClassLoaders()
	 */
	@Override
	public void registerClassLoaders() {
		for (TournamentRevision rev : revisions.values()) {
			registerLoader(rev);
		}
	}

	/*
	 * Increments version and gets a target folder
	 * for extracting (target folder returned is
	 * relative to the work folder).
	 * 
	 * All folders will be created
	 * 
	 * @return
	 */
	/*private File createNextRevisionTargetDir(int version) {
		File targetFolder = new File(workFolder+"/mtt/"+name+"/"+version);
		
		if (!targetFolder.mkdirs()) {
			log.error("Could not create archive target folders!");
			throw new RuntimeException("Could not create archive target folders!");
		}
		
		return targetFolder;
	}*/
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#getAllRevisions()
	 */
	@Override
	public Map<Integer, TournamentRevision> getAllRevisions() {
		return revisions;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#getTournamentRevision(int)
	 */
	@Override
	public TournamentRevision getTournamentRevision(int version) {
		return revisions.get(version);
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#getLatestRevision()
	 */
	@Override
	public TournamentRevision getLatestRevision() {
		return revisions.get(latestVersion.get());
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.tournament.impl.TournamentDeployment#newRevision(com.cubeia.firebase.server.deployment.resources.DeploymentResource, boolean)
	 */
	@Override
	public TournamentRevision newRevision(DeploymentResource resource, boolean registerClassLoader) throws DeploymentFailedException {
		// Arguments.notNull(workFolder, "workFolder");
		int version = incrementVersion(); 
		// Create a target folder
		// File target = createNextRevisionTargetDir(version);
		
		// TournamentArchive archive = new TournamentArchive(resource);
		// archive.extract(target);
		
		resource = (DeploymentResource) resource.clone(version, true);
		if(!resource.isExploded()) {
			throw new DeploymentFailedException("Failed to explode deployment '" + resource.getName() + "'");
		}
		
		try {
			TournamentRevisionImpl revision = new TournamentRevisionImpl(version, resource, parentLoader);
			log.info("New tournament revision created: "+revision);
			revisions.put(version, revision);
			notifyNewRevision(revision);
			if(registerClassLoader) {
				registerLoader(revision);
			}
			return revision;
		} catch (IOException e) {
			throw new DeploymentFailedException("Could not create a new tournament revision. Reported error: "+e,e);
		}
	}

	private void notifyNewRevision(TournamentRevision revision) {
		SystemStateServiceContract systemState = reg.getServiceInstance(SystemStateServiceContract.class);
		TournamentDefinition def = revision.getTournamentDefinition();
		systemState.setAttribute(SystemStateConstants.MTT_META_ROOT_FQN+def.getId(), "version", def.getVersion());
	}
	
	private void registerLoader(TournamentRevision revision) {
		TableSpaceServiceContract con = reg.getServiceInstance(TableSpaceServiceContract.class);
		GameObjectSpace<TransactionalMttState, MttAction> space = con.getObjectSpace(TransactionalMttState.class, MttAction.class);
		ClassLoader loader = revision.getRevisionClassLoader();
		int id = revision.getTournamentDefinition().getId();
		space.registerClassloader(id, loader);
	}
	
	private void unregisterLoader(TournamentRevision revision) {
		TableSpaceServiceContract con = reg.getServiceInstance(TableSpaceServiceContract.class);
		GameObjectSpace<TransactionalMttState, MttAction> space = con.getObjectSpace(TransactionalMttState.class, MttAction.class);
		// ClassLoader loader = revision.getRevisionClassLoader();
		int id = revision.getTournamentDefinition().getId();
		space.unregisterClassloader(id);
	}
}
