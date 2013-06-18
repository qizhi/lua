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
package com.cubeia.firebase.api.service.clientregistry;

import java.net.SocketAddress;
import java.util.Map;

import com.cubeia.firebase.api.service.Contract;

/**
 * Public accessor service for the distributed and local client
 * registry. This interface exposes a subset of the internal 
 * client registry to game developers.
 *
 * @author Fredrik
 */
public interface PublicClientRegistryService extends Contract {
	
	/**
	 * Get a logged in players screenname.
	 * 
	 * @param playerId Player ID
	 * @return the screen name or null if not logged in.
	 */
	public String getScreenname(int clientId);
	
	public int getOperatorId(int clientId);
	
	/**
	 * Returns an immutable map of player data contained in the
	 * distributed player registry.
	 * 
	 * @param playerId Player ID
	 * @return A map of string attributes, or null of not found
	 */
	public Map<?, ?> getPlayerData(int clientId);
	
	/**
	 * Check if a player is logged in to the system.
	 * 
	 * @param playerId Player ID
	 * @return true if logged in, false otherwise
	 */
	public boolean isLoggedIn(int clientId);
	
	
	/**
	 * Get status for a client's session.
	 * 
	 * @param clientId Player ID
	 * @return Session state, or null if not found
	 */
	public ClientSessionState getClientStatus(int clientId);
	
	/**
	 * Get all tables that a client is seated at.
	 * Should never return null. 
	 * 
	 * @param playerId Player ID
	 * @return Map of table ID mapped to seat ID, rturns empty map if not found
	 */
	public Map<Integer, Integer> getSeatedTables(int clientId);
	
	/**
	 * Manage an association of a table:seat to a player. This method should
	 * be used if the player was manually seated. It is used both to add and 
	 * remove associations
	 * 
	 * @param tableid Table ID player was seated at
	 * @param playerid ID of the seated player
	 * @param seat Seat if the player was seated at
	 * @param remove True to remove the association, false to add 
	 */
    public void registerPlayerToTable(int tableid, int clientId, int seat, boolean remove);
    
	/**
	 * Register an association of a table:seat to a player. This method should
	 * be used if the player was manually seated. It is used both to add and 
	 * remove associations
	 * 
	 * @param tableid Table ID player was seated at
	 * @param playerid ID of the seated player
	 * @param seat Seat if the player was seated at
	 * @param mttId Should be -1 if not a tournament table
	 * @param remove True to remove the association, false to add 
	 */
    public void registerPlayerToTable(int tableid, int clientId, int seat, int mttId, boolean remove);

    /**
     * Register an association of a watcher to a table. It is used both to add and 
	 * remove associations
     * 
	 * @param tableid Table ID player is watching at
	 * @param playerid ID of the watching player
	 * @param remove True to remove the association, false to add 
     */
	public void registerWatcherToTable(int tableid, int clientId, boolean remove);
	
	/**
	 * <p>Get the remove address of the client.</p>
	 * 
	 * <p>Will return null if the client is not found. If you using a load balancer
	 * you might have to configure it to keep the original IP address of the 
	 * remote client.</p>
	 * 
	 * @param clientId Player ID
	 * @return Remote socket address, or null if not found
	 */
	public SocketAddress getRemoteAddress(int clientId);
	
	/**
	 * Get the number of clients logged in on this server.
	 * 
	 * @return number of clients.
	 */
    public int getLocalClientCount();
    
    /**
     * Get the number of clients logged in to the system.
     * This method as performance implications and it is recommended that it is not
     * executed frequently.
     * 
     * @return number of clients logged in to the system
     */
    public int getGlobalClientCount();
    
	/**
	 * Get a list of all clients logged in to the system (client ids).
	 */
	public int[] getAllLoggedIn();
	
    /**
     * Returns true if the client with the given id is managed on this node locally.
     * This applies both to live clients and clients waiting for reconnects.
     * 
     * @param clientID Player ID
     * @return True if logged in on this local node, false otherwise
     */
    public boolean isLocal(int clientId);
	
}
