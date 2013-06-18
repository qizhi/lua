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
package com.cubeia.firebase.service.deploy.game.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.DeploymentImpl;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.game.GameDeployment;
import com.cubeia.firebase.server.deployment.game.GameRevision;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.service.space.TableSpaceServiceContract;

/**
 * This class models a deployment for a Game Archive only.
 * The class exposes some game-specific methods such as:
 * 
 * instantiateGame(...);
 * 
 * @author Fredrik
 */
public class GameDeploymentImpl extends DeploymentImpl implements GameDeployment {

	private Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Holds all version revisions of the archive.
	 */
	private ConcurrentMap<Integer, GameRevision> revisions = new ConcurrentHashMap<Integer, GameRevision>();
	
	/** Inject this before using */
	// private File workFolder;
	private ClassLoader parentLoader; // never null

	private final ServiceRegistry reg;
	
	public GameDeploymentImpl(String name, DeploymentType type, ClassLoader parentLoader, ServiceRegistry reg) {
		super(name, type);
		this.parentLoader = parentLoader;
		this.reg = reg;
		if(parentLoader == null) {
			this.parentLoader = getClass().getClassLoader();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#getArtifactId()
	 */
	@Override
	public String getArtifactId() {
		GameRevision rev = getLatestRevision();
		if(rev == null) {
			return super.getArtifactId();
		} else {
			return String.valueOf(rev.getGameDefinition().getId());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#getArtifactName()
	 */
	@Override
	public String getArtifactName() {
		GameRevision rev = getLatestRevision();
		if(rev == null) {
			return getIdentifier();
		} else {
			return rev.getGameDefinition().getName();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#getArtifactVersion()
	 */
	@Override
	public String getArtifactVersion() {
		GameRevision rev = getLatestRevision();
		if(rev == null) {
			return String.valueOf(getLatestVersion());
		} else {
			return rev.getGameDefinition().getVersion();
		}
	}
	
	public GameDeploymentImpl(String name, DeploymentType type, ServiceRegistry reg) {
		this(name, type, null, reg);
	}
	
	private int incrementVersion() {
		return latestVersion.incrementAndGet();
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#unregisterClassLoaders()
	 */
	@Override
	public void unregisterClassLoaders() {
		for (GameRevision rev : revisions.values()) {
			unregisterLoader(rev);
		}
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#registerClassLoaders()
	 */
	@Override
	public void registerClassLoaders() {
		for (GameRevision rev : revisions.values()) {
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
		File targetFolder = new File(workFolder+"/game/"+name+"/"+version);
		
		if (!targetFolder.mkdirs()) {
			log.error("Could not create archive target folders!");
			throw new RuntimeException("Could not create archive target folders!");
		}
		
		return targetFolder;
	}*/
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#getAllRevisions()
	 */
	@Override
	public Map<Integer, GameRevision> getAllRevisions() {
		return revisions;
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#getGameRevision(int)
	 */
	@Override
	public GameRevision getGameRevision(int version) {
		return revisions.get(version);
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#getLatestRevision()
	 */
	@Override
	public GameRevision getLatestRevision() {
		return revisions.get(latestVersion.get());
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.game.impl.GameDeployment#newRevision(com.cubeia.firebase.server.deployment.resources.DeploymentResource, boolean)
	 */
	@Override
	public GameRevision newRevision(DeploymentResource resource, boolean registerClassLoader) throws DeploymentFailedException {
		// Arguments.notNull(workFolder, "workFolder");
		int version = incrementVersion(); 
		// Create a target folder
		// File target = createNextRevisionTargetDir(version);
		
		// GameArchive archive = new GameArchive(resource);
		// archive.extract(target);
		
		resource = (DeploymentResource) resource.clone(version, true);
		if(!resource.isExploded()) {
			throw new DeploymentFailedException("Failed to explode deployment '" + resource.getName() + "'");
		}
		
		try {
			GameRevisionImpl revision = new GameRevisionImpl(version, resource, parentLoader);
			log.info("New game revision created: " + revision);
			revisions.put(version, revision);
			notifyNewRevision(revision);
			if(registerClassLoader) {
				registerLoader(revision);
			}
			return revision;
		} catch (IOException e) {
			throw new DeploymentFailedException("Could not create a new game revision. Reported error: " + e.getMessage(), e);
		}
	}

	private void registerLoader(GameRevision revision) {
		TableSpaceServiceContract con = reg.getServiceInstance(TableSpaceServiceContract.class);
		GameObjectSpace<FirebaseTable, GameAction> space = con.getObjectSpace(FirebaseTable.class, GameAction.class);
		ClassLoader loader = revision.getRevisionClassLoader();
		int id = revision.getGameDefinition().getId();
		space.registerClassloader(id, loader);
	}
	
	private void unregisterLoader(GameRevision revision) {
		TableSpaceServiceContract con = reg.getServiceInstance(TableSpaceServiceContract.class);
		GameObjectSpace<FirebaseTable, GameAction> space = con.getObjectSpace(FirebaseTable.class, GameAction.class);
		// ClassLoader loader = revision.getRevisionClassLoader();
		int id = revision.getGameDefinition().getId();
		space.unregisterClassloader(id);
	}

	private void notifyNewRevision(GameRevision revision) {
		SystemStateServiceContract systemState = reg.getServiceInstance(SystemStateServiceContract.class);
		GameDefinition def = revision.getGameDefinition();
		systemState.setAttribute(SystemStateConstants.GAME_ROOT_FQN+def.getId(), "version", def.getVersion());
	}

	/*public void setWorkFolder(File workFolder) {
		this.workFolder = workFolder;
	}*/	
}
