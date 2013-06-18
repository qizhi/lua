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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TableUpdatePacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdatePacket;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.LobbySubscriptionRequest;
import com.cubeia.firebase.server.lobby.LobbyUnsubscriptionRequest;

/**
 * Wraps subscribers for a specific lobby object.
 * 
 * @author Fredrik
 */
public class ObjectSubscriptions {
	
	private static transient Logger log = Logger.getLogger(ObjectSubscriptions.class);
	
	/**
	 * Holds all current object subscriptions on this node.
	 * This is a Map that holds a Map that holds object id to subscriber list.
	 */
	private ConcurrentMap<LobbyPath, PathContainer> objectSubscriptions = new ConcurrentHashMap<LobbyPath, PathContainer>();
	
	/**
	 * Add an object subscription if object id in the LobbyPath is larger then zero.
	 * 
	 * @param request
	 * @param client
	 */
	public void addSubscription(LobbySubscriptionRequest request, Client client) {
		if (request.getPath().getObjectId() > 0) {
			PathContainer container = getContainer(request.getPath());
			container.addSubscriber(client, request.getPath().getObjectId());
		} else {
			log.warn("Someone tried to subscribe on object with negative id: "+request);
		}
	}

	public void removeSubscription(LobbyUnsubscriptionRequest request, Client client) {
		if (request.getPath().getObjectId() > 0) {
			PathContainer container = getContainer(request.getPath());
			container.removeSubscriber(client, request.getPath().getObjectId());
		} else {
			log.warn("Someone tried to unsubscribe on object with negative id: "+request);
		}
	}
	
	public void removeAllSubscriptionsForClient(Client client) {
		for (PathContainer container : objectSubscriptions.values()) {
			container.removeSubscriber(client);
		}
	}
	
	public void removeObject(LobbyPath path, int objectId) {
		PathContainer container = objectSubscriptions.get(path);
		if (container != null) {
			container.removeObject(objectId);
		}
	}
	
	/**
	 * Will return the matching subscribers. 
	 * 
	 * @param path
	 * @param packet
	 * @return Set of clients, never null.
	 */
	@SuppressWarnings("unchecked")
	public Collection<Client> getSubscribers(LobbyPath path, ProtocolObject packet) {
		try {
			if (objectSubscriptions.containsKey(path)) {
				int objectId = parseObjectId(path.getType(), packet);
				if (objectId > 0) {
					PathContainer container = getContainer(path);
					return container.getClients(objectId).values();
				}
			}
		} catch (Throwable e) {
			log.error("Error when getting object specific subscribers for path: "+path+", packet: "+packet, e);
		}
		return Collections.EMPTY_SET;
	} 
	
	public Set<LobbyPath> getLobbyPaths() {
		return objectSubscriptions.keySet();
	}
	
	/**
	 * Get a list of what objects that has a subscriber for the given LobbyPath.
	 * 
	 * @param objectKey
	 * @return
	 */
	public Collection<Integer> getObjects(LobbyPath objectKey) {
		PathContainer container = objectSubscriptions.get(objectKey);
		if (container != null) {
			return container.subscriptions.keySet();
		}
		return null;
	}
	
	/**
	 * Checking allowed types of protocol packets here.
	 * It ain't pretty but it works.
	 * @param type 
	 * 
	 * @param packet
	 * @return
	 */
	private int parseObjectId(LobbyPathType type, ProtocolObject packet) {
		int objectId = -1;
		
		if (type == LobbyPathType.TABLES) {
			if (packet instanceof TableSnapshotPacket) {
				TableSnapshotPacket typed = (TableSnapshotPacket) packet;
				objectId = typed.tableid;
				
			} else if (packet instanceof TableUpdatePacket) {
				TableUpdatePacket typed = (TableUpdatePacket) packet;
				objectId = typed.tableid;
				
			} 
		} else if (type == LobbyPathType.MTT) {
			if (packet instanceof TournamentSnapshotPacket) {
				TournamentSnapshotPacket typed = (TournamentSnapshotPacket) packet;
				objectId = typed.mttid;
				
			} else if (packet instanceof TournamentUpdatePacket) {
				TournamentUpdatePacket typed = (TournamentUpdatePacket) packet;
				objectId = typed.mttid;
			} 
		}
		
		return objectId;
	}



	/**
	 * Get or create a container object for the given path.
	 * 
	 * @param request
	 * @return
	 */
	protected PathContainer getContainer(LobbyPath path) {
		PathContainer container = objectSubscriptions.get(path);
	    if (container == null) {
	    	PathContainer newContainer = new PathContainer();
	    	container = objectSubscriptions.putIfAbsent(path, newContainer);
	        if (container == null) {
	        	container = newContainer;
	        }
	    }
	    return container;
	}
	
	
	protected class PathContainer {
		/**
		 * Holds all current object subscriptions on this node.
		 * This is a Map that holds a Map that holds object id to subscriber list.
		 */
		private ConcurrentMap<Integer,ConcurrentHashMap<Client, Client>> subscriptions = new ConcurrentHashMap<Integer,ConcurrentHashMap<Client, Client>>();
		
		public void addSubscriber(Client client, int objectId) {
			ConcurrentHashMap<Client, Client> clients = getClients(objectId);
			clients.put(client, client);
		}
		
		public void removeObject(int objectId) {
			subscriptions.remove(objectId);
		}

		public void removeSubscriber(Client client, int objectId) {
			ConcurrentHashMap<Client, Client> clients = getClients(objectId);
			clients.remove(client);
		}
		
		public void removeSubscriber(Client client) {
			for (ConcurrentHashMap<Client, Client> map : subscriptions.values()) {
				map.remove(client);
			}
		}
		
		/**
		 * Get or create a container object for the given path.
		 * 
		 * @param request
		 * @return
		 */
		public ConcurrentHashMap<Client, Client> getClients(int objectId) {
			ConcurrentHashMap<Client, Client> clients = subscriptions.get(objectId);
		    if (clients == null) {
		    	ConcurrentHashMap<Client, Client> newClients = new ConcurrentHashMap<Client, Client>();
		    	clients = subscriptions.putIfAbsent(objectId, newClients);
		        if (clients == null) {
		        	clients = newClients;
		        }
		    }
		    return clients;
		}
		
		protected ConcurrentMap<Integer,ConcurrentHashMap<Client, Client>> getSubscriptionMap() {
			return subscriptions;
		}
	}
	

}
