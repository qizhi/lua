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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.util.CompilationCache;
import com.cubeia.firebase.server.lobby.snapshot.DeltaSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.generator.SnapshotGenerator;

/**
 * Runnable class for executing lobby broadcasts.
 * 
 * The Broadcaster iterates all defined subscriptions, then gets
 * the clients for each subscriptions and send all delta packets to them.
 * 
 *  Since at startup there will be snapshots created, but no subscriptions - 
 *  the snapshots will not have been drained thus causing the first subscriber
 *  to get a full delta snapshot list the first update. Not a major issue but 
 *  a little code smell.
 * 
 * @author Fredrik
 */
final class StateLobbySingleBroadcast implements Runnable {

	private final AtomicBoolean paused;
	private final ConcurrentMap<LobbyPathType, SnapshotGenerator> generators;
	private final ConcurrentMap<LobbyPath, Set<Client>> subscriptions;
	private final ObjectSubscriptions objectSubscriptions;
	
	private final Logger log = Logger.getLogger(getClass());

	StateLobbySingleBroadcast(AtomicBoolean paused, ConcurrentMap<LobbyPathType, SnapshotGenerator> generators, ConcurrentMap<LobbyPath, Set<Client>> subscriptions, ObjectSubscriptions objectSubscriptions) {
		this.paused = paused;
		this.objectSubscriptions = objectSubscriptions;
		this.subscriptions = subscriptions;
		this.generators = generators;
	} 
	
	public void run() {
	    long start = System.currentTimeMillis();
	    int counter = 0;
	    int lobbyKeys = 0;
	    int changes = 0;
		try {
		    if (!paused.get()) {
		        
		        // ExecutorService sender = Executors.newFixedThreadPool(50);
		        
				// Iterate all snapshot generators
				for (SnapshotGenerator generator : generators.values()) {
					
					// Send the regular (full lobby) - Iterate all nodes and send out updates
					// We will merge the regular lobby paths with object specific paths.
					Set<LobbyPath> keySet = new HashSet<LobbyPath>(subscriptions.keySet());
					keySet.addAll(objectSubscriptions.getLobbyPaths());
					
					lobbyKeys = keySet.size();
					
					// Get all delta snapshots for this generator
					ConcurrentMap<LobbyPath, DeltaSnapshot> deltaSnapshots = generator.getAndClearAllDeltaSnapshots();
					
					for (LobbyPath key : keySet) {
						Set<Client> clients = subscriptions.get(key);
						
						// Ticket #675: NPE in StateLobbySingleBroadcast, check for null
						List<ProtocolObject> snapshot;
						if (deltaSnapshots.containsKey(key)) {
							snapshot = deltaSnapshots.get(key).getLobbyData();
						} else {
							snapshot = Collections.emptyList();
						}
						
						// Collection<byte[]> translated = translate(snapshot);
						changes += snapshot.size();
						
						if (snapshot.size() > 0 && clients != null) {
    						// Send snapshot to all clients
							CompilationCache cache = new CompilationCache(snapshot);
    						for (Client client : clients) {
    						    // sender.submit(new Sender(client, snapshot));
    							client.sendClientPackets(cache);
    							counter += snapshot.size();
    						}
						}
						
						// We have sent updates to all lobby subscribers.
						// Now we will iterate the snapshot packets and see if we have any object specific 
						// subscribers. If the client was already in the snapshot recipients then we will ignore
						// that client here (i.e. no double packets).
						for (ProtocolObject packet : snapshot) {
							Collection<Client> objectSubscribers = objectSubscriptions.getSubscribers(key, packet);
							for (Client client : objectSubscribers) {
								if (clients == null || !clients.contains(client)) {
									client.sendClientPacket(packet);
								}
							}
						}
					}
					generator.cleanupFullSnapshots();
				}
				
		    }
		} catch (Throwable th) {
			log.error("Lobby Broadcast failed.", th);
		}
		long elapsed = System.currentTimeMillis() - start;
		if (elapsed > 400 && log.isDebugEnabled()) {
		    log.debug("Broadcasting took > 400 ms - gens["+generators.size()+"] changes["+changes+"] packets["+counter+"] Keys["+lobbyKeys+"] time["+elapsed+"]");
		}
	}

	/*private Collection<byte[]> translate(Collection<ProtocolObject> snapshot) {
		if(snapshot == null || snapshot.size() == 0) return null;
		else {
			List<byte[]> list = new ArrayList<byte[]>(snapshot.size());
			for (ProtocolObject o : snapshot) {
				try {
					list.add(StateLobby.SERIALIZER.packArray(o));
				} catch (IOException e) {
					log.error("Failed to serialize styx object", e);
				}
			}
			return list;
		}
	}*/
}