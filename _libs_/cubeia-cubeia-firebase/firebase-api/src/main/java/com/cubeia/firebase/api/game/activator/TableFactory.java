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
package com.cubeia.firebase.api.game.activator;

import com.cubeia.firebase.api.game.lobby.LobbyTable;
import com.cubeia.firebase.api.game.lobby.LobbyTableFilter;
import com.cubeia.firebase.api.lobby.LobbyPath;

/**
 * This is the access interface published by the platform to game activators
 * in order to enable them to query the current state of the system and manage
 * the tables.
 * 
 * @author lars.j.nilsson
 */
public interface TableFactory {

	/**
	 * Create a new table and return its lobby representation. The second argument
	 * must be supplied of the activator wishes to set default lobby attributes, 
	 * modify the state of the tables or define where in the lobby the table 
	 * should be located.
	 * 
	 * <p>If the activator is representing a game which is an adaption of
	 * {@link GameSupport} the game will be instantiated and set as the state
	 * of the table. For this reason it is possible for this method to fail
	 * when instantiating the game, in which case it returns null.
	 * 
	 * @param seats Number of seats at the table
	 * @param participant Creation participant, may be null
	 * @return The lobby representation of the table, or null if failed
	 */
	public LobbyTable createTable(int seats, CreationParticipant participant);
	
	
	/**
	 * Create new tables and return their lobby representation. This method is
	 * equivalent with {@link #createTable(int, CreationParticipant)} but creates
	 * multiple tables instead of only one. 
	 * 
	 * <p>As a result the given creation participant will be called sequentially,
	 * once per new table.
	 * 
	 * @param tables Number of tables to create, must be positive
	 * @param seats Number of seats at the table, must be positive
	 * @param participant Creation participant, may be null
	 * @return The lobby representation of the tables, never null
	 */
	public LobbyTable[] createTables(int tables, int seats, CreationParticipant participant);
	
	
	/**
	 * This method destroys a table in the system. It can be used to remove
	 * old tables. If the second argument is false the platform will fail the
	 * request if any players are seated at the table. 
	 * 
	 * <p><b>NB:</b> This method may be slow, please use {@link #destroyTable(LobbyTable, boolean)}
	 * whenever possible.
	 * 
	 * @param tableid Id of the table to remove
	 * @param force True if the table should always be destroyed, false to check for seated players
	 * @return True if the table was destroyed, false otherwise
	 */
	public boolean destroyTable(int tableid, boolean force);
	
	
	/**
	 * This method destroys a table in the system. It can be used to remove
	 * old tables. If the second argument is false the platform will fail the
	 * request if any players are seated at the table.
	 * 
	 * @param table Table to remove, must not be null
	 * @param force True if the table should always be destroyed, false to check for seated players
	 * @return True if the table was destroyed, false otherwise
	 */
	public boolean destroyTable(LobbyTable table, boolean force);
	
	/**
	 * Get a list of all tables. This method is most likely rather slow and
	 * should be used sparingly by the activator.
	 * 
	 * @return An array of lobby tables, never null
	 */
	public LobbyTable[] listTables();
	
	/**
	 * Get a list of all tables. This method is most likely rather slow and
	 * should be used sparingly by the activator.
	 * 
	 * @param filt Filter to use, null for all tables
	 * @return An array of lobby tables, never null
	 */
	public LobbyTable[] listTables(LobbyTableFilter filt);
	
	/**
	 * Get a list of all tables under a specified path. 
	 * This method is most likely rather slow and
	 * should be used sparingly by the activator.
	 * 
	 * @param fqn Coordinate in the lobby tree, e.g. "a/b/c"
	 * @return An array of lobby tables, never null
	 */
	public LobbyTable[] listTables(LobbyPath path);
	
	/**
	 * Get a list of all tables under a specified path and
	 * with a set filter. This method is most likely rather slow and
	 * should be used sparingly by the activator.
	 * 
	 * @param fqn Coordinate in the lobby tree, e.g. "a/b/c"
	 * @param filt Filter to use, null for all tables
	 * @return An array of lobby tables, never null
	 */
	public LobbyTable[] listTables(LobbyPath path, LobbyTableFilter filt);
	
}
