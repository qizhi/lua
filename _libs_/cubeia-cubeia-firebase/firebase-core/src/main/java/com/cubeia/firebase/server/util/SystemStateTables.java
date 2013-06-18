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
package com.cubeia.firebase.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;

import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.server.lobby.model.TableInfo;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

/**
 * Small util class for the default firebase table operations on a 
 * system state service.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 27
 */
public class SystemStateTables {

	// --- INSTANCE MEMBERS --- //
	
	private final Logger log = Logger.getLogger(getClass());
	private final SystemStateServiceContract state;
	
	public SystemStateTables(SystemStateServiceContract state) {
		Arguments.notNull(state, "stats");
		this.state = state; 
	}
	
	public void updateTable(FirebaseTable table) {
		log.debug("Update Table: "+table);
		String fqn = SystemStateConstants.TABLE_ROOT_FQN+table.getId();
		state.setAttribute(fqn, DefaultTableAttributes._SEATED.toString(), table.getPlayerSet().getPlayerCount());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<TableInfo> getTables() {
		// FIXME: Remove cache ref here!
		Cache cache = state.getCacheHandler().getCache();
		Collection<TableInfo> snapshot = new ArrayList<TableInfo>();
		try {
			Node tableRoot = cache.getRoot().getChild(Fqn.fromString(SystemStateConstants.TABLE_ROOT_FQN));
			// Iterate all table nodes
			for (Object child : tableRoot.getChildren()) {
				Node tableNode = (Node)child;
				Map<Object,Object> data = tableNode.getData();

				TableInfo table = createTableInfoFromMap(data);

				snapshot.add(table);

			}
		} catch (CacheException ex) {
			log.error("Error on cache SNAPSHOT", ex);
		}
		return snapshot;
	}

	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TableInfo getTable(int tableId) {
		String fqn = SystemStateConstants.TABLE_ROOT_FQN + tableId;
		// FIXME: Remove tree cache ref here!
		Cache cache = state.getCacheHandler().getCache();
		try {
			Node tableNode = cache.getRoot().getChild(fqn);
			if(tableNode == null) return null; // SANITY CHECK
			Map<Object,Object> data = tableNode.getData();
			TableInfo table = createTableInfoFromMap(data);
			
			return table;
		} catch (CacheException e) {
			log.warn("Failed to get Table Info from system state", e);
			return null;
		}
	}

	public void addTable(FirebaseTable table) {
		String fqn = SystemStateConstants.TABLE_ROOT_FQN+table.getId();
		createTableInfo(table, fqn);
	}
	
	public void addTable(FirebaseTable table, String path) {
		path = formatFqn(path);
		String fqn = SystemStateConstants.TABLE_ROOT_FQN+path+table.getId();
		createTableInfo(table, fqn);
	}

	
	
	// --- PRIVATE METHODS --- //
	
	private void createTableInfo(FirebaseTable table, String fqn) {
		// Create a TableInfo for the given table
		TableInfo tableInfo = new TableInfo(table);
		state.setAttribute(fqn, DefaultTableAttributes._ID.toString(), tableInfo.getId());
		state.setAttribute(fqn, DefaultTableAttributes._NAME.toString(), tableInfo.getName());
		state.setAttribute(fqn, DefaultTableAttributes._CAPACITY.toString(), tableInfo.getCapacity());
		state.setAttribute(fqn, DefaultTableAttributes._SEATED.toString(), tableInfo.getSeated());
		state.setAttribute(fqn, DefaultTableAttributes._WATCHERS.toString(), tableInfo.getWatching());
		state.setAttribute(fqn, DefaultTableAttributes._GAMEID.toString(), tableInfo.getGameId());
	}
	
	private TableInfo createTableInfoFromMap(Map<Object, Object> data) {
		TableInfo table = new TableInfo();
		table.setId((Integer)data.get(DefaultTableAttributes._ID.toString()));
		table.setName(data.get(DefaultTableAttributes._NAME.toString()).toString());
		table.setCapacity((Integer)data.get(DefaultTableAttributes._CAPACITY.toString()));
		table.setSeated((Integer)data.get(DefaultTableAttributes._SEATED.toString()));
		table.setWatching((Integer)data.get(DefaultTableAttributes._WATCHERS.toString()));
		table.setGameId((Integer)data.get(DefaultTableAttributes._GAMEID.toString()));
		return table;
	}
	
	private String formatFqn(String fqn) {
		if (fqn.startsWith(Fqn.SEPARATOR)) {
			fqn = fqn.substring(1);
		}
		
		if (!fqn.endsWith(Fqn.SEPARATOR)) {
			fqn += Fqn.SEPARATOR;
		}
		
		return fqn;
	}
}
