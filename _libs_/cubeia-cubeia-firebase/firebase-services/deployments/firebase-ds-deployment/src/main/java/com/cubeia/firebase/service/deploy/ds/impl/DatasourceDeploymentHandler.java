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
package com.cubeia.firebase.service.deploy.ds.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.NodeInfo;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.Constants;
import com.cubeia.firebase.server.deployment.Deployment;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.DeploymentHandler;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.service.datasource.DatasourceManager;
import com.cubeia.firebase.service.datasource.intern.InternalDataSourceProvider;

public class DatasourceDeploymentHandler implements DeploymentHandler {

	/** The Logger */
	private Logger log = Logger.getLogger(this.getClass());

	/** The Datasource Manager */
	private final DatasourceManager manager;
	
	/** The Persistence Manager */
	// private final PersistenceManager persistenceManager;
	
	private final ServiceRegistry registry;
	
	
	/**
	 * Constructor.
	 * Will load the datasource manager from the registry right away. 
	 * 
	 * @param context, only use null if you are mocking the access
	 * to the service registry.
	 */
	public DatasourceDeploymentHandler(ServiceRegistry registry) throws SystemCoreException {
		this.registry = registry;
		manager = getDatasourceManager();
		// persistenceManager = getPersistenceManager();
	}
	
	
	public void setDeploymentClassLoader(ClassLoader parent) { }
	
	public void registerNodeDown(NodeInfo info) { }
	
	public void registerNodeUp(NodeInfo info) { }
	
	/*public String getFilter() {
		return Constants.DATASOURCE_DEF_REGEX;
	}*/

	/**
	 * Handle new datasource deployed
	 * 
	 * @return Deployment if ok. Null if deployment failed.
	 */
	public Deployment handle(DeploymentResource resource, ClassLoader parentLoader) throws DeploymentFailedException {
		String name = resource.getName().substring(0, resource.getName().length()-Constants.DATASOURCE_DEF_TAIL.length());
		
		Deployment deployment = null;
		// Check if new deployment or redeploy
		if (!manager.exists(name)) {
			deployment = newDeployment(name, resource);
		} else {
			deployment = reDeploy(name, resource);
		}
		
		// We have (re)deployed a datasource, we need to tell the persistence manager
		/*try {
			persistenceManager.redeployDatasource(deployment.getName());
		} catch (PersistenceDeploymentFailedException e) {
			// Hmm.. we encountered problems, but I can't quit deploying the datasource
			log.error("Error when notifying the Persistence Manager of new datasource", e);
		}*/
		
		return deployment;
	}


	private Deployment newDeployment(String name, DeploymentResource resource) throws DeploymentFailedException {
		log.info("New datasource targeted for deployment: '"+name+"'  resource: '"+resource+"'");
		DatasourceDeploymentImpl deployment = null;
		try {
			deployment = new DatasourceDeploymentImpl(name, DeploymentType.DATA_SOURCE);
			deployment.configure(resource, registry);

			// Register the datasource to the manager
			manager.addDatasource(name, deployment.getDatasource(), deployment.getTxType());
			
			// Start a file monitor on the resource
			/*try {
				FileMonitor.getInstance().addFileChangeListener(this, resource.getAbsolutePath(), Constants.DEPLOY_SCAN_REFRESH_PERIOD);
			} catch (FileNotFoundException e) {
				// The resource was not found, not sure what to do... throwing exception for now.
				throw new DeploymentFailedException("Resource file not found: "+resource, e);
			}*/
			
		} catch (IOException e) {
			throw new DeploymentFailedException("Could not deploy resource '"+name+"': "+resource, e);
		}
		
		return deployment;
	}
	
	
	private Deployment reDeploy(String name, DeploymentResource resource) throws DeploymentFailedException {
		log.info("Redeploying datasource: '"+name+"'  resource: '"+resource+"'");
		// Only switch deployment if we can load definition
		// and construct a datasource 
		DatasourceDeploymentImpl redeploy = null;
		try {
			redeploy = new DatasourceDeploymentImpl(name, DeploymentType.DATA_SOURCE);
			redeploy.configure(resource, registry);

			// Register the datasource to the manager
			manager.redeploy(name, redeploy.getDatasource(), redeploy.getTxType());
			
		} catch (IOException e) {
			throw new DeploymentFailedException("Could not redeploy resource '"+name+"': "+resource, e);
		}
		 
		
		return redeploy;
	}
	
	/**
	 * Protected accessor for the datasource manager.
	 * Will use the service registry to lookup the manager.
	 * Override this method to provide your own lookup (direct injection)
	 * of a datasource manager when running unit tests. 
	 * @return
	 */
	protected DatasourceManager getDatasourceManager() throws SystemCoreException {
		// Lookup the datasource manager service
		InternalDataSourceProvider service = registry.getServiceInstance(InternalDataSourceProvider.class);
		
		if (service == null) {
			SystemLogger.error("The DatasourceDeploymentHandler could not acquire DatasourceManager service");
			throw new SystemCoreException("Could not acquire DatasourceManager service!");
		}
		
		return service.getDatasourceManager();
	}
	
	/*
	 * Protected accessor for the persistence manager.
	 * Will use the service registry to lookup the manager.
	 * Override this method to provide your own lookup (direct injection)
	 * of a datasource manager when running unit tests. 
	 * @return
	 */
	/*protected PersistenceManager getPersistenceManager() throws SystemCoreException {
		// Lookup the datasource manager service
		PersistenceServiceContract service = registry.getServiceInstance(PersistenceServiceContract.class);
		
		if (service == null) {
			SystemLogger.error("The DatasourceDeploymentHandler could not acquire PersistenceManager service");
			throw new SystemCoreException("Could not acquire PersistenceManager service!");
		}
		return service.getPersistenceManager();
	}*/
	
	public String toString() {
		return "DatasourceDeploymentHandler";
	}

	/*
	 * A monitored resource was changed so we better act on it.
	 */
	/*public void fileChanged(String fileName) {
		try {
			handle(new File(fileName));
		} catch (DeploymentFailedException e) {
			log.error("The redeployment of resource '"+fileName+"' failed. Reported error: "+e, e);
		}
	}*/
}
