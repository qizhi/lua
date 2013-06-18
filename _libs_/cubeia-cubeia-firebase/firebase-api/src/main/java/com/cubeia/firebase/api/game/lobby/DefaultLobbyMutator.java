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
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.util.Arguments;

/**
 * This is the default lobby mutator used by the internal Firebase
 * system to update the lobby on changes. This class can be used by 
 * game that want to extend the information provided by default by 
 * Firebase but not want to write it all themselves.
 * 
 * <p>This mutator uses the table id, directly under the accessor root
 * path, e.g. "/table/&lt;gameId&gt;/&lt;tableId&gt;". Subclasses may
 * change this by overriding the method {@link #getTablePath(Table)].
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 27
 */
public class DefaultLobbyMutator {

	private final TableAttributeMapper mapper;

	
	/**
	 * @param mapper Mapper to use, must not be null
	 */
	public DefaultLobbyMutator(TableAttributeMapper mapper) {
		Arguments.notNull(mapper, "mapper");
		this.mapper = mapper; 
	}
	
	/**
	 * Create a new lobby mutator using a {@link DefaultTableAttributeMapper default}
	 * attribute mapper.
	 */
	public DefaultLobbyMutator() {
		this(new DefaultTableAttributeMapper());
	}
	
	
	/**
	 * Given an accessor and a table, populate the lobby data accordingly. 
	 * This method should (1) calculate the table node path; (2) map the table
	 * into attributes; and (3) set the attributes in the accessor.
	 * 
	 * <p>If a table already exists in the lobby with the same id, this method 
	 * will clear all attributes of the old table and set the new table attributes 
	 * instead.
	 * 
	 * @param acc Accessor for the lobby, never null
	 * @param table Table to set in lobby, never null
	 */
	public void addTable(LobbyTableAccessor acc, Table table) { 
		checkArgs(acc, table);
		LobbyPath path = getTablePath(table);
		if(acc.hasNode(path)) acc.clearNode(path); // CLEAR OLD
		Map<String, AttributeValue> data = mapper.toMap(table);
		for (String key : data.keySet()) {
			acc.setAttribute(path, key, data.get(key));
		}
	}

	
	/**
	 * Given an accessor and a table, remove the lobby data accordingly. 
	 * 
	 * @param acc Accessor for the lobby, never null
	 * @param table Table to set in lobby, never null
	 */
	public void removeTable(LobbyTableAccessor acc, Table table) { 
		checkArgs(acc, table);
		LobbyPath path = getTablePath(table);
		acc.removeNode(path);
	}
	
	
	/**
	 * Given an accessor and a table, remove the lobby data accordingly. This
	 * default implementation does not remove attributes from the old node even
	 * if they are not re-produced by the table mapper.
	 * 
	 * <p>If the data type of an attribute is changed, it will first be removed, then
	 * re-added as a new attribute.
	 * 
	 * @param acc Accessor for the lobby, never null
	 * @param table Table to set in lobby, never null
	 */
	public void updateTable(LobbyTableAccessor acc, Table table) { 
		checkArgs(acc, table);
		LobbyPath path = getTablePath(table);
		Map<String, AttributeValue> oldVals = acc.getAllAttributes(path);
		Map<String, AttributeValue> newVals = mapper.toMap(table);
		Delta delta = calculateDelta(oldVals, newVals);
		doChanges(acc, path, delta, oldVals);
		doAdditions(acc, path, delta);
	}


	// --- PROTECTED METHODS --- //
	
	/**
	 * This method is used to return the path used for
	 * the table node in the lobby. The default implementation
	 * simply returns the table id as a string.
	 * 
	 * @param t Table to get path for, never null
	 * @return The table node path, never null
	 */
	protected LobbyPath getTablePath(Table t) {
		LobbyPath lobbyPath = t.getMetaData().getLobbyPath();
		return new LobbyPath(lobbyPath, t.getId());
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void doChanges(LobbyTableAccessor acc, LobbyPath path, Delta delta, Map<String, AttributeValue> oldVals) {
		for(String key : delta.changedValues.keySet()) {
			AttributeValue v = delta.changedValues.get(key);
			AttributeValue old = oldVals.get(key);
			if(v.type.equals(old.type)) acc.setAttribute(path, key, v);
			else {
				acc.removeAttribute(path, key);
				acc.setAttribute(path, key, v);
			}
		}
	}

	private void doAdditions(LobbyTableAccessor acc, LobbyPath path, Delta delta) {
		for(String key : delta.newValues.keySet()) {
			AttributeValue v = delta.newValues.get(key);
			acc.setAttribute(path, key, v);
		}
	}
	
	private void checkArgs(LobbyTableAccessor acc, Table table) {
		Arguments.notNull(acc, "acc");
		Arguments.notNull(table, "table");
	}
	
	private Delta calculateDelta(Map<String, AttributeValue> oldVals, Map<String, AttributeValue> newVals) {
		Delta d = new Delta();
		populateChanged(d, oldVals, newVals);
		populateNew(d, oldVals, newVals);
		return d;
	}
	
	private void populateNew(Delta d, Map<String, AttributeValue> oldVals, Map<String, AttributeValue> newVals) {
		for(String key : newVals.keySet()) {
			if(!oldVals.containsKey(key)) {
				AttributeValue nv = newVals.get(key);
				d.newValues.put(key, nv);
			}
		}
	}

	private void populateChanged(Delta d, Map<String, AttributeValue> oldVals, Map<String, AttributeValue> newVals) {
		for(String key : newVals.keySet()) {
			if(oldVals.containsKey(key)) {
				AttributeValue ov = oldVals.get(key);
				AttributeValue nv = newVals.get(key);
				if(!ov.equals(nv)) {
					d.changedValues.put(key, nv);
				}
			}
		}
	}


	// --- PRIVATE CLASSES --- //

	private static class Delta {
		
		private Map<String, AttributeValue> newValues = new TreeMap<String, AttributeValue>();
		private Map<String, AttributeValue> changedValues = new TreeMap<String, AttributeValue>();
		
	}
}