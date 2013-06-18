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
package com.cubeia.firebase.server.deployment;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import se.xec.commons.fileset.FileSet;
import se.xec.commons.fileset.FileSetEvent;
import se.xec.commons.fileset.FileSetListener;
import se.xec.commons.path.Path;

import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.mtt.TournamentDefinition;
import com.cubeia.firebase.api.server.NodeInfo;
import com.cubeia.firebase.api.server.NodeListener;
import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.deployment.game.GameDeployment;
import com.cubeia.firebase.server.deployment.mtt.TournamentDeployment;
import com.cubeia.firebase.server.deployment.resources.DeploymentFileSet;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.deployment.ua.UnifiedArchiveDeploymentHandler;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.instance.SystemLogger;

/**
 * The Deployment Manager watches over the designated deploy folder and
 * detects changes to it.
 * 
 * <p>Each specific deployed game archive is referred to as a Deployment. Each 
 * Deployment may have multiple versions.
 * 
 * @author Fredrik
 */
public class DeploymentManager implements Startable, NodeListener {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private final ResourceManager resources;
	private final Map<DeploymentType, DeploymentHandler> handlers = new HashMap<DeploymentType, DeploymentHandler>(7);
	
	/*
	 * Keep track of all deployments. Uses Deployment.Identifier as key and Deployment as value.
	 */
	private ConcurrentMap<String, Deployment> deployments = new ConcurrentHashMap<String, Deployment>();
	private ConcurrentMap<Integer, GameDeployment> gameDeployments = new ConcurrentHashMap<Integer, GameDeployment>();
	private ConcurrentMap<Integer, TournamentDeployment> tournamentDeployments = new ConcurrentHashMap<Integer, TournamentDeployment>();

	private final ServiceContext con;
	private Listener listener;
	
	public DeploymentManager(ResourceManager manager, ServiceContext con) throws SystemException {
		Arguments.notNull(manager, "manager");
		Arguments.notNull(con, "context");
		this.resources = manager;
		this.con = con;
	}

	public void start() {
		try {
			registerHandlers();
		} catch (SystemCoreException e) {
			log.fatal("Failed to register handlers!", e);
		}
		doResourcePickup();
		startListening();
	}

	public void stop() { 
		stopListening();
	}

	public Deployment getDeployment(String identifier) {
		return deployments.get(identifier);
	}

	public Map<String, Deployment> getAllDeployments() {
		return deployments;
	}
	
	public Map<String, Deployment> getAllDeployments(DeploymentType type) {
		Map<String, Deployment> map = new TreeMap<String, Deployment>();
		for (String key : deployments.keySet()) {
			Deployment dep = deployments.get(key);
			if(dep.getType() == type) {
				map.put(key, dep);
			}
		}
		return map;
	}
	
	public Map<String, GameDeployment> getAllGameDeployments() {
		Map<String, GameDeployment> games = new HashMap<String, GameDeployment>();
		for (Deployment resource : deployments.values()) {
			if (resource.getType() == DeploymentType.GAME_ARCHIVE) {
				games.put(resource.getIdentifier(), (GameDeployment)resource);
			}
		}
		return games;
	}

	public Map<Integer, TournamentDeployment> getAllTournamentDeployments() {
		return tournamentDeployments;
	}
	
	public GameDeployment getGameDeploymentForId(int gameId) {
		return gameDeployments.get(gameId);
	}
	
	public TournamentDeployment getTournamentDeploymentForId(int mttId) {
		return tournamentDeployments.get(mttId);
	}
	
	
	// --- NODE LISTENER -- ///
	
	public void nodeDestroy(NodeInfo info, boolean isPre) {
		if(!isPre) {
			for (DeploymentHandler h : handlers.values()) {
				h.registerNodeDown(info);
			}
		}
	}
	
	public void nodeInit(NodeInfo info, boolean isPre) {
		if(isPre) {
			for (DeploymentHandler h : handlers.values()) {
				h.registerNodeUp(info);
			}
		}
	}
	
	public void nodeStart(NodeInfo info, boolean isPre) { }
	
	public void nodeStop(NodeInfo info, boolean isPre) { }



	// --- PRIVATE METHODS --- //
	
	private void startListening() {
		listener = new Listener();
		DeploymentFileSet set = resources.getDeploymentFileSet();
		set.addFileSetListener(listener);
	}
	
