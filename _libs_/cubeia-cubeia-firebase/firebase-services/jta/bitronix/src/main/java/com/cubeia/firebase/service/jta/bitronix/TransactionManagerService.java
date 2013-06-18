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
package com.cubeia.firebase.service.jta.bitronix;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.transaction.EventTransactionContext;
import com.cubeia.firebase.server.deployment.datasource.DatasourceDeployment;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;

/**
 * See the {@link TransactionManagerProvider implemented interface} for a functional 
 * description of this service. This is an implementation using Bitronix as JTA provider.
 * 
 * @author Lars J. Nilsson
 * @see TransactionManagerProvider
 */
public class TransactionManagerService implements TransactionManagerProvider, Service {

	// Illegal LOCAL-TX value
	private static final String MISSING_PROP_ERR = "Missing property, a LOCAL-TX data source must have either " + DatasourceDeployment.XA_DATA_SOURCE_KEY + " or " + DatasourceDeployment.DRIVER_KEY + " property set.";
	private CoreTransactionManager coreManager;

	
	// --- LIFETIME METHODS --- //
	
	@Override
	public void destroy() { 
		getManager().shutdown();
	}

	@Override
	public void init(ServiceContext con) throws SystemException { 
		coreManager = con.getParentRegistry().getServiceInstance(CoreTransactionManager.class);
		performConfiguration(con);
	}

	@Override
	public void start() { }

	@Override
	public void stop() { }
	
	
	// --- TRANCATION MANAGER PROVIDER --- //
	
	@Override
	public DataSource createLocalTxDataSource(String name, Properties props) throws SQLException {
		DataSource s = null;
		if(isEmulated(props)) {
			s = newEmulatedDriver(name, props);
		} else {
			s = newXADataSource(name, props);
		}
		/*
		 * Hack: we're opening a connections here in order to 
		 * get the class loading right.
		 */
		s.getConnection().close();
		return s;
	}
	
	@Override
	public EventTransactionContext getEventContext() {
		CoreTransaction trans = coreManager.currentTransaction();
		return (trans == null ? null : trans.getEventContext());
	}

	@Override
	public TransactionManager getTransactionManager() {
		return getManager();
	}

