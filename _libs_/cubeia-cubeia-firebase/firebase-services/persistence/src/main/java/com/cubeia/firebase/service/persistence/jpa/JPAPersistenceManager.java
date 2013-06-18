/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
 *
 * This program is licensed under a Firebase Enterprise Edition
 * License. You should have received a copy of the Firebase Enterprise
 * Edition License along with this program. If not, contact info@cubeia.com.
 */
package com.cubeia.firebase.service.persistence.jpa;

import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.ejb.packaging.PersistenceMetadata;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigurationException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.datasource.DatasourceManager;
import com.cubeia.firebase.service.datasource.intern.InternalDataSourceProvider;
import com.cubeia.firebase.service.persistence.PersistenceConfig;
import com.cubeia.firebase.service.persistence.PersistenceDeploymentFailedException;
import com.cubeia.firebase.service.persistence.PersistenceManager;

/**
 * Hibernate JPA implementation of the persistence manager.
 * 
 * This class is designed to only exist as a single instance.
 * Since we will access it as a Server Service, it should be fine
 * to not enforce a singleton pattern.
 * 
 * 
 * 
 * @author Fredrik
 *
 */
public class JPAPersistenceManager implements PersistenceManager, JPAPersistenceManagerMBean {
	
	private Logger log = Logger.getLogger(getClass());
	
	private State state = State.STOPPED;
	
	/**
	 * Cache of entity managers.
	 */
	private ConcurrentMap<String,PersistenceFactory> factories = new ConcurrentHashMap<String, PersistenceFactory>();
	
	/**
	 * Reference of datasource names to persistence managers
	 */
	private ConcurrentMap<String,PersistenceFactory> dsToFactory = new ConcurrentHashMap<String, PersistenceFactory>();
	
	
	/**
	 * The Service Registry is used to lookup other service.
	 * This service depends on the Datasource Manager. 
	 */
	private final ServiceRegistry registry;

	/**
	 * Should the created factories collect statistics for JMX?
	 */
	private boolean collectStatistics = false;

	/**
	 * Construct a JPA Persistence Manager.
	 * 
	 *
	 */
	public JPAPersistenceManager(ServiceRegistry registry) throws SystemException {
		this.registry = registry;
		setupConf();
	}

	private void setupConf() throws SystemException {
		try {
			ClusterConfigProviderContract clusterCfg = registry.getServiceInstance(ClusterConfigProviderContract.class);
			PersistenceConfig config = clusterCfg.getConfiguration(PersistenceConfig.class, new Namespace("service.persistence"));
			collectStatistics = config.isStatisticsEnabled();
		} catch(ConfigurationException e) {
			throw new SystemCoreException("Persistence service failed to adapt master proxy configuration; Received message: " + e.getMessage(), e);
		}
	}
	
	public boolean exists(String name) {
		return factories.containsKey(name);
	}

	
	/**
	 * Read configuration from the supplied file (must be a valid
	 * peristence.xml or an exception will be thrown).
	 * 
	 * Then the named datsource is looked up and injected into the
	 * configuration.
	 * 
	 * Finally the EntityManagerFactory is created.
	 * 
	 * @param persistenceUrl, the URL to the persistence.xml
	 * @param jarUrl, URL to base directory or jarfile that will
	 * be scanned for annotated entities.
	 */
	public void registerPersistenceUnits(URL persistenceUrl, URL jarUrl, ClassLoader classLoader) throws PersistenceDeploymentFailedException {
		Arguments.notNull(persistenceUrl, "persistenceUrl");
		Arguments.notNull(jarUrl, "jarUrl");
		Arguments.notNull(classLoader, "classLoader");

		log.info("Deploying persistence units for JAR: "+jarUrl+" XML: "+persistenceUrl);
		
		try {
			// Create the EntityManagerFactoryHelper
			EntityManagerFactoryHelper helper = new EntityManagerFactoryHelper();
			List<PersistenceMetadata> units = helper.loadMetadata(persistenceUrl);
			
			for (PersistenceMetadata unit : units) {
				log.info("Persistence Unit Found: "+unit);
				// Create a new unit
				PersistenceFactory factory = new PersistenceFactory(getDatasourceManager(), unit);
				factory.setCollectStatistics(collectStatistics );
				String datasource = factory.buildEntityManagerFactory(persistenceUrl, jarUrl, classLoader);
				
				// Store the persistence factory using the PU Name
				factories.put(unit.getName(), factory);
				
				// now we also need to keep track of what factory uses what datasource
				// so we cache that relation as well.
				dsToFactory.put(datasource, factory);
				
			}
			
		} catch (Exception e) {
			throw new PersistenceDeploymentFailedException("Persistence deployment failed for: "+persistenceUrl, e);
		}

		
	}

