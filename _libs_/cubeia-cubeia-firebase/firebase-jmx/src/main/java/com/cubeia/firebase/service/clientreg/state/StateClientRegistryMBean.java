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
package com.cubeia.firebase.service.clientreg.state;

import java.util.List;
import java.util.Map;

/**
 * JMX interface for the Client Registry.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface StateClientRegistryMBean {
    
	public int getNumberOfClients();
	
	/**
	 * Kick a player from the system. The player will be forcibly
	 * logged out and the socket will be closed.
	 * 
	 * @param playerId
	 */
	public void kickPlayer(int playerId);
	
	/**
	 * Kick a player from a specific table only.
	 * The client will not be logged out or disconnected.
	 * 
	 * @param playerId
	 * @param tableId
	 */
	public void kickPlayerFromTable(int playerId, int tableId);
	
	/**
	 * Get a textual representation of a clients remote IP address.
	 * 
	 * @param clientId
	 * @return
	 */
	public String getRemoteAddressText(int clientId);
	
	/**
	 * Check if a player is logged in to the system.
	 * Works system wide.
	 * 
	 * @param playerId
	 * @return true if logged in, false otherwise
	 */
	public boolean isLoggedIn(int clientId);
	
	/**
	 * Get all tables that a client is seated at.
	 * 
	 * @param playerId
	 * @return map of table : seat
	 */
	public Map<Integer, Integer> getSeatedTables(int playerId);
	
	/**
	 * Get all tables that a client is watching.
	 * 
	 * @param playerId
	 * @return List of tables
	 */
	public List<Integer> getWatchingTables(int playerId);
	
	/**
	 * Send a message to all logged in players. The type and level integers are 
	 * mapped directly against the actual protocol packet variables with the 
	 * same name, ie. are implementation specific.
	 * 
	 * @param type Message type, implementation specific
	 * @param level Message level, implementation specific
	 * @param message Message string, may be null
	 */
	public void sendSystemMessage(int type, int level, String message);
	
	/**
	 * Send a message to a subset of the logged in players. If a player identified
	 * in the player id array is not logged in the message will be dropped. The type and 
	 * level integers are  mapped directly against the actual protocol packet variables with the 
	 * same name, ie. are implementation specific.
	 * 
	 * @param type Message type, implementation specific
	 * @param level Message level, implementation specific
	 * @param playerIds Players to send to, or null for all
	 * @param message Message string, may be null
	 */
	public void sendSystemMessage(int type, int level, int[] playerIds, String message);
	
	/**
	 * Get a list of all clients logged in to the system (client ids). This 
	 * returns all players from all nodes, and not just locally logged in players.
	 * 
	 * @return
	 */
	public int[] getAllLoggedIn();
	
	
	/**
	 * Get the state of a client. The returned int will be the ordinal 
	 * of the ClientSessionState enum. 
	 *  
	 * @param clientId
	 * @return
	 */
	public int getClientStatusOrdinal(int clientId);
	
	/**
     * Get the state of a client as a readable string representation. 
     *  
     * @param clientId
     * @return
     */
    public String getClientStatusString(int clientId);
    
    /**
     * Get the screen name of a client. Client must be logged in to the system.
     * 
     * @param clientId
     * @return Screen name or null if client with given id is not logged in.
     */
    public String getScreenname(int clientId);
    
    /**
     * Returns true if the client with the given id is managed on this node locally.
     * This applies both to live clients and clients waiting for reconnects.
     * 
     * @param pid
     * @return True if logged in on this local node. False otherwise
     */
    public boolean isLocal(int clientId);
    
}
