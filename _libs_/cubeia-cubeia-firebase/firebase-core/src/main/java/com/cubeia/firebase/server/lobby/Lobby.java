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
package com.cubeia.firebase.server.lobby;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.model.TableInfo;
import com.cubeia.firebase.server.lobby.model.TournamentInfo;
import com.cubeia.firebase.server.lobby.systemstate.LobbyListener;
import com.cubeia.firebase.util.ServiceMBean;


/**
 * Lobby services.
 * 
 * The lobby currently responds with packets instead of
 * Vo's such as actions for instance. This is to lower the
 * overhead of constructing lobby data.
 * 
 * We probably want to evaluate this strategi over time,
 * if we see the need for a different packet facade, then 
 * we should make a proper VO object which is then translated 
 * in the client server tier.
 *
 * @author Fredrik
 */
public interface Lobby extends ServiceMBean {
	
	
	/**
	 * Query for lobby data.
	 * Will respond with full table snapshot packets.
	 * 
	 * @param packet
	 * @return
	 */
	public List<ProtocolObject> getLobbyQuery(LobbyQueryRequest request);
	
	/**
	 * Gets a snapshot given lobby path type and an object id.
	 * 
	 * @param type the lobby path type
	 * @param objectId the id of the object to get the snapshot for
	 * @return the snapshot, or null if none found
	 */
	public ProtocolObject getSnapshot(LobbyPathType type, int objectId);
	
	/**
	 * Subscribe to a lobby path.
	 * We will send out a complete full table snapshot before delta updates start
	 * to arrive.
	 * 
	 * @param request
	 * @return
	 */
	public void subscribe(LobbySubscriptionRequest request, Client client);
	
	/**
	 * Unsubscribe a client to a lobby node (and all subnodes)
	 * 
	 * @param request
	 * @param client
	 */
	public void unsubscribe(LobbyUnsubscriptionRequest request, Client client);
	
	/**
	 * Unsubscribe a client on all nodes and objects.
	 * 
	 * @param client
	 */
	public void unsubscribeAll(Client client);
	
	/**
	 * Subscribe to a specific lobby object.
	 * We will send out a complete snapshot before delta updates.
	 * If the object does not exists in the lobby then no subscription will be saved.
	 * 
	 * A generic unsubscribe will not stop the subscription for this object. You will
	 * need to make an explicit unsubscribe in order to stop the object subscription.
	 * 
	 * @param request
	 * @param client
	 */
	public void subscribeToLobbyObject(LobbySubscriptionRequest request, Client client);
	
	/**
	 * Stop a subscription on an explicit object.
	 * 
	 * @param request
	 * @param client
	 */
	public void unsubscribeToLobbyObject(LobbyUnsubscriptionRequest request, Client client);
	
	/**
	 * Add listener for lobby changes
	 * 
	 * @param listener
	 */
	public void addLobbyListener(LobbyListener listener);
	
	/**
	 * Remove listener.
	 * 
	 * @param listener
	 */
	public void removeLobbyListener(LobbyListener listener);
	
	/**
	 * Get all tables under the supplied LobbyPath as TableInfos.
	 * 
	 * @param path
	 * @return
	 */
	public Collection<TableInfo> getTableInfos(LobbyPath path);
	
	/**
	 * Get all tournaments under the supplied LobbyPath
	 * as TournamentInfos.
	 * 
	 * @param path
	 */
	public Collection<TournamentInfo> getMttInfos(LobbyPath path);
	
	/**
	 * Get all (current) end nodes in the lobby tree.
	 * @param type 
	 * 
	 * @return
	 */
	public List<LobbyPath> getAllLobbyLeaves(LobbyPathType type);
	
	
	/**
	 * Get all end nodes starting from the given String fqn domain.
	 * 
	 * e.g.
	 * 
	 * Lobby:
	 *    /a/b/c
	 *    /a/b/d
	 *    /a/c/e
	 * 
	 * Fqn:
	 *    /a/b/
	 * 
	 * will return:
	 *    /a/b/c
	 *    /a/b/d
	 *    
	 * 
	 * @param fqn
	 * @return
	 */
	public Collection<LobbyPath> getLeaves(LobbyPath path);
	
	public void addPath(String path);
	
	/**
	 * Get a set of subscribers for a given path. 
	 * NOTE: Modifications to the returned set will modify the underlying set!
	 * 
	 * @param key
	 * @return
	 */
	public Set<Client> getSubscribers(LobbyPath key);
	
}
