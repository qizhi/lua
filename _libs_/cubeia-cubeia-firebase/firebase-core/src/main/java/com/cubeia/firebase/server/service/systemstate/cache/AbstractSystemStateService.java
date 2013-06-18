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
package com.cubeia.firebase.server.service.systemstate.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public abstract class AbstractSystemStateService implements SystemStateServiceContract, Service {

	/** The logger */
	private Logger log = Logger.getLogger(getClass());
	
	/** The underlying cache implementation */
	protected SystemStateCacheHandler cacheHandler;
	
	public void destroy() {
		cacheHandler = null;
	}

	public void init(ServiceContext con) throws SystemException {
		cacheHandler = createSystemStateHandler(con);
	}

	protected abstract SystemStateCacheHandler createSystemStateHandler(ServiceContext con);

	public void start() {
		cacheHandler.start();
	}

	public void stop() {
		cacheHandler.stop();
	}
	
	
	//-----------------------------------
	// 
	//  Direct cache references
	// 
	//-----------------------------------
	
	public SystemStateCacheHandler getCacheHandler() {
		return cacheHandler;
	}

	public boolean hasNode(String fqn) {
		try {
			return cacheHandler.hasNode(fqn);
		} catch (CacheException e) {
			log.error("Error on cache HAS for FQN["+fqn+"]", e);
			return false;
		}
	}
	
	public void removeAttribute(String fqn, String attr) {
		try {
			cacheHandler.removeAttribute(fqn, attr);
		} catch (CacheException e) {
			log.error("Error on cache REMOVE for FQN["+fqn+"] Attrib["+attr+"]", e);
		}
	}
	
	public void removeNode(String fqn) {
		try {
			cacheHandler.removeNode(fqn);
		} catch (CacheException e) {
			log.error("Error on cache REMOVE for FQN["+fqn+"]", e);
		}
	}
	
	public void setAttribute(String fqn, String attribute, Object value, boolean doAsynch) {
		try {
			cacheHandler.updateAttribute(fqn, attribute, value, doAsynch);
		} catch (CacheException e) {
			log.error("Error on cache PUT for FQN["+fqn+"] Attrib["+attribute+"] Value["+value+"]", e);
		}
	}
	
	public void setAttribute(String fqn, String attribute, Object value) {
		setAttribute(fqn, attribute, value, false);
	}
	
	public void setAttributes(String fqn, Map<String, Object> attributes) {
		setAttributes(fqn, attributes, false);
	}
	
	public void setAttributes(String fqn, Map<String, Object> attributes, boolean doAsynch) {
		try {
			cacheHandler.updateAttributes(fqn, attributes, doAsynch);
		} catch (CacheException e) {
			log.error("Error on cache PUT for FQN["+fqn+"] Attributes["+attributes+"]", e);
		}
	}
	
	public Object getAttribute(String fqn, String attribute) {
		try {
			return cacheHandler.getAttribute(fqn, attribute);
		} catch (CacheException e) {
			log.error("Error on cache GET for FQN["+fqn+"] Attrib["+attribute+"]", e);
			return null;
		}
	}

	
	public Map<Object,Object> getAttributes(String fqn) {
	   return cacheHandler.getNodeData(fqn);
	}
	
	public Set<String> getEndNodes(String address) {
		Set<String> set = new HashSet<String>();
		Set<Fqn<?>> fqns = cacheHandler.getEndNodes(address).keySet();
		for (Fqn<?> fqn : fqns) {
			set.add(fqn.toString());
		}
		return set;
	}
	
	
	public boolean exists(String fqn) {
		return cacheHandler.exists(fqn);
	}
	
	
	public void printAllData() {
		cacheHandler.dumpInfo();
	}

    public Set<String> getChildren(String fqn) {
        return cacheHandler.getChildren(fqn);
    }

	/*void addNode(String fqn) {
		cacheHandler.addNode(fqn);
	}*/


	//-----------------------------------
	// 
	//  Convinience methods
	// 
	//-----------------------------------
	
	/**
	 * Returns all table.
	 * 
	 */
	/*public Collection<TableInfo> getTables() {
		Collection<TableInfo> objects = cacheHandler.getTables(TABLE_ROOT_FQN);
		return objects;
	}
	
	public TableInfo getTable(int tableId) {
		try {
			return cacheHandler.getTable(TABLE_ROOT_FQN+tableId);
		} catch (CacheException e) {
			log.warn("Failed to get Table Info from system state", e);
			return null;
		}
	}*/

	/**
	 * Adds a table to the system state universe. 
	 * Call this when a table is created.
	 * 
	 */
	/*public void addTable(Table table, String fqn) {
		fqn = formatFqn(fqn);
		log.debug("Add Table: "+table+" at "+fqn);
		
		// Create a TableInfo for the given table
		TableInfo tableInfo = new TableInfo(table);
		
		fqn = TABLE_ROOT_FQN+fqn+table.getId();
		setAttribute(fqn, TableAttributes.ID.toString(), tableInfo.getId());
		setAttribute(fqn, TableAttributes.NAME.toString(), tableInfo.getName());
		setAttribute(fqn, TableAttributes.CAPACITY.toString(), tableInfo.getCapacity());
		setAttribute(fqn, TableAttributes.SEATED.toString(), tableInfo.getSeated());
		setAttribute(fqn, TableAttributes.GAMEID.toString(), tableInfo.getGameId());
		
	}*/
	
	

	/**
	 * Updates runtime attributes for the given table.
	 * Currently the runtime updated attributes are:
	 * 	- SEATED (seated player count)
	 * 
	 */
	/*public void updateTable(Table table) {
		log.debug("Update Table: "+table);
		String fqn = TABLE_ROOT_FQN+table.getId();
		setAttribute(fqn, TableAttributes.SEATED.toString(), table.getPlayerCount());
	}*/

	
	/* ------- TABLE SPECIFIC CODE ENDS HERE ----- */
	
	/**
	 * Removes leading separator and appends trailing
	 * separator (if missing).
	 * 
	 * @param fqn
	 * @return
	 */
	/*private String formatFqn(String fqn) {
		if (fqn.startsWith(Fqn.SEPARATOR)) {
			fqn = fqn.substring(1);
		}
		
		if (!fqn.endsWith(Fqn.SEPARATOR)) {
			fqn += Fqn.SEPARATOR;
		}
		
		return fqn;
	}*/	
}
