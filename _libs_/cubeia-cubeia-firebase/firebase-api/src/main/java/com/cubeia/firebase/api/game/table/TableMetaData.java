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
package com.cubeia.firebase.api.game.table;

import com.cubeia.firebase.api.lobby.LobbyPath;

/**
 * This interface contains the meta data of a table. The meta data
 * is static and will never change for a particular table instance.
 * 
 * @author Lars J. Nilsson
 */
public interface TableMetaData {

    /**
     * Gets the game id for this table. The game id is the id of the game being
     * played at this table.
     * 
     * @return The id of the game associated with this table
     */
	public int getGameId();
	
    /**
     * Gets the name of this table. The name is set when the table
     * is created. This will never be null.
     * 
     * @return The name of the table, never null
     */
	public String getName();
	
	/**
	 * This method returns the lobby path of the table. This
	 * details where in the lobby data structure the table will
	 * be created.
	 * 
	 * @return The table lobby path, never null
	 */
	public LobbyPath getLobbyPath();

	/**
	 * Returns the type of this table. 
	 * @return The table type.
	 */
	public TableType getType();
	
	/**
	 * Returns the MTT id if applicable.
	 * @return the mtt id or -1 it this is not a tournament table.
	 * TODO: this property should not be in the general table meta data
	 */
	public int getMttId();
	
}
