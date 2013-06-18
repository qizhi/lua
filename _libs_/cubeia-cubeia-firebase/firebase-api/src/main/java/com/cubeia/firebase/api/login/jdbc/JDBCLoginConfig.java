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
package com.cubeia.firebase.api.login.jdbc;

import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Configurated;
import com.cubeia.firebase.api.server.conf.Property;

/**
 * Configuration interface for the jdbc login service. This interface is
 * fullfilled from the cluster configuration. The following properties
 * apply:
 * 
 * <pre>
 * 		service.login.data-source - deployed data source name
 * 		service.login.table-name - database table to use
 * 		service.login.id-column - user id column name
 * 		service.login.user-column - user name column name
 * 		service.login.password-column - user password column name
 * </pre>
 * 
 * @author Larsan
 * @date 2007 maj 24
 */
@Configurated(namespace="service.login")
public interface JDBCLoginConfig extends Configurable {

	/**
	 * @return The name of the data source to use, must not return null
	 */
	@Property public String getDataSource();
	
	
	/**
	 * @return The name of the table, must not return null
	 */
	@Property public String getTableName();
	
	
	/**
	 * @return The name of the user id column, must not return null
	 */
	@Property public String getIdColumn();
	
	/**
	 * @return The name of the user name column, must not return null
	 */
	@Property public String getUserColumn();
	
	/**
	 * @return The name of the password column, must not return null
	 */
	@Property public String getPasswordColumn();
	
}