	/**
	 * Trigger a full rebuild of the Entity Manager Factory
	 * @throws PersistenceDeploymentFailedException 
	 */
	public void redeploy(URL persistenceUrl, URL jarUrl) throws PersistenceDeploymentFailedException {
		try {
			// Create the EntityManagerFactoryHelper
			EntityManagerFactoryHelper helper = new EntityManagerFactoryHelper();
			List<PersistenceMetadata> units = helper.loadMetadata(persistenceUrl);
			
			for (PersistenceMetadata unit : units) {
				PersistenceFactory factory = factories.get(unit.getName());
				log.info("Redeploying Persistence Factory: "+factory);
				// Actually... until we are supporting hot deploy of 
				// game archives this shouldn't happen. 
				throw new PersistenceDeploymentFailedException("We don't support game archive hot deploy yet," +
						" so how did this happen? We got a redeployment for: "+unit);
			}
			
		} catch (Exception e) {
			throw new PersistenceDeploymentFailedException("Persistence deployment failed for: "+persistenceUrl, e);
		}
	}

	/**
	 * Trigger a full rebuild of the Entity Manager Factory
	 * which uses the given datasource.
	 * 
	 * We are supporting redeploy of datasources, so this is 
	 * a plausible scenario.
	 * 
	 */
	public void redeployDatasource(String datasource) throws PersistenceDeploymentFailedException {
		PersistenceFactory factory = dsToFactory.get(datasource);
		if (factory != null) {
			log.info("Redeploy Persistence Factory : "+factory+" since it depends on the changed datasource: "+datasource);
			factory.buildEntityManagerFactory();
		}
	}
	
	
	public String getStateDescription() {
		return state.toString();
	}

	/**
	 * 
	 */
	public void start() {
		bindCacheToJMX();
	}
	
	/**
     * Bind the Tree-Cache to JMX
     */
    private void bindCacheToJMX() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(this, new ObjectName("com.cubeia.firebase.jpa:type=PersistenceMan"));
        } catch(Exception ex) {
            log.error("Could bind the cache to the JMX Server", ex);
        }
    }


	/**
	 * Stops all PersistenceFactories
	 */
	public void stop() {
		for (PersistenceFactory factory : factories.values()) {
			factory.stop();
		}
	}
	

	/**
	 * Get the system default datasource.
	 */
	public EntityManager getEntityManager() {
		return getEntityManager(PersistenceManager.SYSTEM_DS);
	}

	public boolean isReady(String name) {
		PersistenceFactory factory = factories.get(name);
		if (factory != null && factory.getDatasourceState().equals(PersistenceFactory.DatasourceState.READY)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Get an Entity Manager from id (name).
	 * The method will return null if the given name
	 * is not bound to a Persistence Factory.
	 * 
	 * @return null if not found.
	 */
	public EntityManager getEntityManager(String name) {
		PersistenceFactory factory = factories.get(name);
		if (factory == null) {
			log.warn("PersistenceFactory '"+name+"' was not found in registry");
			return null;
		} else {
			if (factory.getDatasourceState().equals(PersistenceFactory.DatasourceState.READY)) {
				// The datasource is ready to go
				return factory.getEntityManager();
			} else  {
				// No datasource yet
				// TODO: Change this to a checked exception or null or something more... deterministic
				throw new IllegalStateException("Datasource not ready");
			}
			
			
		}
	}

	/**
	 * Get a named datasource and override properties
	 */
	public EntityManager getEntityManager(String name, Map<?, ?> props) {
		PersistenceFactory factory = factories.get(name);
		return factory.getEntityManager(props);
	}



	/**
	 * Override this to provide a mock implementation.
	 * @return
	 */
	protected DatasourceManager getDatasourceManager() {
		InternalDataSourceProvider service = registry.getServiceInstance(InternalDataSourceProvider.class);
		return service.getDatasourceManager();
	}

	/**
	 * JMX / MBean method
	 */
	public List<String> getPersistenceUnits() {
		List<String> list = new LinkedList<String>();
		for (String name : factories.keySet()) {
			list.add(name+" : "+factories.get(name).getDatasourceState());
		}
		return list;
	}
	
}
