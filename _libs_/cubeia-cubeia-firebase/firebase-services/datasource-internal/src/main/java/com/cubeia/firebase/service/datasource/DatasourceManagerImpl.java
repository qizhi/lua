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
package com.cubeia.firebase.service.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.service.datasource.DatasourceListener;


/**
 * Implementation of the DatasourceManager interface.
 * 
 * 
 * @author Fredrik
 *
 */
public class DatasourceManagerImpl implements DatasourceManager {
	
	/** The Logger */
	@SuppressWarnings("unused")
	private transient Logger log = Logger.getLogger(this.getClass());
	
	private State state = State.STOPPED;

	/** Holds references to all deploymed datasources */
	private Map<String, DataSource> datasources = new ConcurrentHashMap<String, DataSource>();
	private Map<String, TxType> types = new ConcurrentHashMap<String, TxType>();
	
	private List<DatasourceListener> listeners = new CopyOnWriteArrayList<DatasourceListener>();
	
	
	@Override
	public void addDatasourceListener(DatasourceListener l) {
		if(l != null) {
			listeners.add(l);
		}
	}
	
	@Override
	public List<String> getDatasources() {
		return new ArrayList<String>(datasources.keySet());
	}
	
	@Override
	public void removeDatasourceListener(DatasourceListener l) {
		if(l != null) {
			listeners.remove(l);
		}
	}
	
	
	/**
	 * Adds the datasource.
	 * Will not notify any other services.
	 */
	public void addDatasource(String name, DataSource ds, TxType type) {
		datasources.put(name, ds);
		types.put(name, type);
		for (DatasourceListener l : listeners) {
			l.datasourceAdded(name);
		}
	}

	public TxType getDatasourceType(String name) {
		return types.get(name);
	}
	
	/**
	 * Get a datasource from the datasource registry
	 */
	public DataSource getDatasource(String name) {
		return datasources.get(name);
	}


	public DataSource getSystemDatasource() {
		return datasources.get(DatasourceManager.SYSTEM_DS);
	}
	
	/**
	 * Replaces the datasource.
	 * Will notify other services.
	 */
	public void redeploy(String name, DataSource ds, TxType type) {
		// Here we need to notify the persistence manager that we have
		// a redeployed datasource. 
		datasources.put(name, ds);	
		types.put(name, type);
		for (DatasourceListener l : listeners) {
			l.datasourceRedeployed(name);
		}
	}

	public String getStateDescription() {
		return state.toString();
	}

	public void start() {
		state = State.STARTED;
	}

	public void stop() {
		state = State.STOPPED;
	}

	/**
	 * Checks whether the manager holds 
	 * the given datasource
	 */
	public boolean exists(String name) {
		return datasources.containsKey(name);
	}
}
