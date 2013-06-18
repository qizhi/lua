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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.deployment.DeploymentFailedException;
import com.cubeia.firebase.server.deployment.DeploymentImpl;
import com.cubeia.firebase.server.deployment.DeploymentType;
import com.cubeia.firebase.server.deployment.datasource.DatasourceDeployment;
import com.cubeia.firebase.server.deployment.resources.DeploymentResource;
import com.cubeia.firebase.service.datasource.TxType;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.util.IoUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Wraps a deployed datasource in a deployment descriptor.
 * 
 * 
 * @author Fredrik
 *
 */
public class DatasourceDeploymentImpl extends DeploymentImpl implements DatasourceDeployment {

	
	/** The Logger */
	private transient Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Holds the datasource properties.
	 * E.g. 
	 * 	URL, user, password etc.
	 * But also pooling (C3P0) specific properties
	 * can be included here. 
	 */
	private Properties dsProps;
	
	/**
	 * The underlying datasource.
	 * We will not keep a revision history for datasources.
	 * The latest will always be used.
	 */
	private DataSource datasource;
	
	
	/**
	 * Constructor 
	 * 
	 * @param name
	 * @param type
	 */
	public DatasourceDeploymentImpl(String name, DeploymentType type) {
		super(name, type);
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.ds.impl.DataSourceDeplyment#getArtifactName()
	 */
	@Override
	public String getArtifactName() {
		return getIdentifier();
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.ds.impl.DataSourceDeplyment#getArtifactVersion()
	 */
	@Override
	public String getArtifactVersion() {
		return String.valueOf(getLatestVersion());
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.ds.impl.DataSourceDeplyment#configure(com.cubeia.firebase.server.deployment.resources.DeploymentResource, com.cubeia.firebase.api.service.ServiceRegistry)
	 */
	@Override
	public void configure(DeploymentResource resource, ServiceRegistry registry) throws DeploymentFailedException, IOException {
		// Load property file and catch all possible errors
		dsProps = new Properties();
		InputStream in = null;
		try {
			in = resource.getInputStream();
			dsProps.loadFromXML(in);
		} catch (InvalidPropertiesFormatException e) {
			throw new IOException("Datasource descriptor '"+resource+"' is invalid; Message: " + e.getMessage());
		} catch (FileNotFoundException e) {
			throw new IOException("Datasource descriptor '"+resource+"' could not be found.");
		} catch (IOException e) {
			throw new IOException("Datasource descriptor '"+resource+"' could not be read; Message: " + e.getMessage());
		} finally {
			IoUtil.safeClose(in);
		}
		
		writePropertiesToLog();
		
		String txType = dsProps.getProperty(TX_TYPE_KEY, NON_TX_PROPERTY);
		
		try {
			if(txType.equals(NON_TX_PROPERTY)) createNonTxDataSource();
			else createLocalTxDataSource(registry);
		} catch(Exception e) {
			throw new DeploymentFailedException("Failed to deploy " + txType + " data source '" + getName() + "'; Recevied error message: " + e.getMessage(), e);
		}
	}
	
	private void createLocalTxDataSource(ServiceRegistry registry) throws SQLException, Exception {
		TransactionManagerProvider service = registry.getServiceInstance(TransactionManagerProvider.class);
		this.datasource = service.createLocalTxDataSource(getName(), dsProps);
	}
	
	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.ds.impl.DataSourceDeplyment#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return dsProps;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.ds.impl.DataSourceDeplyment#getTxType()
	 */
	@Override
	public TxType getTxType() {
		if(dsProps.getProperty(TX_TYPE_KEY, NON_TX_PROPERTY).equals(NON_TX_PROPERTY)) {
			return TxType.NON_TX;
		} else {
			return TxType.LOCAL_TX;
		}
	}
	
	private void createNonTxDataSource() throws SQLException, Exception {
		ComboPooledDataSource ds = new ComboPooledDataSource(); 
		ds.setDataSourceName(getName());
		ds.setDriverClass( dsProps.getProperty("driver") ); 
		ds.setJdbcUrl( dsProps.getProperty("url") ); 
		ds.setUser(dsProps.getProperty("user")); 
		ds.setPassword(dsProps.getProperty("password"));
		ds.setMaxIdleTime(getIntProperty(TIMEOUT_KEY, TIMEOUT_DEF));
		ds.setMaxPoolSize(getIntProperty(MAX_SIZE_KEY, MAX_SIZE_DEF));
		ds.setMinPoolSize(getIntProperty(MIN_SIZE_KEY, MIN_SIZE_DEF));
		ds.setCheckoutTimeout(getIntProperty(CHECKOUT_TIMEOUT_KEY, CHECKOUT_TIMEOUT_DEF));
		if(dsProps.containsKey(VALIDATION_STM_KEY)) {
			ds.setPreferredTestQuery(dsProps.getProperty(VALIDATION_STM_KEY));
			ds.setIdleConnectionTestPeriod(ds.getMaxIdleTime()); // TRAC ISSUE #62
		}
		this.datasource = ds;
	}
	
	private int getIntProperty(String key, int defValue) {
		String tmp = dsProps.getProperty(key);
		if(tmp == null || tmp.length() == 0) return defValue;
		else return safeParse(key, tmp, defValue);
	}

	private int safeParse(String key, String tmp, int def) {
		try {
			return Integer.parseInt(tmp);
		} catch(NumberFormatException e) {
			log.error("Illegal number format in datasource descriptor, property '" + key  + "'; Reverting to default value: " + def, e);
			return def;
		}
	}

	/*private long getLongProperty(String key, long defValue) {
		String tmp = dsProps.getProperty(key);
		if(tmp == null || tmp.length() == 0) return defValue;
		else return Long.parseLong(tmp);
	}*/

	/**
	 * Tell the people about the properties loaded for the 
	 * datasouce.
	 */
	private void writePropertiesToLog() {
		// Report loaded definitions in the log
		String description = "";
		for (Object key : dsProps.keySet()) {
			String property = "password".equals(key) ? "*****" : dsProps.getProperty(key.toString());
			description += "\n\t"+key+" = " + property;
		}
		log.info("Datasource definition loaded: "+description);
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.deploy.ds.impl.DataSourceDeplyment#getDatasource()
	 */
	@Override
	public DataSource getDatasource() {
		return datasource;
	}
}
