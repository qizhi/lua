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
package com.cubeia.firebase.api.service.datasource;

import java.util.List;

import javax.sql.DataSource;

import com.cubeia.firebase.api.service.Contract;

/**
 * This is the public interface for accessing deployed data sources 
 * within the firebase server. 
 * 
 * @author Larsan
 */
public interface DatasourceServiceContract extends Contract {
	
	/**
	 * @param l Listener to add, must not be null
	 */
	public void addDatasourceListener(DatasourceListener l);
	
	
	/**
	 * @param l Listener to remove, must not be null
	 */
	public void removeDatasourceListener(DatasourceListener l);
	
	/**
	 * @return A list of database names, never null
	 */
	public List<String> getDatasources();

	/**
	 * @param name Data source name, must not be null
	 * @return True if the data source is deployed, false otherwise
	 */
	public boolean exists(String name);
	
	/**
	 * @param name Data source name, must not be null
	 * @return The deployed data source, or null if not deployed
	 */
	public DataSource getDatasource(String name);
	
	/**
	 * @return Any data source deployed with name "system", or null if not deployed
	 */
	public DataSource getSystemDatasource();

}