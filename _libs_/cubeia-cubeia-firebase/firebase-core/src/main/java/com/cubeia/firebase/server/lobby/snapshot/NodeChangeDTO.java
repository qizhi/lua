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

import java.util.Map;

import com.cubeia.firebase.api.lobby.LobbyPath;

public class NodeChangeDTO {

	private final LobbyPath path;
	private final Map<Object, Object> changed;
	private final Map<Object, Object> allData;
	private final boolean isRemoval;

	public NodeChangeDTO(LobbyPath path, Map<Object, Object> changed, boolean isRemoval, Map<Object, Object> allData) {
		this.path = path;
		this.changed = changed;
		this.isRemoval = isRemoval;
		this.allData = allData;
	}
	
	public boolean isRemoval() {
		return isRemoval;
	}

	public String toString() {
		return "LobbyObjectChange ["+path+"] changed: "+changed + "; isRemoval: " + isRemoval;
	}
	
	public Map<Object, Object> getAllData() {
		return allData;
	}

	public Map<Object, Object> getChanged() {
		return changed;
	}

	public LobbyPath getPath() {
		return path;
	}	
}
