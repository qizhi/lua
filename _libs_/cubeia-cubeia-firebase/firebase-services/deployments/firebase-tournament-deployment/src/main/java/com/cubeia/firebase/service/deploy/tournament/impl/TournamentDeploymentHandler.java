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
import com.cubeia.firebase.server.deployment.mtt.TournamentDeployment;
import com.cubeia.firebase.server.deployment.mtt.TournamentRevision;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;

public class TournamentDeploymentHandler implements DeploymentHandler {
	
	// private static final String PERSISTENCE_FILE = "META-INF" + File.separator + "persistence.xml";
	
	/** The Logger */
	private Logger log = Logger.getLogger(this.getClass());
	
	/** Keep a reference of all deployed games */
	private Map<String, TournamentDeploymentImpl> tournaments = new ConcurrentHashMap<String, TournamentDeploymentImpl>();

	private final Set<NodeInfo> mttNodes = new HashSet<NodeInfo>();
	
	/** Work folder */
	// private final File workfolder;
	// private ClassLoader parentLoader;

	private final ServiceRegistry reg;
	
	public TournamentDeploymentHandler(/*File workfolder,*/ServiceRegistry reg/*, ClassLoader depLoader*/) {
		// this.parentLoader = depLoader;
		// this.workfolder = workfolder;
		this.reg = reg;
	}
	
	/*public void setDeploymentClassLoader(ClassLoader parent) {
		this.parentLoader = parent;
	}*/
	
	/*public String getFilter() {
		return Constants.MTT_ARCHIVE_DEF_REGEX;
	}*/
	
	public synchronized void registerNodeDown(NodeInfo info) {
		if(info.getType() == NodeType.MTT) {
			mttNodes.remove(info);
			if(mttNodes.size() == 0) {
				for (TournamentDeployment dep : tournaments.values()) {
					dep.unregisterClassLoaders();
				}
			}
		}
	}
	
	public synchronized boolean haveMttNode() {
		return mttNodes.size() > 0;
	}
	
	public synchronized void registerNodeUp(NodeInfo info) {
		if(info.getType() == NodeType.MTT) {
			mttNodes.add(info);
			if(mttNodes.size() == 1) {
				for (TournamentDeployment dep : tournaments.values()) {
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
		String name = resource.getName().substring(0, resource.getName().length()-Constants.MTT_ARCHIVE_DEF_TAIL.length());
		
		TournamentDeploymentImpl deploy = null;
		// See if we have this game as a deployment already
		if (tournaments.containsKey(name)) {
			// Re-deploy
			deploy = tournaments.get(name);
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
	 * We will create a new TournamentDeployment with version 1
	 * and explode the archive to a work folder.
	 * 
	 * @param resource
	 * @param name
	 * @return the created TournamentDeployment. Version will be set to 1.
	 */
	private TournamentDeploymentImpl handleNewDeploy(String name, DeploymentResource resource, ClassLoader parentLoader) throws DeploymentFailedException {
		TournamentDeploymentImpl deploy = new TournamentDeploymentImpl(name, DeploymentType.TOURNAMENT_ARCHIVE, parentLoader, reg);
		// deploy.setWorkFolder(workfolder);
		TournamentRevision revision = deploy.newRevision(resource, haveMttNode());
		if(alreadyExists(revision)) throw new DeploymentFailedException("Failed to deploy Tournament with name '" + name + "'; Tournament id clash, Tournament id '" + revision.getTournamentDefinition().getId() + "' is already deployed!");
		// Deploy persistence if applicable
		// deployPersistenceUnits(resource, revision);
		
		tournaments.put(deploy.getName(), deploy);
		
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
	private boolean alreadyExists(TournamentRevision revision) {
		int check = revision.getTournamentDefinition().getId();
		for (TournamentDeployment dep : tournaments.values()) {
			TournamentRevision rev = dep.getLatestRevision();
			if(rev != null) {
				int id = rev.getTournamentDefinition().getId();
				if(id == check) { 
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param deploy
	 * @param resource
	 * @throws IOException 
	 */
	private void handleReDeploy(TournamentDeploymentImpl deploy, DeploymentResource resource) throws DeploymentFailedException {
		log.info("Redeploying Tournament Archive: "+deploy.getName());
		deploy.newRevision(resource, haveMttNode());
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
		return "TournamentDeploymentHandler";
	}

	/*private void deployPersistenceUnits(DeploymentResource resource, TournamentRevision revision) {
		PersistenceServiceContract serviceInstance = reg.getServiceInstance(PersistenceServiceContract.class);
		PersistenceManager pm = serviceInstance.getPersistenceManager();
		
		String persistence = "jar:file:"+resource.getFile().toString()+"!"+File.separator+PERSISTENCE_FILE;
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
		
	}*/

}
