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

import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.GameDefinition;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributeMapper;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.lobby.TableAttributeMapper;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.util.Arguments;

/**
 * This is the default table creator participant. It creates tables
 * directly under the table id in the lobby and uses the specified attribute
 * mapper.
 * 
 * @author lars.j.nilsson
 */
public class DefaultCreationParticipant implements CreationParticipant {

	private final TableAttributeMapper mapper;

	public DefaultCreationParticipant() {
		this(new DefaultTableAttributeMapper());
	}
	
	/**
	 * @param mapper Mapper to use, must not be null
	 */
	public DefaultCreationParticipant(TableAttributeMapper mapper) {
		Arguments.notNull(mapper, "mapper");
		this.mapper = mapper;
	}

	/**
	 * <p>
	 * Return the lobby path for the table.
	 * The lobby path is without table root and table id.
	 * </p>
	 * I.e.<br/>
	 * <code>"/a/b"</code><br/> 
	 * will place the table in <br/> 
	 * <code>"/TABLE_ROOT/a/b/&lt;table.getId()&gt;"</code>
	 * 
	 */
	public LobbyPath getLobbyPathForTable(Table table) {
		LobbyPath path = new LobbyPath(table.getMetaData().getGameId(), "", table.getId());		
		return path;
	}

	/**
	 * Override this method to set your own state on the table.
	 */
	public void tableCreated(Table table, LobbyTableAttributeAccessor acc) {
		setTableAttributes(table, acc);
	}
	
	/**
	 * Update the distributed lobby data with the new table's attributes
	 */
	private void setTableAttributes(Table table, LobbyTableAttributeAccessor acc) {
		Map<String, AttributeValue> atts = mapper.toMap(table);
		for (String key : atts.keySet()) {
			AttributeValue val = atts.get(key);
			acc.setAttribute(key, val);
		}
	}

	/**
	 * This method returns the definition name (game name) appended
	 * with the table id in brackets.
	 */
	public String getTableName(GameDefinition def, Table t) {
		return def.getName() + "<" + t.getId() + ">";
	}
}
