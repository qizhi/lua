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

import java.util.List;

import javax.sql.DataSource;

import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.api.service.datasource.DatasourceListener;

/**
 * The Datasource Manager holds all deployed datasources
 * in the system.
 * 
 * The system will need a defined system-datasource in order to 
 * be able to perform domain-specific task (e.g. login lookup).
 * 
 * The system specific datasource should be deployed as
 * system-ds.xml and will be references to as 'system'.
 * 
 * @author Fredrik
 *
 */
public interface DatasourceManager extends Startable {
	
	/** Default naming convention of the system datasource */
	public static final String SYSTEM_DS = "system";
	
	public void addDatasourceListener(DatasourceListener l);
	
	public void removeDatasourceListener(DatasourceListener l);
	
	/**
	 * Add a new datasource definition.
	 * 
	 * @param ds
	 */
	public void addDatasource(String name, DataSource source, TxType type);
	
	/**
	 * Change a datasource bind.
	 * This will trigger rebuild of any applicable
	 * persistence services (I.e. PersistenceManager)
	 * 
	 * @param datasource
	 */
	public void redeploy(String name, DataSource source, TxType type);
	
	public boolean exists(String name);
	
	/**
	 * Get the datasource with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public DataSource getDatasource(String name);
	
	/**
	 * @return A list of database names, never null
	 */
	public List<String> getDatasources();
	
	/**
	 * @param name 
	 * @return The source tx-type, or null if not found
	 */
	public TxType getDatasourceType(String name);
	
	/**
	 * Get the system default datasource.
	 * You need to have a deployed system-ds.xml for this to return a valid datasource.
	 * 
	 * @param name
	 * @return
	 */
	public DataSource getSystemDatasource();
	
	
}
