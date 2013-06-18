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
package com.cubeia.firebase.server.lobby.snapshot.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.lobby.snapshot.DeltaSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.FullSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;
import com.cubeia.firebase.server.lobby.snapshot.SnapshotListener;
import com.cubeia.firebase.server.lobby.systemstate.LobbyListener;
import com.cubeia.firebase.util.FirebaseLockFactory;

/**
 * <p>The SnapshotGenerator is responsible for creating cache lists of packets for
 * the nodes in the lobby tree.</p>
 * 
 * <p>Changes in the system state lobby will be propagated to the full snapshot list
 * which will determine if it is a new table or an updated table. The FullSnapshot will
 * then propagate the change to the DeltaSnapshot as appropriate, i.e. it will flag
 * as new or as update.</p>
 *
 * @author Fredrik
 */
public abstract class SnapshotGenerator implements Startable, LobbyListener, SnapshotListener {

	private static transient Logger log = Logger.getLogger(SnapshotGenerator.class);
	
	private final Lobby lobby;
	
	/** Map of all contained full snapshots */
	private ConcurrentMap<LobbyPath, FullSnapshot> fullSnapshots = new ConcurrentHashMap<LobbyPath, FullSnapshot>();
	
	/** Map of all contained delta snapshots */
	private ConcurrentMap<LobbyPath, DeltaSnapshot> deltaSnapshots = new ConcurrentHashMap<LobbyPath, DeltaSnapshot>();
	
	/*
	 * The one and only read/write lock used to synchronize reading and 
	 * writing (clear) to/from the delta snapshots.
	 * 
	 * NOTE: If the lock is causing performance problems, it would be
	 * perfectly possible to store a lock in the FullSnapshot implementation
	 * to get a more fine-grained locking behavior. 
	 *
	 * Trac #562: Using fair locks + #581
	 */
	protected final ReadWriteLock lock;
	
	/*
	 * Read/write lock for adding and removing full snapshots to 
	 * the full snapshots map.
	 */
	protected final ReadWriteLock lockFull;

	private final LobbyPathType type;
	
	/**
	 * Construct a snapshot scheduler.
	 * Will register as a lobby listener to the supplied lobby.
	 * 
	 * @param lobby
	 * @param type 
	 */
	protected SnapshotGenerator(Lobby lobby, LobbyPathType type, String lockJmxId) {
		lock = FirebaseLockFactory.createLock(lockJmxId);
		lockFull = FirebaseLockFactory.createLock(lockJmxId+"-Full");
		this.lobby = lobby;
		this.type = type;
	}
	
	
	
	// -------------- SERVICE METHODS  ------------------
	
	public LobbyPathType getType() {
		return type;
	}



	/* (non-Javadoc)
	 * @see com.game.server.lobby.snapshot.LobbySnapshot#start()
	 */
	public void start() {
		initSnapshots();
	}

	/* (non-Javadoc)
	 * @see com.game.server.lobby.snapshot.LobbySnapshot#stop()
	 */
	public void stop() {
	}

	// ----------- END OF SERVICE METHODS  ---------------
	
	
	
	
	// -------------- LOBBY LISTENER METHODS ------------------
	
	public void nodeRemoved(String path) { }
	
	/* (non-Javadoc)
	 * @see com.game.server.lobby.snapshot.LobbySnapshot#nodeCreated(com.cubeia.firebase.api.game.lobby.LobbyPath)
	 */
	public void nodeCreated(LobbyPath path) {}

	/* (non-Javadoc)
	 * @see com.game.server.lobby.snapshot.LobbySnapshot#nodeAttributeChanged(com.game.server.lobby.snapshot.TableChangeDTO)
	 */
	public abstract void nodeAttributeChanged(NodeChangeDTO change);
	
	public void tableRemoved(LobbyPath path) {
		FullSnapshot full = getOrCreateFullSnapshot(path);
		full.tableRemoved(path);
		// safeCheckEmpty(path, full);
		
		lock.readLock().lock();
		try {
			DeltaSnapshot delta = getOrCreateDeltaSnapshot(path);
			delta.tableRemoved(path);
		} finally {
			lock.readLock().unlock();
		}
	}
	

	// ---------- END OF LOBBY LISTENER METHODS  ---------------
	
	/**
	 * This method forces the creation of a snapshot, if one
	 * does not already exist for the path.
	 */
	public void initSnapshot(LobbyPath path) {
		getOrCreateFullSnapshot(path);
	}
	
	/**
	 * Get update packet for a single object.
	 * Will return null if no update was found.
	 * @return 
	 */
	public ProtocolObject getSinglePacket(LobbyPath path, int objectId) {
		DeltaSnapshot deltaSnapshot = deltaSnapshots.get(path);
		if (deltaSnapshot != null) {
			return deltaSnapshot.getObjectData(objectId);
		}
		return null;
	}
	
	/**
	 * Gets a single full snapshot packet.
	 * 
	 * Will return null if no snapshot was found.
	 * @return 
	 */
	public ProtocolObject getSingleSnapshot(LobbyPath path, int objectId) {
		if (path == null) return null;
		
		FullSnapshot fullSnapshot = fullSnapshots.get(path);
		if (fullSnapshot != null) {
			return fullSnapshot.getObjectData(objectId);
		}
		return null;
	}	
	
	/**
	 * Call this to clear delta data when the delta snapshot has been published.
	 * We do not clear the full snapshot since that is still needed.
	 * 
	 * This will call on remove, so make sure that you use this in a controlled
	 * way, i.e. within a write lock.
	 */
	public void clearDelta(LobbyPath path) {
		deltaSnapshots.remove(path);
	}
	
