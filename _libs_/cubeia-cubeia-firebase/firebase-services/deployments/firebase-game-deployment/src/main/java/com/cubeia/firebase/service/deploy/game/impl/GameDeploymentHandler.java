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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.NodeInfo;
import com.cubeia.firebase.api.server.NodeType;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.deployment.Deployment;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.DeploymentHandler;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.game.GameDeployment;
import com.cubeia.firebase.server.deployment.game.GameRevision;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

public class GameDeploymentHandler implements DeploymentHandler {
	
	// private static final String PERSISTENCE_FILE = "META-INF" + File.separator + "persistence.xml";
	
	/** The Logger */
	private Logger log = Logger.getLogger(this.getClass());
	
	/** Keep a reference of all deployed games */
	private Map<String, GameDeploymentImpl> games = new ConcurrentHashMap<String, GameDeploymentImpl>();

	/** Work folder */
	// private final File workfolder;
	// private ClassLoader parentLoader;
	private final ServiceRegistry reg;
	
	private final Set<NodeInfo> gameNodes = new HashSet<NodeInfo>();
	
	public GameDeploymentHandler(/*File workfolder, */ServiceRegistry reg/*, ClassLoader parent*/) {
		//this.parentLoader = parent;
		// this.workfolder = workfolder;
		this.reg = reg;
	}
	
	/*public void setDeploymentClassLoader(ClassLoader parent) {
		this.parentLoader = parent;
	}*/
	
	/*public String getFilter() {
		return Constants.GAME_ARCHIVE_DEF_REGEX;
	}*/
	
	public synchronized void registerNodeDown(NodeInfo info) {
		if(info.getType() == NodeType.GAME) {
			gameNodes.remove(info);
			if(gameNodes.size() == 0) {
				for (GameDeployment dep : games.values()) {
					dep.unregisterClassLoaders();
				}
			}
		}
	}
	
	public synchronized boolean haveGameNode() {
		return gameNodes.size() > 0;
	}
	
	public synchronized void registerNodeUp(NodeInfo info) {
		if(info.getType() == NodeType.GAME) {
			gameNodes.add(info);
			if(gameNodes.size() == 1) {
				for (GameDeployment dep : games.values()) {
					dep.registerClassLoaders();
				}
			}
		}
	}

	/**
	 * Handle changed deployment resource.
	 * @throws IOException 
	 */
	public Deployment handle(DeploymentResource resource, ClassLoader parentLoader) throws DeploymentFailedException {
		String name = resource.getName().substring(0, resource.getName().length()-Constants.GAME_ARCHIVE_DEF_TAIL.length());
		
		GameDeploymentImpl deploy = null;
		// See if we have this game as a deployment already
		if (games.containsKey(name)) {
			// Re-deploy
			deploy = games.get(name);
			handleReDeploy(deploy, resource);
		} else {
			// New deployment
			deploy = handleNewDeploy(name, resource, parentLoader);
		}
		
		return deploy;
	}

	/**
	 * A new game archive was deployed. Handle this.
	 * 
	 * We will create a new GameDeployment with version 1
	 * and explode the archive to a work folder.
	 * 
	 * @param resource
	 * @param name
	 * @return the created GameDeployment. Version will be set to 1.
	 */
	private GameDeploymentImpl handleNewDeploy(String name, DeploymentResource resource, ClassLoader parentLoader) throws DeploymentFailedException {
		GameDeploymentImpl deploy = new GameDeploymentImpl(name, DeploymentType.GAME_ARCHIVE, parentLoader, reg);
		// deploy.setWorkFolder(workfolder);
		GameRevision revision = deploy.newRevision(resource, haveGameNode());
		if(alreadyExists(revision)) throw new DeploymentFailedException("Failed to deploy game with name '" + name + "'; Game id clash, game id '" + revision.getGameDefinition().getId() + "', revision '" + revision + "' is already deployed!");
		// Deploy persistence if applicable
		// deployPersistenceUnits(revision);
		
		games.put(deploy.getName(), deploy);
		
		// Start a file monitor on the resource
		/*try {
			FileMonitor.getInstance().addFileChangeListener(this, resource.getAbsolutePath(), Constants.DEPLOY_SCAN_REFRESH_PERIOD);
		} catch (FileNotFoundException e) {
			// The resource was not found, not sure what to do... throwing exception for now.
			throw new DeploymentFailedException("Resource file not found: "+resource, e);
		}*/
		
		return deploy;
	}
	
	/*
	 * Check if game id is already deployed
	 */
	private boolean alreadyExists(GameRevision revision) {
		int check = revision.getGameDefinition().getId();
		for (GameDeployment dep : games.values()) {
			GameRevision rev = dep.getLatestRevision();
			if(rev != null) {
				int id = rev.getGameDefinition().getId();
				if(id == check) { 
					return true;
				}
			}
		}
		return false;
	}

	private void handleReDeploy(GameDeploymentImpl deploy, DeploymentResource resource) throws DeploymentFailedException {
		log.info("Redeploying Game Archive: "+deploy.getName());
		deploy.newRevision(resource, haveGameNode());
	}

	/*
	 * Triggered when a file that we are monitoring was changed.
	 */
	/*public void fileChanged(String fileName) {
		try {
			handle(new File(fileName));
		} catch (DeploymentFailedException e) {
			log.error("The redeployment of resource '"+fileName+"' failed. Reported error: "+e, e);
		}
	}*/
	
	public String toString() {
		return "GameDeploymentHandler";
	}

	/*private void deployPersistenceUnits(GameRevision revision) {
		PersistenceServiceContract serviceInstance = reg.getServiceInstance(PersistenceServiceContract.class);
		PersistenceManager pm = serviceInstance.getPersistenceManager();
		
		File base = revision.getResource().getExplodedBase();
		String persistence = "jar:file:"+base.toString()+"!"+File.separator+PERSISTENCE_FILE;
		persistence = persistence.replaceAll("\\\\", "/");
		File[] libs = revision.getLayout().getGameLibraries(revision);
		ClassLoader revisionClassLoader = revision.getRevisionClassLoader();
		
		for (int i = 0; i < libs.length; i++) {
			try {
				URL persistenceUrl = new URL(persistence);
				URL jarUrl = libs[i].toURI().toURL();
				if (persistenceUrl.openConnection().getContentLength() > 0) {
					// We have a persistence.xml
					pm.registerPersistenceUnits(persistenceUrl, jarUrl, revisionClassLoader);
				}
				
			} catch (MalformedURLException e) {
				log.error("Failed (1) to deploy persistence unit for: "+revision+". Resource: "+libs[i], e);
			} catch (PersistenceDeploymentFailedException e) {
				log.error("Failed (2) to deploy persistence unit for: "+revision+". Resource: "+libs[i], e);
			} catch (IOException e) {
				log.error("Failed (3) to deploy persistence unit for: "+revision+". Resource: "+libs[i], e);
			}
		}
		
//		Map<URL, URL> def = revision.getPersistenceDef();
//		for (URL jar : def.keySet()) {
//			try {
//				pm.registerPersistenceUnits(def.get(jar), jar);
//			} catch (PersistenceDeploymentFailedException e) {
//				log.error("Failed to deploy persistence unit for: "+revision+". URL: "+jar);
//			}
//		}
	}*/

}
