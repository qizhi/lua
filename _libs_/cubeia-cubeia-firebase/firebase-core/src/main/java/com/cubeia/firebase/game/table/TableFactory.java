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
package com.cubeia.firebase.game.table;

import com.cubeia.firebase.api.game.table.Table;

/**
 * A simple factory interface for creating certain types of
 * tables. The factory will must probably be stateful and not kept
 * as a singleton. 
 * 
 * <p>The factory is thread safe.
 * 
 * <p>NB: Table data objects is paired with their specific
 * table representations, thus the data object must be created by the
 * factory class as well.
 * 
 * @author Larsan
 */
public interface TableFactory<E extends Table> {
	
	/**
	 * @return The configured size in bytes at which the system will warn for large packages, or -1
	 */
	public long getTableWarnSize();

	/**
	 * For a particular game, set a corresponding class loader. If 
	 * found, the tables will use the class loader when deserializng
	 * the game state object. Set to null in order to remove the class 
	 * loader.
	 * 
	 * @param gameId Game id
	 * @param load Class loader, may be null
	 */
	public void setGameClassLoader(int gameId, ClassLoader load);
	
	/**
	 * This returns the registered class loader for a given game.
	 * 
	 * @param gameId Game id
	 * @return Class load, or null if not found
	 */
	public ClassLoader getGameClassLoader(int gameId);

	
	/**
	 * Given a meta data object, create a new table data representation. The
	 * meta data has no particular constraints. The number of seats must be given.
	 * 
	 * @param meta Mta data to use, must not be null
	 * @param numSeats Number of seats for the table
	 * @return A new table data representation, never null
	 */
	public TableData createTableData(InternalMetaData meta, int numSeats);

	
	/**
	 * Given a table data object, create a new table. The meta data 
	 * can be a newly constructed data or a previously saved or serialized
	 * data object but must correspond to the factory data types.
	 * 
	 * @param data Data to wrap in table, must not be null
	 * @return A new table for the data, never null
	 */
	public E createTable(TableData data);

	
	/**
	 * Given a table, unrap it's underlying data object. Please use
	 * this method carefully as playing with the data object is not 
	 * recomended.
	 * 
	 * @param table Table to unwrap, must not be null
	 * @return The table data, never null
	 */
	public TableData extractData(E table);

}