	/**
	 * Returns a copy of the delta snapshot map and clears the one used in the generator.
	 * @return
	 */
	public ConcurrentMap<LobbyPath, DeltaSnapshot> getAndClearAllDeltaSnapshots() {
		ConcurrentMap<LobbyPath, DeltaSnapshot> map;
		lock.writeLock().lock();
		try {
			map = new ConcurrentHashMap<LobbyPath, DeltaSnapshot>(deltaSnapshots);
			deltaSnapshots.clear();
		} finally {
			lock.writeLock().unlock();
		}
		return map;
	}
	
	/**
	 * Get lobby data for *this* lobby path and this lobby path only.
	 */
	public List<ProtocolObject> getFullSnapshot(LobbyPath key) {
		Collection<LobbyPath> leaves = lobby.getLeaves(key);
		List<ProtocolObject> packets = null; 
		for (LobbyPath path : leaves) {
			FullSnapshot snapshot = fullSnapshots.get(path);
			if (snapshot != null) {
				// Get current snapshot
				if(packets == null) {
					packets = new ArrayList<ProtocolObject>(snapshot.getLobbyData());
				} else {
					packets.addAll(snapshot.getLobbyData());
				}
			}
		}
		if(packets == null) {
			return Collections.emptyList();
		} else {
			return packets;
		}
	}
	
	/**
	 * Get delta lobby data for *this* path and this path only.
	 * The returned collection is a copy of the underlying collection so it 
	 * will be not be fail-fast when iterating.
	 * 
	 * Calling this method will also clear the snapshot of data. We need to do this
	 * in a single method in order to keep the method atomic. A read write lock
	 * is used to ensure this. 
	 * 
	 * 
	 * @param key
	 * @return
	 */
//	public Collection<ProtocolObject> takeDeltaSnapshot(LobbyPath key) {
//		if (deltaSnapshots.containsKey(key)) {
//			lock.writeLock().lock();
//			try {
//				// Get and clear data while inside a write locked context
//				Collection<ProtocolObject> packets = deltaSnapshots.get(key).getLobbyData();
//				clearDelta(key);
//				return packets;
//			} finally {
//				lock.writeLock().unlock();
//			}
//		} else {
//			return Collections.emptySet();
//		}
//	}

	public void dumpFullSnapshotKeysToLog() {
		String nfo = "Full Snapshot keys: ";
		for (LobbyPath path : fullSnapshots.keySet()) {
			nfo += "["+path.getRootLobbyPath()+"] ";
		}
		log.debug(nfo);
	}
	
	/**
	 * We need to initialize the full snapshot since that should always
	 * hold every information.
	 *
	 */
	private void initSnapshots() {
		List<LobbyPath> lobbyLeaves = lobby.getAllLobbyLeaves(type);
		for (LobbyPath path : lobbyLeaves) {
			getOrCreateFullSnapshot(path);
		}
	}
	
	public void cleanupFullSnapshots() {
		lockFull.writeLock().lock();
		try {
			for (LobbyPath path : fullSnapshots.keySet()) {
				if (fullSnapshots.get(path).getLobbyData().size() == 0) {
					fullSnapshots.remove(path);
					log.debug("Cleaned up empty lobby node: "+path);
				}
			}
		} finally {
			lockFull.writeLock().unlock();
		}
	}
	
	protected FullSnapshot getOrCreateFullSnapshot(LobbyPath path) {
		return getOrCreateFullSnapshot(path, true);
	}
	
	/**
	 * The newly created flag should only be used when you access a snapshot from
	 * within an update method. By keeping track of newly created the update method
	 * can differentiate between sending table updates and table snapshots to subscribers. 
	 * 
	 * @param path
	 * @param clearNewFlag, use false if from a change listener
	 * @return
	 */
	protected FullSnapshot getOrCreateFullSnapshot(LobbyPath path, boolean clearNewFlag) {
		lockFull.readLock().lock();
		try {
			FullSnapshot snapshot = fullSnapshots.get(path);
		    if (snapshot == null) {
		        FullSnapshot newSnapshot = new FullSnapshot(lobby, path, this);
		        snapshot = fullSnapshots.putIfAbsent(path, newSnapshot);
		        if (snapshot == null) {
		        	snapshot = newSnapshot;
		        	if (clearNewFlag) {
		        		snapshot.clearNewFlag();
		        	}
		        }
		    }
		    return snapshot;
		} finally {
			lockFull.readLock().unlock();
		}
	}
	
	protected DeltaSnapshot getOrCreateDeltaSnapshot(LobbyPath path) {
		DeltaSnapshot snapshot = deltaSnapshots.get(path);
	    if (snapshot == null) {
	    	DeltaSnapshot newSnapshot = new DeltaSnapshot(path);
	        snapshot = deltaSnapshots.putIfAbsent(path, newSnapshot);
	        if (snapshot == null) {
	        	snapshot = newSnapshot;
	        }
	    }
	    return snapshot;
	}
	
	protected abstract ConcurrentMap<Integer, LobbyPath> getIdToPathMap();
	
	public void snapshotCreated(int objectId, LobbyPath path, FullSnapshot snapshot) {
		getIdToPathMap().put(objectId, path);
	}
	
	public void snapshotRemoved(int objectId) {
		getIdToPathMap().remove(objectId);
	}

	public LobbyPath getPath(int objectId) {
		return getIdToPathMap().get(objectId);
	}
	
	public ConcurrentMap<LobbyPath, FullSnapshot> getFullSnapshots() {
		return fullSnapshots;
	}
	
	public ConcurrentMap<LobbyPath, DeltaSnapshot> getDeltaSnapshots() {
		return deltaSnapshots;
	}

}
