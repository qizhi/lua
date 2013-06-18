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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.server.lobby.Lobby;
import com.cubeia.firebase.server.lobby.snapshot.DeltaSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.FullSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;

public class TableSnapshotGenerator extends SnapshotGenerator {

	private ConcurrentMap<Integer, LobbyPath> idToPathMap = new ConcurrentHashMap<Integer, LobbyPath>();
	
	public TableSnapshotGenerator(Lobby lobby, LobbyPathType type) {
		super(lobby, type, "table_snapshot_generator");
	}
	
	public void nodeAttributeChanged(NodeChangeDTO change) {
		if (change.getPath().getType().equals(LobbyPathType.TABLES)) {
			// Notify Full Snapshot. Full snapshot will report to Delta Snapshot
			FullSnapshot full = getOrCreateFullSnapshot(change.getPath(), false);
			
			lock.readLock().lock();
			try {
				// Report change while inside a read locked context
				DeltaSnapshot delta = getOrCreateDeltaSnapshot(change.getPath());
				full.reportChange(change, delta);
			} finally {
				lock.readLock().unlock();
			}
		}
	}

	@Override
	protected ConcurrentMap<Integer, LobbyPath> getIdToPathMap() {
		return idToPathMap;
	}

}
