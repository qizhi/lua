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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.hibernate.jmx.StatisticsService;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.datasource.DatasourceManager;
import com.cubeia.firebase.service.persistence.PersistenceDeploymentFailedException;
import com.cubeia.firebase.util.FirebaseLockFactory;
import com.cubeia.firebase.util.ServiceMBean;

/**
 * 
 * The Persistency Factory handles access to the underlying
 * EntityManagerFactory. We need to keep locks for access
 * due to hot deploy of new persistence and datasources.
 * 
 * Specified JTA Datasource has precedence over non-JTA datasources.
 * 
 * @author Fredrik
 *
 */
public class PersistenceFactory implements ServiceMBean {
	
	private Logger log = Logger.getLogger(getClass());
	
	private State state = State.STOPPED;

	/** 
	 * Factory for creating EntityManager.
	 * This is the managed resource. 
	 */
	private EntityManagerFactory factory; 
	
	/**
	 * Read/write lock for accessing the entity manager factory.
	 */
	private ReadWriteLock emLock = FirebaseLockFactory.createLock();

	/** The persistence ingfo used to build the EntityManagerFactory */
	private PersistenceInfoImpl persistenceInfo;

	/** The hibernate metadata */
	private final PersistenceMetadata unit;

	/**
	 * The Datasource Manager is used to lookup datasources
	 * which are referenced in the persistence declaration.
	 * The service is injected.
	 */
	private final DatasourceManager dsManager;
	
	/** Readiness state for the datasource which should be used */
	private DatasourceState datasourceState = DatasourceState.NOT_READY;

	private URL persistenceUrl;

	private URL jarUrl;

	private ClassLoader classLoader;

	private boolean collectStatistics = true;
	
	public static enum DatasourceState {
		NOT_READY,
		READY
	}
	
	/**
	 * Constructor.
	 * @throws PersistenceDeploymentFailedException 
	 * 
	 */
	public PersistenceFactory(DatasourceManager dsManager, PersistenceMetadata unit) throws PersistenceDeploymentFailedException {
		this.dsManager = dsManager;
		this.unit = unit;
		log.debug("Created a Persistence Factory with name: "+unit.getName());
	}
	
	/**
	 * Retrieves the datasource from the datasource manager.
	 * This should needed to be called when rebuilding the 
	 * entity manager factory.
	 * 
	 * @return
	 * @throws PersistenceDeploymentFailedException
	 */
	private DataSource lookupDatasource() throws PersistenceDeploymentFailedException {
		Arguments.notNull(dsManager, "datasourceManager");
		
		DataSource datasource = null;
		
		// First get the named datasource
		String ds = getNamedDatasource();
		
		if (ds != null) {
			datasource = dsManager.getDatasource(ds);
		} else {
			throw new PersistenceDeploymentFailedException("Persistence Unit '"+unit.getName()+"' does not specify datasource");
		}
		
		// Did we get a datasource?
		if (datasource != null) {
			datasourceState = DatasourceState.READY;
		}
		
		return datasource;
	}

	public String getNamedDatasource() {
		String ds = unit.getJtaDatasource();
		if (ds == null) {
			ds = unit.getNonJtaDatasource();
		}
		return ds;
	}


	public String getStateDescription() {
		return state.toString();
	}

	/**
	 * Starts the PerstistenceFactory and creates an EntityManager
	 * for the given datasource.
	 * 
	 */
	public void start() {
		state = State.STARTED;
	}

	public void stop() {
		factory.close();
		state = State.STOPPED;
	}

	
	/**
	 * (Re-)Build an inital build of a new EntityManagerFactory from the set Persistence data
	 * and the datasource.
	 * 
	 * A build should triggered by:
	 * 1. A redeployed game with a persistence unit
	 * 2. A redeployed datasource
	 * 
	 * 
	 * Acquires a writelock while rebuilding the 
	 * EntityManagerFactory
	 * 
	 * return Name of the bound datasource.
	 * @throws PersistenceDeploymentFailedException 
	 */
	public String buildEntityManagerFactory(URL persistenceUrl, URL jarUrl, ClassLoader classLoader) throws PersistenceDeploymentFailedException {
		this.persistenceUrl = persistenceUrl;
		this.jarUrl = jarUrl;
		this.classLoader = classLoader;
		return buildEntityManagerFactory();
	}
	
