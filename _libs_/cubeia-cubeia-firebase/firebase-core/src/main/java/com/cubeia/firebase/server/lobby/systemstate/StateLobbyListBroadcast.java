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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.TableSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TableUpdateListPacket;
import com.cubeia.firebase.io.protocol.TableUpdatePacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotListPacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdateListPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdatePacket;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.gateway.util.CompilationCache;
import com.cubeia.firebase.server.lobby.snapshot.DeltaSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.generator.SnapshotGenerator;

/**
 * Runnable class for executing lobby broadcasts, sending aggregated lists (1.5).
 * 
 * See the SingleBroadcast for more info.
 * 
 * @author Larsan
 * @since Firebase 1.5
 */
final class StateLobbyListBroadcast implements Runnable {
	
	private AtomicBoolean paused;
	private ObjectSubscriptions objectSubscriptions;
	private ConcurrentMap<LobbyPath, Set<Client>> subscriptions;
	private ConcurrentMap<LobbyPathType, SnapshotGenerator> generators;
	
	private final Logger log = Logger.getLogger(getClass());
	
	// private final AtomicBoolean fulFlag = new AtomicBoolean(false);

	StateLobbyListBroadcast(AtomicBoolean paused, ConcurrentMap<LobbyPathType, SnapshotGenerator> generators, ConcurrentMap<LobbyPath, Set<Client>> subscriptions, ObjectSubscriptions objectSubscriptions) {
		this.paused = paused;
		this.objectSubscriptions = objectSubscriptions;
		this.subscriptions = subscriptions;
		this.generators = generators;
	} 

