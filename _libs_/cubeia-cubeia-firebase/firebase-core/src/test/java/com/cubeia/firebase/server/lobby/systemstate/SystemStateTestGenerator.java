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
package com.cubeia.firebase.server.lobby.systemstate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.lobby.AttributeMapper;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributes;

public class SystemStateTestGenerator {
	
	/**
	 * Creates a map with data representing a table.
	 * The following attributes are set:
	 * 
	 * _nodeType : table
	 * _ID : [id]
	 * _GAMEID : 99
	 * _NAME : [name]
	 * _CAPACITY : 0
	 * _SEATED : 0
	 * _WATCHERS : 0
	 * 
	 * @param tableId
	 * @return
	 */
	public static Map<String, Object> createTableAttributes(int tableId, String name) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		
		attributes.put(AttributeMapper.NODE_TYPE_ATTRIBUTE_NAME, AttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE);
		attributes.put(DefaultTableAttributes._ID.name(), tableId);
		attributes.put(DefaultTableAttributes._GAMEID.name(), 99);
		attributes.put(DefaultTableAttributes._NAME.name(), name);
		attributes.put(DefaultTableAttributes._CAPACITY.name(), 0);
		attributes.put(DefaultTableAttributes._SEATED.name(), 0);
		attributes.put(DefaultTableAttributes._WATCHERS.name(), 0);
		
		return attributes;
		
	}
	
	
	/**
	 * Creates a map with data representing a tournament.
	 * The following attributes are set:
	 * 
	 * _nodeType : mtt
	 * _ID : [id]
	 * 
	 * @param mttId
	 * @return
	 */
	public static Map<String, Object> createTournamentAttributes(int mttId) {
		Map<String, Object> attributes = new LinkedHashMap<String, Object>();
		
		attributes.put(AttributeMapper.NODE_TYPE_ATTRIBUTE_NAME, AttributeMapper.MTT_NODE_TYPE_ATTRIBUTE_VALUE);
		attributes.put(DefaultMttAttributes._ID.name(), mttId);
		attributes.put(DefaultMttAttributes.TOURNAMENT_ID.name(), 99);
		attributes.put(DefaultMttAttributes.ACTIVE_PLAYERS.name(), 0);
		attributes.put(DefaultMttAttributes.CAPACITY.name(), 0);
		attributes.put(DefaultMttAttributes.REGISTERED.name(), 100);
		attributes.put(DefaultMttAttributes._READY.name(), "true");
		
		return attributes;
		
	}
	
}
