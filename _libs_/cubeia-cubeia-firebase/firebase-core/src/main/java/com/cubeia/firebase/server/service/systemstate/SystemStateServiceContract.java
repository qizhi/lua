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
package com.cubeia.firebase.server.service.systemstate;

import java.util.Map;
import java.util.Set;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;

/**
 * The System State provides the user with system global state information.
 * The data contained will be lighweight representation of the objects 
 * in the system and will not be eligable for manipulation (I.e. modifying the
 * object information will not modify the object itself).
 * 
 * The interface method are fairly specific at the moment. 
 * 
 * @author Fredrik
 */
public interface SystemStateServiceContract extends Contract {
	
	/** Delimiter */
	public static final String DELIMITER = "/";
	
	/** The absolute root node */
	public static final String ROOT_FQN = "/";
	/** Node that holds all tables as children */
	// public static final String TABLE_ROOT_FQN = ROOT_FQN+"table/";
	/** Node that holds all chat channels as children */
	// public static final String CHAT_CHANNEL_ROOT_FQN = ROOT_FQN+"chat/";
	
	
	/**
	 * Hmmm... we need to expose the underlying cache handler, since some 
	 * services (lobby *cough*) need to register as listeners.
	 * 
	 * We could of course wrap the cache and the listener in our own 
	 * implementations, but at this point it seems like we would wrap 
	 * everything including the kitchen sink for the handler.
	 * 
	 * So, we use the TreeCache-specific implementation for now, and
	 * should the need arise, or we get a heck of a lot of time to kill,
	 * we can implementat our own wrappers here so we can plug in and out
	 * the underlying cache implementation.
	 * 
	 * @param listener
	 */
	public SystemStateCacheHandler getCacheHandler();
	
	/**
	 * Prints all contained information to log.
	 * Debug only, this can be *extremely* verbose.
	 *
	 */
	// public void dumpInfo();
	
	
	
	/**
	 * @param fqn
	 * @return true if the node exists
	 */
	public boolean exists(String fqn);
	
	/**
	 * Update a lobby attribute synchronously. This method is equivalent of
	 * {@link #setAttribute(String, String, Object, boolean)} with the last parameter
	 * set to false.
	 */
	public void setAttribute(String fqn, String attribute, Object value);
	
	/**
	 * Updates a single attribute in the data model.
	 * 
	 * FQN stands for Fully Qualified Name, full path to the object being updated
	 * E.g. updating name on table with id 1: 
	 * 
	 * FQN = /table/1
	 * attribute = TableAttributes.name
	 * value = "New Name"
	 * 
	 * 
	 * @param fql, path to the object being updated
	 * @param attribute, name of the attribute
	 * @param value, the value of the attribute
	 * @param doAsynch Update asynchronously
	 */
	public void setAttribute(String fqn, String attribute, Object value, boolean doAsynch);
	
	
	/**
	 * Update a lobby attributes synchronously. This method is equivalent of
	 * {@link #setAttributes(String, Map, boolean)} with the last parameter set 
	 * to false.
	 */
	public void setAttributes(String fqn, Map<String, Object> attributes);
	
	
	/**
	 * Updates all attributes for a node in the data model.
	 * 
	 * FQN stands for Fully Qualified Name, full path to the object being updated
	 * E.g. updating name on table with id 1: 
	 * 
	 * FQN = /table/1
	 * attribute = TableAttributes.name
	 * value = "New Name"
	 * 
	 * @param fql, path to the object being updated
	 * @param attributes, all attributes mapped to name
	 * @param doAsynch Update asynchronously
	 */
	public void setAttributes(String fqn, Map<String, Object> attributes, boolean doAsynch);
	
	
	/**
	 * @param fqn Node name, must not be null
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, may be null
	 */
	public Object getAttribute(String fqn, String attribute);
	
	
	/**
	 * Get the node data associated to the given FQN. If node not found 
	 * an empty Map will be returned. If found a defensive copy will be returned.
	 * 
	 * @param fqn Node name, must not be null
	 * @return A defensive copy of the node attributes, or an empty map
	 */
	public Map<Object,Object> getAttributes(String fqn);
	
	
	/**
	 * Get all end nodes (leaves) in the tree for the given FQN.
	 * 
	 * Example:
	 * Cache:
	 *   /a/b/c
	 *   /a/b/d
	 *   
	 * Input address:
	 *   /a/b 
	 *   
	 * Result:
	 *   /a/b/c and /a/b/d
	 * 
	 * 
	 * @param fqn
	 * @return List of FQN's
	 */
	public Set<String> getEndNodes(String address);
    
    /**
     * Gets the children of a node.
     * 
     * Example:
     * Cache:
     *   /a/b/c
     *   /a/b/d
     *   
     * Input address:
     *   /a
     *   
     * Result:
     *   b
     * @param fqn
     * @return
     */
    public Set<String> getChildren(String fqn);

	
	/**
	 * @param fqn Path of node to remove, never null
	 */
	public void removeNode(String fqn);
		
	
	/**
	 * @param fqn Path of node to checkmove, never null
	 * @return True if the node exists, false otherwise
	 */
	public boolean hasNode(String fqn);

	
	/**
	 * @param fqn Path to remove attribute from, neve rnull
	 * @param attr Attribute name to remove, never null
	 */
	public void removeAttribute(String fqn, String attr);
	
	/**
	 * Print all data contained in the system state to the log
	 * as DEBUG.
	 *
	 */
	public void printAllData();

	/*
	 * Create a node with no data in it.
	 * 
	 * @param string
	 */
	// public void addNode(String string);
}
