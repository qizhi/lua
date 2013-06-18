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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.io.ProtocolObject;

public abstract class AbstractSnapshot implements Snapshot {

	protected final LobbyPath path;
	
	public AbstractSnapshot(LobbyPath path) {
		this.path = path;
	}
	
	/**
	 * Packet snapshot of all tables.
	 * 
	 */
	protected ConcurrentMap<Integer, ProtocolObject> packets = new ConcurrentHashMap<Integer, ProtocolObject>();
	
	
	/**
	 * Get the full snapshot as protocol packets.
	 * 
	 * @return
	 */
	public Collection<ProtocolObject> getLobbyData() {
		return packets.values();
	}
	

}
