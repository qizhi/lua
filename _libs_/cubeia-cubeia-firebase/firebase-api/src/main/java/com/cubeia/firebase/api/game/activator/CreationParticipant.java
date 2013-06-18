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

import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.lobby.LobbyPath;

/**
 * This is a participant interface for table creation. This interface
 * can be supplied by activators to define lobby attributes and initial table
 * data.
 * 
 * @author lars.j.nilsson
 */
public interface CreationParticipant { 
	
	/**
	 * A table will always reside in the lobby as a descendant of the
	 * path "/table/&lt;gameId&gt;/". This method is used to determine exactly 
	 * where under the fixed prefix the table should be located. For example,
	 * the default implementation in Firebase simply returns the table id in
	 * string form, thus fixing the table in the lobby at path: 
	 * "/table/&lt;gameId&gt;/&lt;tableId&gt;".
	 * 
	 * @param table The table to create a path for, never null
	 * @return The LobbyPath object for the table. Never null
	 */
	public LobbyPath getLobbyPathForTable(Table table);
	
	
	/**
	 * This method is called when the table has been created. It gives the
	 * activator a chance to (1) set initial table data; and (2) modify the
	 * table attributes as they appear in the lobby.
	 * 
	 * @param table Table to set initial attributes for, never null
	 * @param acc Accessor to use for setting the attributes, never null
	 */
	public void tableCreated(Table table, LobbyTableAttributeAccessor acc);

	
	/**
	 * Return the name to use for this table. Override this to provide 
	 * you own table name scheme.
	 * 
	 * @param def Game definition the table belongs to, never null
	 * @param t The actual table, never null
	 * @return The name for the table, or null
	 */
	public String getTableName(GameDefinition def, Table table);

}
