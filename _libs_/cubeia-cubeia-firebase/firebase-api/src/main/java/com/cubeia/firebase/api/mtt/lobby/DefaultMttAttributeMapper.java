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
package com.cubeia.firebase.api.mtt.lobby;

import java.util.Map;
import java.util.TreeMap;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.lobby.LobbyAccessor;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.util.Arguments;
 
/**
 * This is the default implementation used by the internal Firebase classes
 * to map a table into a set of attributes. It can be used and extended by games
 * that accesses the lobby themselves.
 * 
 * @author lars.j.nilsson
 */
public class DefaultMttAttributeMapper implements MttAttributeMapper {
	
	/**
	 * @param acc Accessor for the tournament, must not be null
	 * @param b Values to set for the "_READY" attribute
	 */
	public static void setReady(LobbyAttributeAccessor acc, boolean b) {
		acc.setStringAttribute(DefaultMttAttributes._READY.toString(), String.valueOf(b));
	}
	
	/**
	 * @param acc Lobby accessor, never null
	 * @param path Path to check, never null
	 * @return True if the path is a tournament, false otherwise
	 */
	public static boolean isMtt(LobbyAccessor acc, LobbyPath path) {
		AttributeValue att = acc.getAttribute(path, NODE_TYPE_ATTRIBUTE_NAME);
		return (att == null ? false : att.data.equals(MTT_NODE_TYPE_ATTRIBUTE_VALUE));
	}
	
	/**
	 * @param acc Attribute accessor for the tournament to be updated, never null
	 */
	public static void updateLastModified(LobbyAttributeAccessor acc) {
		acc.setAttribute(DefaultMttAttributes._LAST_MODIFIED.toString(), new AttributeValue(String.valueOf(System.currentTimeMillis())));
	}
	
	/**
	 * <p>This method sets the required attributes which are need by Firebase.
	 * All internal values start with underscore, e.g. _ID.</p>
	 * 
	 * <p>
	 * Internal Values:
	 * <ul>
	 *  <li>_nodeType : Node type, will be 'mtt'</li>
	 * 	<li>_ID : ID of the tournament instance</li>
	 *  <li>_TOURNAMENT_ID : ID of the tournament logic</li>
	 *  <li>_LAST_MODIFIED : Time of last modification of the state</li>
	 *  <li>_CAPACITY : Tournament player limit</li>
	 *  <li>_SEATED : Number of registered players</li>
	 *  <li></li>
	 *  <li></li>
	 *  <li></li>
	 * </ul>
	 * @param table Table to take values from, never null
	 * @param acc Lobby accessor, never null
	 */
	public static final void setRequiredValues(MTTState mtt, LobbyAttributeAccessor acc) {
		acc.setAttribute(DefaultMttAttributes._ID.toString(), new AttributeValue(mtt.getId()));
		acc.setAttribute(DefaultMttAttributes.TOURNAMENT_ID.toString(), new AttributeValue(mtt.getMttLogicId()));
		acc.setAttribute(DefaultMttAttributes._LAST_MODIFIED.toString(), new AttributeValue(String.valueOf(System.currentTimeMillis())));
		acc.setAttribute(DefaultMttAttributes.NAME.toString(), new AttributeValue(mtt.getName()));
		acc.setAttribute(DefaultMttAttributes.CAPACITY.toString(), new AttributeValue(mtt.getCapacity()));
		acc.setAttribute(DefaultMttAttributes.REGISTERED.toString(), new AttributeValue(mtt.getRegisteredPlayersCount()));
		acc.setAttribute(DefaultMttAttributes.ACTIVE_PLAYERS.toString(), new AttributeValue(mtt.getRemainingPlayerCount()));
		//acc.setAttribute(DefaultMttAttributes.STATUS.toString(), new AttributeValue(mtt.getTournamentLobbyStatus().toString()));
		acc.setAttribute(NODE_TYPE_ATTRIBUTE_NAME, new AttributeValue(MTT_NODE_TYPE_ATTRIBUTE_VALUE));
	}

	/**
	 * This method maps all {@link DefaultMtteAttributes} to values taken
	 * from the parameter table. The enum toString method will be used to generate 
	 * the attribute names.
	 * 
	 * @param table Table to map, must not be null
	 */
	public Map<String, AttributeValue> toMap(MTTState logic) {
		Arguments.notNull(logic, "logic");
		Map<String, AttributeValue> map = new TreeMap<String, AttributeValue>();
		map.put(DefaultMttAttributes._ID.toString(), new AttributeValue(logic.getId()));
		map.put(DefaultMttAttributes.TOURNAMENT_ID.toString(), new AttributeValue(logic.getMttLogicId()));
		map.put(DefaultMttAttributes.NAME.toString(), new AttributeValue(logic.getName()));
		map.put(DefaultMttAttributes.CAPACITY.toString(), new AttributeValue(logic.getCapacity()));
		map.put(DefaultMttAttributes.REGISTERED.toString(), new AttributeValue(logic.getRegisteredPlayersCount()));
		map.put(DefaultMttAttributes.ACTIVE_PLAYERS.toString(), new AttributeValue(logic.getRemainingPlayerCount()));
		map.put(DefaultMttAttributes._LAST_MODIFIED.toString(), new AttributeValue(String.valueOf(System.currentTimeMillis())));
		map.put(NODE_TYPE_ATTRIBUTE_NAME, new AttributeValue(MTT_NODE_TYPE_ATTRIBUTE_VALUE));
		return map;
	}
}