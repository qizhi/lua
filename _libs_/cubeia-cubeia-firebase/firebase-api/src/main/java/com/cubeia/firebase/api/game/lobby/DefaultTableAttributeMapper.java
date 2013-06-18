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
package com.cubeia.firebase.api.game.lobby;

import java.util.Map;
import java.util.TreeMap;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableMetaData;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableSeatingMap;
import com.cubeia.firebase.api.game.table.TableWatcherSet;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.util.Arguments;
 
/**
 * This is the default implementation used by the internal Firebase classes
 * to map a table into a set of attributes. It can be used and extended by games
 * that accesses the lobby themselves.
 * 
 * @author lars.j.nilsson
 */
public class DefaultTableAttributeMapper implements TableAttributeMapper {
	
	/**
	 * @param acc Lobby accessor, never null
	 * @param path Path to check, never null
	 * @return True if the path is a table, false otherwise
	 */
	public static boolean isTable(LobbyTableAccessor acc, LobbyPath path) {
		AttributeValue att = acc.getAttribute(path, NODE_TYPE_ATTRIBUTE_NAME);
		return (att == null ? false : att.data.equals(TABLE_NODE_TYPE_ATTRIBUTE_VALUE));
	}
	
	/**
	 * @param acc Accessor for a table, must not be null
	 * @return The MTT id the table belongs to, or -1 if not found
	 */
	public static int getMttId(LobbyTableAttributeAccessor acc) {
		AttributeValue att = acc.getAttribute(DefaultTableAttributes._MTT_ID.toString());
		return (att == null ? -1 : Integer.parseInt(att.toString()));
	}
	
	/**
	 * @param acc Attribute accessor for table to be update, never null
	 */
	public static void updateLastModified(LobbyTableAttributeAccessor acc) {
		acc.setAttribute(DefaultTableAttributes._LAST_MODIFIED.toString(), new AttributeValue(String.valueOf(System.currentTimeMillis())));
	}
	
	/**
	 * This method sets the required table id and node type attributes which
	 * are used internally by Firebase.
	 * 
	 * @param table Table to take values from, never null
	 * @param acc Lobby accessor, never null
	 */
	public static final void setRequiredValues(Table table, LobbyTableAttributeAccessor acc) {
		acc.setAttribute(DefaultTableAttributes._ID.toString(), new AttributeValue(table.getId()));
		acc.setAttribute(DefaultTableAttributes._LAST_MODIFIED.toString(), new AttributeValue(String.valueOf(System.currentTimeMillis())));
		acc.setAttribute(NODE_TYPE_ATTRIBUTE_NAME, new AttributeValue(TABLE_NODE_TYPE_ATTRIBUTE_VALUE));
		int mttId = table.getMetaData().getMttId();
		if(mttId != -1) {
			acc.setAttribute(DefaultTableAttributes._MTT_ID.toString(), new AttributeValue(mttId));
		}
	}

	/**
	 * Maps all {@link DefaultTableAttributes} to values taken
	 * from the parameter table. The <code>toString</code> method will be used to generate 
	 * the attribute names.
	 * 
	 * @param table Table to map, must not be null
	 */
	public Map<String, AttributeValue> toMap(Table table) {
		Arguments.notNull(table, "table");
		TableMetaData meta = table.getMetaData();
		TablePlayerSet plyrs = table.getPlayerSet();
		TableWatcherSet watchers = table.getWatcherSet();
		TableSeatingMap seating = plyrs.getSeatingMap();
		Map<String, AttributeValue> map = new TreeMap<String, AttributeValue>();
		map.put(DefaultTableAttributes._ID.toString(), new AttributeValue(table.getId()));
		map.put(DefaultTableAttributes._NAME.toString(), new AttributeValue(meta.getName()));
		map.put(DefaultTableAttributes._CAPACITY.toString(), new AttributeValue(seating.getNumberOfSeats()));
		map.put(DefaultTableAttributes._SEATED.toString(), new AttributeValue(seating.countSeatedPlayers()));
		map.put(DefaultTableAttributes._WATCHERS.toString(), new AttributeValue(watchers.getCountWatchers()));
		map.put(DefaultTableAttributes._GAMEID.toString(), new AttributeValue(meta.getGameId()));
		map.put(DefaultTableAttributes._LAST_MODIFIED.toString(), new AttributeValue(String.valueOf(System.currentTimeMillis())));
		map.put(NODE_TYPE_ATTRIBUTE_NAME, new AttributeValue(TABLE_NODE_TYPE_ATTRIBUTE_VALUE));
		int mttId = table.getMetaData().getMttId();
		if(mttId != -1) {
			map.put(DefaultTableAttributes._MTT_ID.toString(), new AttributeValue(mttId));
		}
		return map;
	}
}