	@Override
	public UserTransaction getUserTransaction() {
		return getManager();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	// is this for an emulated driver?
	private boolean isEmulated(Properties props) throws SQLException {
		if(props.containsKey(DatasourceDeployment.XA_DATA_SOURCE_KEY)) return false;
		else if(props.containsKey(DatasourceDeployment.DRIVER_KEY))return true;
		else throw new SQLException(MISSING_PROP_ERR);
	}
	
	private void performConfiguration(ServiceContext con) {
		Configuration conf = TransactionManagerServices.getConfiguration();
		conf.setServerId(con.getServerId());
		conf.setJournal("null"); // no journaling
		conf.setDefaultTransactionTimeout(60); // One minute
		conf.setDisableJmx(true);
	}
	
	private BitronixTransactionManager getManager() {
		return TransactionManagerServices.getTransactionManager();
	}
	
	private DataSource newXADataSource(String name, Properties props) {
		PoolingDataSource ds = new PoolingDataSource();   
		ds.setAllowLocalTransactions(true); // ?! /Larsan
		ds.setClassName(props.getProperty(DatasourceDeployment.XA_DATA_SOURCE_KEY));
		ds.setDriverProperties(getXaDsProps(props));
		ds.setUniqueName(name);
		if(props.containsKey(DatasourceDeployment.MIN_SIZE_KEY)) {
			ds.setMinPoolSize(getInt(props, DatasourceDeployment.MIN_SIZE_KEY, DatasourceDeployment.MIN_SIZE_DEF));
		} else {
			ds.setMinPoolSize(DatasourceDeployment.MIN_SIZE_DEF);
		}
		if(props.containsKey(DatasourceDeployment.MAX_SIZE_KEY)) {
			ds.setMaxPoolSize(getInt(props, DatasourceDeployment.MAX_SIZE_KEY, DatasourceDeployment.MAX_SIZE_DEF));
		} else {
			ds.setMaxPoolSize(DatasourceDeployment.MAX_SIZE_DEF);
		}
		if(props.containsKey(DatasourceDeployment.VALIDATION_STM_KEY)) {
			ds.setTestQuery(props.getProperty(DatasourceDeployment.VALIDATION_STM_KEY));
		}
		if(props.containsKey(DatasourceDeployment.CHECKOUT_TIMEOUT_KEY)) {
			ds.setAcquisitionTimeout(getInt(props, DatasourceDeployment.CHECKOUT_TIMEOUT_KEY, DatasourceDeployment.CHECKOUT_TIMEOUT_DEF / 1000));
		}
		if(props.containsKey(DatasourceDeployment.TIMEOUT_KEY)) {
			ds.setMaxIdleTime(getInt(props, DatasourceDeployment.TIMEOUT_KEY, DatasourceDeployment.TIMEOUT_DEF / 1000));
		}
		return ds;
	}
	
	// get props in string form
	private Properties getXaDsProps(Properties props) {
		Properties next = new Properties();
		for (Iterator<?> it = props.keySet().iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			if(key.startsWith("ds.")) {
				String val = props.getProperty(key);
				String realKey = key.substring(3);
				next.setProperty(realKey, val);
			}
		}
		return next;
	}

	private DataSource newEmulatedDriver(String name, Properties props) {
		PoolingDataSource ds = new PoolingDataSource();
		ds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
		ds.getDriverProperties().setProperty("driverClassName", props.getProperty(DatasourceDeployment.DRIVER_KEY));
		ds.setUniqueName(name);
		ds.setAllowLocalTransactions(true); // ? /LJN
		if(props.containsKey(DatasourceDeployment.MIN_SIZE_KEY)) {
			ds.setMinPoolSize(getInt(props, DatasourceDeployment.MIN_SIZE_KEY, DatasourceDeployment.MIN_SIZE_DEF));
		} else {
			ds.setMinPoolSize(DatasourceDeployment.MIN_SIZE_DEF);
		}
		if(props.containsKey(DatasourceDeployment.MAX_SIZE_KEY)) {
			ds.setMaxPoolSize(getInt(props, DatasourceDeployment.MAX_SIZE_KEY, DatasourceDeployment.MAX_SIZE_DEF));
		} else {
			ds.setMaxPoolSize(DatasourceDeployment.MAX_SIZE_DEF);
		}
		if(props.containsKey(DatasourceDeployment.VALIDATION_STM_KEY)) {
			ds.setTestQuery(props.getProperty(DatasourceDeployment.VALIDATION_STM_KEY));
		}
		if(props.containsKey(DatasourceDeployment.CHECKOUT_TIMEOUT_KEY)) {
			ds.setAcquisitionTimeout(getInt(props, DatasourceDeployment.CHECKOUT_TIMEOUT_KEY, DatasourceDeployment.CHECKOUT_TIMEOUT_DEF / 1000));
		}
		if(props.containsKey(DatasourceDeployment.TIMEOUT_KEY)) {
			ds.setMaxIdleTime(getInt(props, DatasourceDeployment.TIMEOUT_KEY, DatasourceDeployment.TIMEOUT_DEF / 1000));
		}
		ds.getDriverProperties().setProperty("url", props.getProperty(DatasourceDeployment.URL_KEY));
		ds.getDriverProperties().setProperty("user", props.getProperty(DatasourceDeployment.USER_KEY));
		ds.getDriverProperties().setProperty("password", props.getProperty(DatasourceDeployment.PASSWORD_KEY));
		return ds;
	}
	
	private int getInt(Properties props, String key, int def) {
		String s = props.getProperty(key);
		return safeParse(s, def);
	}

	private int safeParse(String s, int def) {
		if(s == null) return def;
		try {
			return Integer.parseInt(s);
		} catch(NumberFormatException e) {
			return def;
		}
	}
}
