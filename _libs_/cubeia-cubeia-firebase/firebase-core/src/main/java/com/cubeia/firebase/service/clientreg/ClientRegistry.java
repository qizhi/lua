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
package com.cubeia.firebase.service.clientreg;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.clientregistry.ClientSessionState;
import com.cubeia.firebase.server.gateway.client.Client;

/**
 * This is the internal client registry service. The client registry
 * manages client connection over the entire Firebase server. It uses
 * the system state to synchronize data between cluster members. 
 * 
 * @author Fredrik
 */
public interface ClientRegistry extends Service {

	/**
	 * Is the Client Registry accepting new clients? The
	 * registry is only accepting new clients when it is properly
	 * started. 
	 * 
	 * @return true if the registry is accepting new clients, false otherwise
	 */
	public boolean accepting();

	/**
	 * Get the number of local clients registered in this instance. This
	 * may differ from {@link #getNumberOfGlobalClients()} if running in a 
	 * cluster. 
	 * 
	 * @return The total number of clients in the registry
	 */
	public int getNumberOfClients();
	
	
	/**
	 * Get the total number of clients known over an entire Firebase
	 * installation. For single servers this will equal {@link #getNumberOfClients()}.
	 * 
	 * @return The total number of client in a Firebase cluster
	 */
	public int getNumberOfGlobalClients();

	
	/**
	 * Add a client to the registry. This will typically be 
	 * done on login.
	 * 
	 * @param id Client ID, mandatory
	 * @param client Client to add, must not be null
	 */
	public void addClient(int id, Client client);

	
	/**
	 * Logout the client. This will...
	 * 
	 * <ul>
	 *  <li>Remove him from all tables as watcher</li>
	 *  <li>Remove him from all tables as player</li>
	 *  <li>Remove the reference in the local registry</li>
	 *  <li>Remove him from the distributed registry</li>
	 * </ul>
	 * 
	 * @param client Client to remove, must not be null
	 * @param removeFromTables True to remove all table associations, false otherwise
	 */
	public void logoutClient(Client client, boolean removeFromTables);
	
	
	/**
	 * Report that we have lost connection to a client. If the client has logged out
	 * before this method, or has not logged in the method returns false. This will
	 * update the client state accordingly and notify tables.
	 * 
	 * @param id Client ID, mandatory
	 * @return True if the client was known, false if ha had already logged out
	 * 
	 */
	public boolean clientLostConnection(int id);
	
	
	/**
	 * Called by reaper when a session is due to 
	 * be removed from the system. This will update the client state 
	 * accordingly and notify tables.
	 * 
	 * @param id Client ID, mandatory
	 */
	public void clientReaperTimeout(int id);

	
	/**
	 * Get a local client, will return null if the 
	 * client is not connected to this local node.
	 * 
	 * @param id Client ID, mandatory
	 * @return A client, or null
	 */
	public Client getClient(int id);
	
	
	/**
     * Return the client state for the given player ID.
     * Try to minimize system state lookup.
     * 
     * @param pid Client ID, mandatory
     * @return The session state for the given client
     */
    public ClientSessionState getClientStatus(int pid);
	
    
	/**
	 * For a given client, return the mbus details of the
	 * client event. This can be used to pinpoint a target node
	 * in the mbus.
	 * 
	 * @param id Client ID, mandatory
	 * @return The socket address of the client node the client is at, or null
	 */
	public com.cubeia.firebase.api.util.SocketAddress getClientMBusChannel(int id);
	
	
	/**
	 * Get all clients contained in the local client registry.
	 * 
	 * @return An array of clients, never null
	 */
	public Client[] getLocalClients();
	
	
	/**
	 * Checks if the client ID exists in the distributed data.
	 * 
	 * @param id Client ID, mandatory
	 * @return True if the client exists, false otherwise
	 */
	public boolean exists(int id);
	
	
	/**
	 * A client is seated at a table. We need to store this 
	 * information so that we can handle disconnects and reconnects
	 * in an orderly fashion. This method is equivalent of calling
	 * {@link #addClientTable(int, int, int, int)} with an mttId of
	 * "-1".
	 * 
	 * @param playerId Client ID, mandatory
	 * @param tableId Table ID, mandatory
	 * @param seat Seat number, mandatory
	 */
	public void addClientTable(int playerId, int tableId, int seat);
	
	
	/**
	 * A client is seated at a table. We need to store this 
	 * information so that we can handle disconnects and reconnects
	 * in an orderly fashion.
	 * 
	 * @param playerId Client ID, mandatory
	 * @param tableId Table ID, mandatory
	 * @param seat Seat number, mandatory
	 * @param mttId Tournament ID, optional
	 */
	public void addClientTable(int playerId, int tableId, int seat, int mttId);
	
	
	/**
	 * A client has left a table. This will remove any mappings 
	 * in the registry.
	 * 
	 * @param playerId Client ID, mandatory
	 * @param tableId Table ID, mandatory
	 */
	public void removeClientTable(int playerId, int tableId);
	
	
	/**
	 * Remove a client from all tables. This will remove any mappings 
	 * in the registry. This is typically done on logout.
	 * 
	 * @param playerId Client ID, mandatory
	 */
	public void removeClientFromAllTables(int playerId);
	
	
	/**
	 * A client is watching a table. We need to store this 
	 * information so that we can handle disconnects and reconnects
	 * in an orderly fashion.  
	 * 
	 * @param playerId Client ID, mandatory
	 * @param tableId Table ID, mandatory
	 */
	public void addWatchingTable(int playerId, int tableId);
	
	
	/**
	 * A client has left a table as watcher so remove the mapping.
	 * 
	 * @param playerId Client ID, mandatory
	 * @param tableId Table ID, mandatory
	 */
	public void removeWatchingTable(int playerId, int tableId);
	