	public String buildEntityManagerFactory(URL persistenceUrl, URL jarUrl) throws PersistenceDeploymentFailedException {
		this.persistenceUrl = persistenceUrl;
		this.jarUrl = jarUrl;
		this.classLoader = Thread.currentThread().getContextClassLoader();
		return buildEntityManagerFactory();
	}
	
	
	
	
	/**
	 * (Re-)Build a new EntityManagerFactory from the set Persistence data
	 * and the datasource. Will use URLs to persistence and jarfile from previous
	 * deploys.
	 * 
	 * This method can only be used for successive builds and not for the initial build
	 * since the URLs will not have been set.
	 * 
	 * A build should triggered by:
	 * 1. A redeployed game with a persistence unit
	 * 2. A redeployed datasource
	 * 
	 * 
	 * Acquires a writelock while rebuilding the 
	 * EntityManagerFactory
	 * 
	 * return Name of the bound datasource.
	 * @throws PersistenceDeploymentFailedException 
	 */
	public String buildEntityManagerFactory() throws PersistenceDeploymentFailedException {
		emLock.writeLock().lock();
		try {
			EntityManagerFactoryHelper helper = new EntityManagerFactoryHelper();
			// Convert to JPA persistence info
			persistenceInfo = helper.createPersistenceInfo(unit, persistenceUrl, jarUrl, classLoader);
			
			// Get datasource, fail if we could not acquire it
			DataSource source = lookupDatasource();
			
			// Set Datasource here
			if (unit.getJtaDatasource() != null) {
				persistenceInfo.setJtaDataSource(source);
			} else {
				persistenceInfo.setNonJtaDataSource(source);
			} 
			
			Map<String, String> props = getFactoryProperties();
			
			if (datasourceState.equals(DatasourceState.READY)) {
				factory = helper.createEMF(persistenceInfo, props);
				if (collectStatistics) {
					checkForStatistics(factory);
				}
			} else {
				log.warn("Persistence Unit '"+unit.getName()+"' is waiting for datasource '"+getNamedDatasource()+"'");
			}
			
			return getNamedDatasource();
			
		} catch (Exception e) {
			throw new PersistenceDeploymentFailedException(e);
		} finally {
			emLock.writeLock().unlock();
		}
	}

	private void checkForStatistics(EntityManagerFactory factory) {
		if (factory instanceof HibernateEntityManagerFactory) {
			try {
				HibernateEntityManagerFactory hFactory = (HibernateEntityManagerFactory) factory;
				SessionFactory sessionFactory = hFactory.getSessionFactory();

				Hashtable<String, String> tb = new Hashtable<String, String>();
				tb.put("type", "statistics");
				tb.put("sessionFactory", persistenceInfo.getEntityManagerName());
				StatisticsService statistics = new StatisticsService();
				statistics.setSessionFactory(sessionFactory);
				statistics.setStatisticsEnabled(true);
				
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				mbs.registerMBean(statistics, new ObjectName("com.cubeia.firebase.jpa:type=PM["+persistenceInfo.getEntityManagerName()+"]"));

			} catch(Exception ex) {
				log.error("Could bind JPA statistics to the JMX Server", ex);
			}
		}
	}
	


	/**
	 * Get the entity manager for this datasource.
	 * 
	 * Acquires a readlock for the entity manager.
	 * 
	 * @return
	 */
	public EntityManager getEntityManager() {
		try {
			emLock.readLock().lock();
			return factory.createEntityManager();
		} finally {
			emLock.readLock().unlock();
		}
	}
	
	/**
	 * Get the entity manager for this datasource.
	 * 
	 * Acquires a readlock for the entity manager.
	 * 
	 * @param props, properties that wiull override the 
	 * default entitymanager's.
	 * @return EntityManager
	 */
	public EntityManager getEntityManager(Map<?, ?> props) {
		try {
			emLock.readLock().lock();
			return factory.createEntityManager(props);
		} finally {
			emLock.readLock().unlock();
		}
	}

	public DatasourceState getDatasourceState() {
		return datasourceState;
	}
	
	
	private Map<String, String> getFactoryProperties() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("hibernate.transaction.manager_lookup_class", "com.cubeia.firebase.service.persistence.jpa.SystemTransactionManagerLookup");
		// map.put("hibernate.transaction.factory_class", "com.cubeia.firebase.service.persistence.jpa.SystemTransactionFactory");
		return map;
	}

	public void setCollectStatistics(boolean collectStatistics) {
		this.collectStatistics = collectStatistics;
	}
	
}
