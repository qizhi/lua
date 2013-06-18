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
package com.cubeia.firebase.util;

import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.lobby.LobbyTable;
import com.cubeia.firebase.api.lobby.LobbyPath;

public final class LobbyTableImpl implements LobbyTable {
	
	private static final long serialVersionUID = -1900932172534559167L;
	
	private final int id;
	private final Map<String, AttributeValue> atts;
	
	protected final LobbyPath path;
	
	public LobbyTableImpl(int id, Map<String, AttributeValue> atts, LobbyPath path) {
		this.id = id;
		this.atts = atts;
		this.path = path;
	}
	
	public LobbyPath getLobbyPath() {
		return path;
	}

	public Map<String, AttributeValue> getAttributes() {
		return atts;
	}
	
	public int getTableId() {
		return id;
	}
	
	public int getObjectId() {
		return id;
	}
}