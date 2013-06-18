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
package com.cubeia.firebase.service.conn;

import com.cubeia.firebase.api.service.Contract;

/**
 * This is the service contract for cluster connections. It is responsible for
 * opening and closing connections used in internal communication. Currently there
 * is only a "shared" connection available.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 29
 * @see ClusterConnection
 */
public interface ConnectionServiceContract extends Contract {

	/*
	 * @return A new cluster connection, never null
	 * @throws ClusterException If a new connection cannot be opened
	 */
	// public ClusterConnection openNewConnection() throws ClusterException;
	
	/**
	 * @return A shared connection, used by services mainly, never null
	 */
	public ClusterConnection getSharedConnection();
	
	/**
	 * @param conn Connection to close, must not be null
	 */
	public void closeConnection(ClusterConnection conn);
	
}
