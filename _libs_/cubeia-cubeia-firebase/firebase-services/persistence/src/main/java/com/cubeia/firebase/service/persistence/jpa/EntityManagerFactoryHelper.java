/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
 *
 * This program is licensed under a Firebase Enterprise Edition
 * License. You should have received a copy of the Firebase Enterprise
 * Edition License along with this program. If not, contact info@cubeia.com.
 */
package com.cubeia.firebase.service.persistence.jpa;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.log4j.Logger;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.hibernate.ejb.packaging.PersistenceXmlLoader;

/**
 * Provides useful methods for creating and dealing
 * with entitymanagers and their likes.
 * 
 * This class is somewhat of a glue class between Hibernate and JPA.
 * It turns out that hibernate and JPA is not totally transparent;
 * hibernate has its own persistence unit information wrapper and 
 * trying to use EJBConfiguration with any arbitrary persistence-declaration
 * is like pulling your nails off your fingers. Slowly.
 * 
 * We are using Hibernate EntityManager 3.2.1 GA at the time of 
 * writing, perhaps support for JPA container managers will increase
 * over new releases.
 * 
 * @author Fredrik
 *
 */
public class EntityManagerFactoryHelper {
	
	/** Tzee wonderful logger */
	private transient Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Loads persistence metadata objects from found persistence.xml.
	 * 
	 * The URL can be inside a jarfile by using the URL syntax.
	 * E.g.:
	 * jar:file:/E:/Projects/sandbox/JPA_testing/game.jar!/META-INF/persistence.xml
	 * 
	 * 
	 * @param url, URL to the persistence.xml
	 * 
	 * @return A list of persistence unit declarations
	 * 
	 * @throws Exception, Some kind of exception (!) will be thrown if the loader failed. 
	 * 
	 */
	public List<PersistenceMetadata> loadMetadata(URL url) throws Exception {
		List<PersistenceMetadata> units = PersistenceXmlLoader.deploy(url, new HashMap<String, String>(), null);
        return units;
	}
	
	/**
	 * Transform a hibernate metadata object into a JPA standard
	 * persistence info object.
	 * 
	 * IMPORTANT: The datasource will not be set in the returned
	 * object regardless of setting in the Metadata object.
	 * 
	 * To provide a datasource, you will need to set it you self by
	 * calling either 
	 * <code>pi.setJtaDataSource(...)</code> 
	 * or 
	 * <code>pi.setNonJtaDataSource(...)</code>
	 * 
	 * If using the DatasourceManager, you can check the PersistenceMetadata
	 * object for datasource. Use something like this:
	 * 
	 * <br>
	 * <code>
	 * if (metadata.getJtaDatasource() != null) {
	 * 		DataSource ds = datasourceManager.getDatasource(metadata.getJtaDatasource());
	 *		pi.setJtaDataSource(ds); 		
	 * } else {
	 * 		DataSource ds = datasourceManager.getDatasource(metadata.getNonJtaDatasource());
	 *		pi.setNonJtaDataSource(ds); 		
	 * }
	 * </code>
	 * <br>
	 * 
	 * That should take care of business.
	 * 
	 * @param metadata
	 * @param persistenceUrl
	 * @param jarUrl
	 * 
	 * @return PersistenceUnitInfo <strong>without</strong> any datasource set.
	 */
	public PersistenceInfoImpl createPersistenceInfo(PersistenceMetadata metadata, URL persistenceUrl, URL jarUrl) {
		return createPersistenceInfo(metadata, persistenceUrl, jarUrl, Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * Transform a hibernate metadata object into a JPA standard
	 * persistence info object.
	 * 
	 * IMPORTANT: The datasource will not be set in the returned
	 * object regardless of setting in the Metadata object.
	 * 
	 * To provide a datasource, you will need to set it you self by
	 * calling either 
	 * <code>pi.setJtaDataSource(...)</code> 
	 * or 
	 * <code>pi.setNonJtaDataSource(...)</code>
	 * 
	 * If using the DatasourceManager, you can check the PersistenceMetadata
	 * object for datasource. Use something like this:
	 * 
	 * <br>
	 * <code>
	 * if (metadata.getJtaDatasource() != null) {
	 * 		DataSource ds = datasourceManager.getDatasource(metadata.getJtaDatasource());
	 *		pi.setJtaDataSource(ds); 		
	 * } else {
	 * 		DataSource ds = datasourceManager.getDatasource(metadata.getNonJtaDatasource());
	 *		pi.setNonJtaDataSource(ds); 		
	 * }
	 * </code>
	 * <br>
	 * 
	 * That should take care of business.
	 * 
	 * @param metadata
	 * @param persistenceUrl
	 * @param jarUrl
	 * 
	 * @return PersistenceUnitInfo <strong>without</strong> any datasource set.
	 */
	public PersistenceInfoImpl createPersistenceInfo(PersistenceMetadata metadata, URL persistenceUrl, URL jarUrl, ClassLoader classLoader) {
		PersistenceInfoImpl pi = new PersistenceInfoImpl();
		pi.setClassLoader(classLoader);
		
		ArrayList<URL> jarFiles = new ArrayList<URL>();
		pi.setJarFiles(jarFiles);
		pi.setPersistenceProviderClassName(HibernatePersistence.class.getName());
		pi.setEntityclassNames(metadata.getClasses());
		pi.setEntityManagerName(metadata.getName());
		pi.setPersistenceXmlFileUrl(persistenceUrl);
		pi.setMappingFileNames(metadata.getMappingFiles());

		pi.setTransactionType(metadata.getTransactionType());
		
		// URL to JAR
		pi.setPersistenceUnitRootUrl(jarUrl);
		
		// JTA Resource local
		// pi.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
		
		Properties props = new Properties();
		props.putAll(metadata.getProps());
		pi.setProperties(props);
		
		return pi;
	}
	
	/**
	 * Create an Entity Manager Factory from the hibernate persistence unit
	 * metadata and URLs to persistence xml and to the jar file containing the
	 * persistence xml.
	 * 
	 * @param metadata
	 * @param persistenceUrl
	 * @param jarUrl
	 * 
	 * @return EntityManagerFactory, the factory. 
	 * 
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public EntityManagerFactory createEMF(PersistenceUnitInfo pi, Map<String, String> properties) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> providerClass = Thread.currentThread().getContextClassLoader().loadClass(pi.getPersistenceProviderClassName());
		PersistenceProvider pp = (PersistenceProvider) providerClass.newInstance();
		if(properties == null) properties = new HashMap<String, String>();
		EntityManagerFactory factory = pp.createContainerEntityManagerFactory(pi, properties);
		if (log.isDebugEnabled()) {
			log.debug("EntityManagerFactory created: "+factory);
		}
		return factory;
	}
}