	public void run() {
	    long start = System.currentTimeMillis();
	    long startSend = 0;
	    long realSend = 0;
	    int counter = 0;
	    int lobbyKeys = 0;
	    int changes = 0;
	    int sent = 0;
	    int clis = 0;
	    // long totSize = 0;
		try {
		    if (!paused.get()) {
		    	
		    	// fulFlag.set(false);
		    	
		    	// Map all clients (by id) to entries with lists
		    	Map<Client, Entry> entries = new HashMap<Client, Entry>();
		    	
		    	// Create a cache of lists for the translations
		    	TranslationCache tmpCache = new TranslationCache();
		    	
		    	// loop through the generators
		    	for (SnapshotGenerator generator : generators.values()) {
		    	
		    		// get all relevant keys
		    		Set<LobbyPath> keySet = new HashSet<LobbyPath>(subscriptions.keySet());
					keySet.addAll(objectSubscriptions.getLobbyPaths());
					lobbyKeys = keySet.size();
					
					// Get all delta snapshots for this generator
					ConcurrentMap<LobbyPath, DeltaSnapshot> deltaSnapshots = generator.getAndClearAllDeltaSnapshots();
					
					for (LobbyPath key : keySet) {
						
						// get all clients
						Set<Client> clients = subscriptions.get(key);
						
						// get protocol objects
						List<ProtocolObject> snapshot;
						if (deltaSnapshots.containsKey(key)) {
							snapshot = deltaSnapshots.get(key).getLobbyData();
						} else {
							snapshot = Collections.emptyList();
						}
						
						
						// Collection<ProtocolObject> snapshot = generator.takeDeltaSnapshot(key);
						changes += snapshot.size();
						
						// Collection<byte[]> translated = translate(snapshot, tmpCache);
						CompilationCache translated = translate(snapshot, tmpCache);
						
						if(clients != null) {
							// add to each client entry
							for (Client c : clients) {
								counter += snapshot.size();
								addAllTranslated(c, entries, translated);
							}
						}
						
						// now double check for object subscriptions
						for (ProtocolObject packet : snapshot) {
							Collection<Client> objectSubscribers = objectSubscriptions.getSubscribers(key, packet);
							for (Client client : objectSubscribers) {
								if (clients == null || !clients.contains(client)) {
									addObject(client, entries, packet);
								}
							}
						}
					}
					generator.cleanupFullSnapshots();
		    	}
		    	
				// now commit all entries, this will flush the data to the clients
				clis = entries.size();
				startSend = System.currentTimeMillis();
				/*if(fulFlag.get()) {
					log.debug(" *** Comitting " + entries.size() + " entries");
				}*/
		    	for (Entry e : entries.values()) {
					sent += e.commit();
					realSend += e.sendTime;
					// totSize += e.bytes;
				}
		    }
		} catch (Throwable th) {
			log.error("Lobby Broadcast failed.", th);
		}
		long elapsed = System.currentTimeMillis() - start;
		long sendTime = System.currentTimeMillis() - startSend;
		if (elapsed > 600 && log.isDebugEnabled()) {
		    log.debug("List broadcasting took > 600 ms - clients[" + clis + "] gens["+generators.size()+"] changes["+changes+"] packets["+counter+"] aggregated[" + sent + "] keys["+lobbyKeys+"] time["+elapsed+"] commit-time[" + sendTime + "] send-time[" + realSend + "]");
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	/*
	 * Given a client, make sure there's a corresponding entry and add all pre-translated
	 * protocol objects to the entry.
	 */
	private void addAllTranslated(Client c, Map<Client, Entry> entries, CompilationCache cache) {
		Entry e = ensureEntry(c, entries);
		e.setPrecompiled(cache);
	}

	/*
	 * Translate the snapshot objects to a collection of byte arrays. This should 
	 * allocate objects into lists if possible.
	 */
	private CompilationCache translate(Collection<ProtocolObject> snapshot, TranslationCache c) {
		/*
		 * NB: Clear the cache for this entry.
		 */
		c.clear();
		
		/*
		 * Iterate and sort packets.
		 */
		for (ProtocolObject o : snapshot) {
			if(o instanceof TableSnapshotPacket) {
				c.tableSnapshots.add((TableSnapshotPacket) o);
			} else if(o instanceof TableUpdatePacket) {
				c.tableUpdates.add((TableUpdatePacket) o);
			} else if(o instanceof TournamentSnapshotPacket) {
				c.mttSnapshots.add((TournamentSnapshotPacket) o);
				/*if(((TournamentSnapshotPacket)o).mttid >= 30 && ((TournamentSnapshotPacket)o).mttid < 40) {
					fulFlag.set(true);
				}*/
			} else if(o instanceof TournamentUpdatePacket) {
				c.mttUpdates.add((TournamentUpdatePacket) o);
				/*if(((TournamentUpdatePacket)o).mttid >= 30 && ((TournamentUpdatePacket)o).mttid < 40) {
					fulFlag.set(true);
				}*/
			} else {
				c.others.add(o);
			} 
		}
		
		/*
		 * Create final list of byte arrays
		 */
		List<ProtocolObject> precompiled = new ArrayList<ProtocolObject>();
//		try {
			if(c.tableSnapshots.size() > 0) {
				precompiled.add(new TableSnapshotListPacket(new ArrayList<TableSnapshotPacket>(c.tableSnapshots)));
			}
			if(c.tableUpdates.size() > 0) {
				precompiled.add(new TableUpdateListPacket(new ArrayList<TableUpdatePacket>(c.tableUpdates)));
			}
			if(c.mttSnapshots.size() > 0) {
				precompiled.add(new TournamentSnapshotListPacket(new ArrayList<TournamentSnapshotPacket>(c.mttSnapshots)));
			} 
			if(c.mttUpdates.size() > 0) {
				precompiled.add(new TournamentUpdateListPacket(new ArrayList<TournamentUpdatePacket>(c.mttUpdates)));
			}
			if(c.others.size() > 0) {
				precompiled.addAll(new ArrayList<ProtocolObject>(c.others));
			}
//		} catch(IOException e) {
//			log.error("Failed to serialize objects", e);
//		}
		return new CompilationCache(precompiled);
	}
	
	
	/*
	 * Given a protocol objects and a client, ensure there's an entry for the 
	 * client and add the protocol object.
	 */
	private void addObject(Client c, Map<Client, Entry> entries, ProtocolObject o) {
		Entry e = ensureEntry(c, entries);
		e.add(o);
	}

	
	/*
	 * Check if there's an entry for the client available, or add one if possible.
	 */
	private Entry ensureEntry(Client c, Map<Client, Entry> entries) {
		Entry e = entries.get(c);
		if(e == null) {
			e = new Entry(c);
			entries.put(c, e);
		}
		return e;
	}

	
	// --- PRIVATE CLASSES --- //
	
	/**
	 * A single entry is coupled to a client. It aggregates lists of packets
	 * and pre-compiled lobby updates.
	 * 
	 * @author Larsan
	 */
	private class Entry {
		
		private Client client;
		
		private List<TableUpdatePacket> tableUpdates;
		private List<TableSnapshotPacket> tableSnapshots;
		private List<TournamentUpdatePacket> mttUpdates;
		private List<TournamentSnapshotPacket> mttSnapshots;
		
		private long sendTime = 0;
		// private long bytes;
		
		private CompilationCache precompiled;
		
		private Entry(Client cl) {
			this.client = cl;
		}
		
		/**
		 * Add all from collection. 
		 */
		public void setPrecompiled(CompilationCache precompiled) {
			if(this.precompiled != null) {
				this.precompiled.getObjects().addAll(precompiled.getObjects());
			} else {
				this.precompiled = precompiled;
			}
		}

		@Override
		public String toString() {
			return "Client: " + client.getId();
		}
		
		/**
		 * Send all objects. This method returns the number
		 * of actual objects sent (as some objects may have been
		 * aggregated into lists).
		 * 
		 * @return The number of objects sent, or 0
		 */
		public int commit() {
			int i = 0;
			/*if(fulFlag.get()) {
				log.debug(" *** Starting commit for client: " + client);
			}*/
			if(precompiled != null) {
				i += precompiled.getObjects().size();
				/*if(fulFlag.get()) {
					log.debug(" *** Precompiled: " + i);
				}*/
				send(precompiled);
			}
			if(tableSnapshots != null) {
				send(new TableSnapshotListPacket(tableSnapshots));
				/*if(fulFlag.get()) {
					log.debug(" *** Table snapshots: " + tableSnapshots.size());
				}*/
				i++;
			}
			if(tableUpdates != null) {
				send(new TableUpdateListPacket(tableUpdates));
				/*if(fulFlag.get()) {
					log.debug(" *** Table updates: " + tableUpdates.size());
				}*/
				i++;
			}
			if(mttSnapshots != null) {
				send(new TournamentSnapshotListPacket(mttSnapshots));
				/*if(fulFlag.get()) {
					log.debug(" *** Mtt snapshots: " + mttSnapshots.size());
				}*/
				i++;
			}
			if(mttUpdates != null) {
				send(new TournamentUpdateListPacket(mttUpdates));
				/*if(fulFlag.get()) {
					log.debug(" *** Mtt updates: " + mttUpdates.size());
				}*/
				i++;
			}
			return i;
		}
		
		
		/**
		 * Add object.
		 */
		public void add(ProtocolObject o) {
			if(o instanceof TableSnapshotPacket) {
				addTableSnapshot((TableSnapshotPacket) o);
			} else if(o instanceof TableUpdatePacket) {
				addTableUpdate((TableUpdatePacket) o);
			} else if(o instanceof TournamentSnapshotPacket) {
				addMttSnapshot((TournamentSnapshotPacket) o);
			} else if(o instanceof TournamentUpdatePacket) {
				addMttUpdate((TournamentUpdatePacket) o);
			} else {
				log.warn("Unknown packet type: " + (o == null ? "null" : o.getClass().getName()));
				precompiled.getObjects().add(o);
			}
		}

		/**
		 * Add object.
		 */
		public void addTableSnapshot(TableSnapshotPacket o) {
			if(tableSnapshots == null) {
				tableSnapshots = new LinkedList<TableSnapshotPacket>();
			}
			tableSnapshots.add(o);
		}
		
		/**
		 * Add object.
		 */
		public void addTableUpdate(TableUpdatePacket o) {
			if(tableUpdates == null) {
				tableUpdates = new LinkedList<TableUpdatePacket>();
			}
			tableUpdates.add(o);
		}
		
		/**
		 * Add object.
		 */
		public void addMttSnapshot(TournamentSnapshotPacket o) {
			if(mttSnapshots == null) {
				mttSnapshots = new LinkedList<TournamentSnapshotPacket>();
			}
			/*if(o.mttid >= 30 && o.mttid < 40) {
				fulFlag.set(true);
			}*/
			mttSnapshots.add(o);
		}
		
		/**
		 * Add object.
		 */
		public void addMttUpdate(TournamentUpdatePacket o) {
			if(mttUpdates == null) {
				mttUpdates = new LinkedList<TournamentUpdatePacket>();
			}
			/*if(o.mttid >= 30 && o.mttid < 40) {
				fulFlag.set(true);
			}*/
			mttUpdates.add(o);
		}
		
		
		// --- PRIVATE METHODS --- //
		
		private void send(ProtocolObject o) {
			long time = System.currentTimeMillis();
			client.sendClientPacket(o);
			sendTime += System.currentTimeMillis() - time;
		}
		
		private void send(CompilationCache cache) {
			long time = System.currentTimeMillis();
			client.sendClientPackets(cache);
			sendTime += System.currentTimeMillis() - time;
		}
	}
	
	/**
	 * A simple cache of lists which can be cleared in order to minimize 
	 * allocation during iterations.
	 * 
	 * @author Larsan
	 */
	private static class TranslationCache {
		
		List<TableUpdatePacket> tableUpdates = new ArrayList<TableUpdatePacket>();
		List<TableSnapshotPacket> tableSnapshots = new ArrayList<TableSnapshotPacket>();
		List<TournamentUpdatePacket> mttUpdates = new ArrayList<TournamentUpdatePacket>();
		List<TournamentSnapshotPacket> mttSnapshots = new ArrayList<TournamentSnapshotPacket>();
		List<ProtocolObject> others = new ArrayList<ProtocolObject>();
		
		public void clear() {
			tableUpdates.clear();
			tableSnapshots.clear();
			mttUpdates.clear();
			mttSnapshots.clear();
			others.clear();
		}
	}
}