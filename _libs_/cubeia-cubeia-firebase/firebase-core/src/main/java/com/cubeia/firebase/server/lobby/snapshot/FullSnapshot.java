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
package com.cubeia.firebase.server.lobby.snapshot;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributes;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.lobby.model.TableInfo;
import com.cubeia.firebase.server.lobby.model.TournamentInfo;
import com.cubeia.firebase.server.lobby.systemstate.LobbyTransformer;
import com.cubeia.firebase.server.lobby.systemstate.TableInfoBuilder;
import com.cubeia.firebase.server.lobby.systemstate.TournamentInfoBuilder;

/**
 * Keeps full information (snapshots) for a node in the lobby
 * This class will only contain TableInfos (no delta updates).
 * 
 * @author Fredrik
 */
public class FullSnapshot extends AbstractSnapshot {
	
	private static transient Logger log = Logger.getLogger(FullSnapshot.class);
	
	private final Lobby lobby;

	private final SnapshotListener listener;

	/** Keep track of if this snapshot has been just created */
	private AtomicBoolean newlyCreated = new AtomicBoolean(true);

	public FullSnapshot(Lobby lobby, LobbyPath path, SnapshotListener listener) {
		super (path);
		this.lobby = lobby;
		this.listener = listener;
		initSnapshot();
	}
	
	public FullSnapshot(Lobby lobby, LobbyPath path) {
		this(lobby, path, new NullSnapshotListener());
	}	
	
	/**
	 * Create all initial full snapshots for the given 
	 * LobbyPath
	 *
	 */
	private void initSnapshot() {
		// Check lobby type and create information objects accordingly
		switch (path.getType()) {
			case TABLES:
				initTableNode();
				break;
			case MTT:
				initMttNode();
				break;
			default:
				log.warn("Unknown Lobby Type encountered: "+path.getLobbyPath());
		}
	}

	private void initTableNode() {
		Collection<TableInfo> tableInfos = lobby.getTableInfos(path);
		for (TableInfo table : tableInfos) {
			if (table.getId() > -1) { // Sanity check 
				ProtocolObject snapshot = LobbyTransformer.transform(table);
				packets.put(table.getId(), snapshot);
				notifySnapshotCreated(table.getId(), path);
			}
		}
	}
	
	private void notifySnapshotCreated(int objectId, LobbyPath path) {
		listener.snapshotCreated(objectId, path, this);
	}
	
	private void notifySnapshotRemoved(int objectId, LobbyPathType type) {
		listener.snapshotRemoved(objectId);
	}	
	
	private void initMttNode() {
		Collection<TournamentInfo> mttInfos = lobby.getMttInfos(path);
		for (TournamentInfo mtt : mttInfos) {
			if (mtt.getId() > -1 && mtt.isReady()) { // Sanity check 
				packets.put(mtt.getId(), LobbyTransformer.transform(mtt));
				notifySnapshotCreated(mtt.getId(), path);
			}
		}
	}
	

    public ProtocolObject getObjectData(int objectId) {
		return packets.get(objectId);
	}
	
	/**
	 * Report a change in the lobby data.
	 * This snapshot implementation will build a full table info.
	 * 
	 */
	public void reportChange(NodeChangeDTO change, DeltaSnapshot delta) {
		try {
			
			// Check if the data contains the ID field. It is not an error if it doesn't 
			// since newly created tables might start to update in random field order.
			Object oid = change.getAllData().get(DefaultTableAttributes._ID.toString());
			if (oid == null) return; // EARLY RETURN, id not set (yet)
			
			int objectId = -1;
			
			ProtocolObject packet = null;
			boolean verified = false;
			
			if (change.getPath().getType().equals(LobbyPathType.TABLES)) {
				if (verifyTableData(change)) {
					TableInfo table = TableInfoBuilder.createTableInfo(change.getPath().getRootLobbyPath(), change.getAllData());
					packet = LobbyTransformer.transform(table);
					objectId = table.getId();
					verified = true;
				}	
				
			} else if (change.getPath().getType().equals(LobbyPathType.MTT)) {
				if (verifyTournamentData(change)) {
					TournamentInfo mtt = TournamentInfoBuilder.createTableInfo(change.getPath().getRootLobbyPath(), change.getAllData());
					packet = LobbyTransformer.transform(mtt);
					objectId = mtt.getId();
					verified = true;
				}
				
			} else {
				log.warn("Unknown lobby type encountered: "+change.getPath().getType());
			}
			
			if (verified) {
				/*
				 * If the snapshot was just created, i.e. by this change notification, then it is not 
				 * an update since the full snapshot has not yet been pushed to the subscribers.
				 */
				boolean update = !newlyCreated.getAndSet(false) && packets.containsKey(objectId);
				packets.put(objectId, packet);
				notifySnapshotCreated(objectId, path);
				
				// Notify Delta Snapshot
				if (update) {
					// Partial update
					delta.reportUpdate(objectId, change);
				} else {
					// New table
					delta.reportNew(objectId, change);
				}
			}
			
		} catch (Exception e) {
			log.error("Could not handle a lobby table update. Change: "+change, e);
		}
	}


	public void tableRemoved(LobbyPath path) {
		if (!packets.containsKey(path.getObjectId())) {
			log.debug("LBY Removing an object that was not in the snapshot: "+path+" This is: "+this);
		}
		packets.remove(path.getObjectId());
		notifySnapshotRemoved(path.getObjectId(), path.getType());
	}
	
	
	/**
	 * Check if we have enough data to create a snapshot
	 * 
	 * @param change
	 * @return
	 */
	private boolean verifyTableData(NodeChangeDTO change) {
		boolean enough = true;
		Map<Object, Object> allData = change.getAllData();
		enough &= allData.containsKey(DefaultTableAttributes._ID.name());
		enough &= allData.get(DefaultTableAttributes._NAME.name()) != null;
		enough &= allData.containsKey(DefaultTableAttributes._CAPACITY.name());
		enough &= allData.containsKey(DefaultTableAttributes._SEATED.name());
		enough &= allData.containsKey(DefaultTableAttributes._GAMEID.name());
		enough &= allData.containsKey(DefaultTableAttributes._WATCHERS.name());
		return enough;
	}
	
	/**
	 * Check if we have enough data to create a snapshot
	 * 
	 * @param change
	 * @return
	 */
	private boolean verifyTournamentData(NodeChangeDTO change) {
		boolean enough = true;
		Map<Object, Object> allData = change.getAllData();
		enough &= allData.containsKey(DefaultMttAttributes._ID.toString());
		enough &= allData.containsKey(DefaultMttAttributes.TOURNAMENT_ID.toString());
		enough &= allData.containsKey(DefaultMttAttributes.ACTIVE_PLAYERS.toString());
		enough &= allData.containsKey(DefaultMttAttributes.CAPACITY.toString());
		enough &= allData.containsKey(DefaultMttAttributes.REGISTERED.toString());
		enough &= allData.containsKey(DefaultMttAttributes._READY.toString()); // Ticket #574
		return enough;
	}

	public void clearNewFlag() {
		newlyCreated.set(false);		
	}

	
	@Override
	public String toString() {
		return "Fullsnapshot; path=" + super.path;
	}
}
