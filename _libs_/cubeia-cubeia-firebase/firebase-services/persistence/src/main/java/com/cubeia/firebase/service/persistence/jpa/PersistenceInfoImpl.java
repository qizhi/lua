/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
 *
 * This program is licensed under a Firebase Enterprise Edition
 * License. You should have received a copy of the Firebase Enterprise
 * Edition License along with this program. If not, contact info@cubeia.com.
 */
package com.cubeia.firebase.service.persistence.jpa;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

/**
 * This class contains the information from a persistence.xml declaration.
 * 
 * This class is a DTO that wraps the Hibernate JPA info model
 * 'PersistenceMetadata' as a JPA standard info model 
 * 'PersistenceUnitInfo'.
 * 
 * Personally I think this is a mess. But all in all we have no 
 * other way of creating a container entity manager factory.
 * 
 * Reference javax.persistence.PersistenceProvider:
 * http://www.hibernate.org/hib_docs/ejb3-api/javax/persistence/spi/PersistenceProvider.html
 * 
 * Some getters / setters are redundant since I am basing this on the JBoss/hibernate
 * implementation.
 * 
 * @author Fredrik
 */
public class PersistenceInfoImpl implements PersistenceUnitInfo {
	
	private String entityManagerName;
	private DataSource jtaDataSource;
	private DataSource nonJtaDataSource;
	private List<String> mappingFileNames;
	private List<URL> jarFiles;
	private List<String> entityclassNames;
	private Properties properties;
	private ClassLoader classLoader;
	private URL persistenceXmlFileUrl;
	private URL entityMappingsXmlFileUrl;
	private String persistenceProviderClassName;
	private URL persistenceUnitRootUrl;
	private PersistenceUnitTransactionType persistenceUnitTransactionType;

	public PersistenceInfoImpl(){}

	public String getPersistenceProviderClassName()	{
		return persistenceProviderClassName;
	}

	public void setPersistenceProviderClassName(String persistenceProviderClassName) {
		this.persistenceProviderClassName = persistenceProviderClassName;
	}

	public String getEntityManagerName() {
		return entityManagerName;
	}

	public void setEntityManagerName(String entityManagerName) {
		this.entityManagerName = entityManagerName;
	}

	public DataSource getJtaDataSource() {
		return jtaDataSource;
	}

	public void setJtaDataSource(DataSource jtaDataSource) {
		this.jtaDataSource = jtaDataSource;
	}

	public DataSource getNonJtaDataSource() {
		return nonJtaDataSource;
	}

	public void setNonJtaDataSource(DataSource nonJtaDataSource) {
		this.nonJtaDataSource = nonJtaDataSource;
	}

	public List<String> getMappingFileNames() {
		return mappingFileNames;
	}

	public void setMappingFileNames(List<String> mappingFileNames) {
		this.mappingFileNames = mappingFileNames;
	}

	public List<URL> getJarFiles() {
		return jarFiles;
	}

	public void setJarFiles(List<URL> jarFiles) {
		this.jarFiles = jarFiles;
	}

	public List<String> getEntityclassNames() {
		return entityclassNames;
	}

	public void setEntityclassNames(List<String> entityclassNames) {
		this.entityclassNames = entityclassNames;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public URL getPersistenceXmlFileUrl() {
		return persistenceXmlFileUrl;
	}

	public void setPersistenceXmlFileUrl(URL persistenceXmlFileUrl) {
		this.persistenceXmlFileUrl = persistenceXmlFileUrl;
	}

	public URL getEntityMappingsXmlFileUrl(){
		return entityMappingsXmlFileUrl;
	}

	public void setEntityMappingsXmlFileUrl(URL entityMappingsXmlFileUrl) {
		this.entityMappingsXmlFileUrl = entityMappingsXmlFileUrl;
	}

	public void addTransformer(ClassTransformer arg0) {
		// Not implemented
	}

	public boolean excludeUnlistedClasses() {
		return false;
	}

	public List<URL> getJarFileUrls() {
		return getJarFiles();
	}

	public List<String> getManagedClassNames() {
		return getEntityclassNames();
	}

	/** Will return same as getClassLoader */
	public ClassLoader getNewTempClassLoader() {
		return classLoader;
	}

	public String getPersistenceUnitName() {
		return entityManagerName;
	}

	public URL getPersistenceUnitRootUrl() {
		return persistenceUnitRootUrl;
	}

	public PersistenceUnitTransactionType getTransactionType() {
		return persistenceUnitTransactionType;
	}

	public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
		this.persistenceUnitRootUrl = persistenceUnitRootUrl;
	}

	public void setTransactionType(PersistenceUnitTransactionType persistenceUnitTransactionType) {
		this.persistenceUnitTransactionType = persistenceUnitTransactionType;
	}


}