	/**
	 * Remove the client from watchers for all tables registered at.
	 * If the client generated flag is set then we will simulate a client
	 * request for un-watch table (i.e. the table association will be removed).
	 * 
	 * @param playerId Client ID, mandatory
	 * @param clientGenerated True if this was initiated on a player initiative
	 */
	public void removeWatcherFromAllTables(int playerId, boolean clientGenerated);
	
	
	/**
	 * Get all tables that a client is seated at. This includes all tournament 
	 * tables. 
	 * 
	 * @param playerId Client ID, mandatory
	 * @return A map of table ID -> seat ID, never null
	 */
	public Map<Integer, Integer> getSeatedTables(int playerId);
	
	
	/**
	 * Get all tables that a client is seated at. This excludes all non-tournament 
	 * tables and returns a map of table id to tournament id.
	 * 
	 * @param playerId Client ID, mandatory
	 * @return A map of table ID -> seat ID, never null
	 */
	public Map<Integer, Integer> getSeatedTournamentTables(int playerId);
	
	
	/**
	 * Get all tables that a client is watching.
	 * 
	 * @param playerId Client ID, mandatory
	 * @return List of tables ID's, never null
	 */
	public List<Integer> getWatchingTables(int playerId);
	
	
	/**
	 * Ask the registry to create a reaper for itself. The management if the reaper
	 * will be done outside of the registry.
	 * 
	 * @return A new reaper for the registry, never null
	 */
	public ClientReaper createReaper();
	
	
	/**
	 * Get the current session id for a client.
	 * 
	 * @param playerId Client ID, mandatory
	 * @return A session ID, or null if not found
	 */
	public String getSessionId(int playerid);

	
	/**
	 * Get all data in the distributed registry for a given 
	 * connected player. 
	 * 
	 * @param playerId Client ID, mandatory
	 * @return A client key/value store, empty if the player is not found
	 */
	public Map<Object, Object> getPlayerData(int playerId);

	
	/**
	 * Get the screen name for this connected player.
	 * 
	 * @param playerId Client ID, mandatory
	 * @return A screen name, or null
	 */
	public String getScreenname(int playerId);
    
	/**
	 * Get the operator id for the client.
	 * 
	 * @param playerId
	 * @return
	 */
	public int getOperatorId(int playerId);
	
    /**
     * Reports a new status for a player at a table.
     * 
	 * @param playerId Table ID, mandatory
	 * @param playerId Client ID, mandatory
     * @param status Status, must not be null
     */
    public void reportTableStatusChanged(int tableId, int playerId, PlayerStatus status);
    
    
    /**
     * Removes all waiting requests that a client was registered for.
     * 
	 * @param playerId Client ID, mandatory
     */
    public void removeClientFromWaitingLists(int playerId);
    
    
    /**
     * Removes a player from all chat channels.
     * 
	 * @param playerId Client ID, mandatory
     */
    public void removeClientFromChatChannels(int playerId);

    /**
     * Add a managed client node id.
     * 
     * @param id Client node id, must not be null
     */
	public void addNodeId(String id);
	
	
    /**
     * remove a managed client node id.
     * 
     * @param id Client node id, must not be null
     */
	public void removeNodeId(String id);
	
	
	/**
	 * <p>Get the remove address of the client.</p>
	 * 
	 * <p>Will return null if the client is not found. If you using a load balancer
	 * you might have to configure it to keep the original IP address of the 
	 * remote client.</p>
	 * 
	 * @param clientId Client ID, mandatory
	 * @return The remote address of a clinet, or null
	 */
	public SocketAddress getRemoteAddress(int clientId);

	
	/**
	 * This method checks if a given table is an tournament table for a given player. If the
	 * table is not registered as an tournament table, this method returns -1.
	 * 
	 * @param playerId Player ID, mandatory
	 * @param tableId Table ID, mandatory
	 * @return The tournament id, or -1
	 */
	public int getTableMttId(int playerId, int tableId);
	
	
    /**
     * Returns true if the client with the given id is managed on this node locally.
     * This applies both to live clients and clients waiting for reconnects.
     * 
	 * @param clientId Table ID, mandatory
     * @return True if logged in on this local node. False otherwise
     */
    public boolean isLocal(int clientId);
    
    
	/**
	 * Get a list of the ID's of all clients logged in to the system.
	 * This includes clients logged in at remote nodes.
	 * 
     * @return An array of ID's never null
	 */
	public int[] getAllLoggedIn();
}