	private void stopListening() {
		DeploymentFileSet set = resources.getDeploymentFileSet();
		set.removeFileSetListener(listener);
	}

	private void registerHandlers() throws SystemCoreException {
		
		// ClassLoader depLoader = resources.getDeploymentClassLoader();
		// handlers.put(DeploymentType.GAME_ARCHIVE, con.getParentRegistry().getServiceInstance(DeploymentHandlerService.class));
		// handlers.put(DeploymentType.DATA_SOURCE, new DatasourceDeploymentHandler(con.getParentRegistry()));
		// handlers.put(DeploymentType.GAME_ACTIVATOR_CONF, new GarActivationConfigDeploymentHandler(con.getParentRegistry()));
		// handlers.put(DeploymentType.TOURNAMENT_ACTIVATOR_CONF, new TarActivationConfigDeploymentHandler(con.getParentRegistry()));
		// handlers.put(DeploymentType.PERSISTENCE_ARCHIVE, new PersistenceDeploymentHandler(con.getParentRegistry()));
		// handlers.put(DeploymentType.TOURNAMENT_ARCHIVE, new TournamentDeploymentHandler(con.getParentRegistry()));
		
		ServiceRegistry reg = con.getParentRegistry();
		for (ServiceInfo info : reg.listServicesByContract(DeploymentHandlerService.class)) {
			log.debug("Found deployment service: " + info.getPublicId());
			DeploymentHandlerService serv = (DeploymentHandlerService) reg.getServiceInstance(info.getPublicId());
			handlers.put(serv.getDeploymentType(), serv);
		}
		
		handlers.put(DeploymentType.UNIFIED_ARCHIVE, new UnifiedArchiveDeploymentHandler(new UnifiedDeploymentCallback() {
		
			public void deploy(FileSet set, ClassLoader parentLoader) {
				doFileSetPickup(set, parentLoader);
			}
		}));
	}

	private void doResourcePickup() {
		DeploymentFileSet set = resources.getDeploymentFileSet();
		doFileSetPickup(set, resources.getDeploymentClassLoader());
	}

	private void doFileSetPickup(FileSet set, ClassLoader parent) {
		for (Path p : set.getResourcePaths()) {
			if(!deployments.containsKey(p.getName())) {
				DeploymentType type = DeploymentType.parse(p.getName());
				DeploymentResource resource = (DeploymentResource)set.getResource(p);
				DeploymentHandler handler = handlers.get(type);
				if(resource != null && handler != null) {
					deploy(resource, handler, parent);
				}
			}
		}
	}
	
	public void doResourceChange(DeploymentResource res) {
		DeploymentType type = res.getDeploymentType();
		DeploymentHandler handler = handlers.get(type);
		if(handler != null) {
			deploy(res, handler, resources.getDeploymentClassLoader());
		}
	}

	private void deploy(DeploymentResource resource, DeploymentHandler handler, ClassLoader parentLoader) {
		try {
			// Handle the resource
			Deployment deploy = handler.handle(resource, parentLoader);
			if (deploy != null) {
				deployments.put(resource.getName(), deploy);
				SystemLogger.info("Deployed: " + deploy.getArtifactName() + " " + deploy.getArtifactVersion() + "; Internal ID: " + deploy.getArtifactId() + "; Deployment Revision: " + deploy.getLatestVersion());
				if (deploy.getType() == DeploymentType.GAME_ARCHIVE) {
					GameDeployment gd = (GameDeployment)deploy;
					GameDefinition def = gd.getLatestRevision().getGameDefinition();
					gameDeployments .put(def.getId(), gd);
				} else if (deploy.getType() == DeploymentType.TOURNAMENT_ARCHIVE) {
					TournamentDeployment gd = (TournamentDeployment)deploy;
					TournamentDefinition def = gd.getLatestRevision().getTournamentDefinition();
					tournamentDeployments.put(def.getId(), gd);
				}
			}
		} catch (DeploymentFailedException e) {
			log.error("The deployment of resource '"+resource+"' failed. Reported error: "+e, e);
		}
	}
	
	
	// --- INNER CLASSES --- //
	
	private class Listener implements FileSetListener {
		
		public void receiveFileSetEvent(FileSetEvent event) {
			if(event.getType() == FileSetEvent.RESOURCE_ADDED) {
				doResourcePickup();
			} else if(event.getType() == FileSetEvent.RESOURCE_CHANGED) {
				doResourceChange((DeploymentResource)event.getResource());
			}
		}
	}
}
