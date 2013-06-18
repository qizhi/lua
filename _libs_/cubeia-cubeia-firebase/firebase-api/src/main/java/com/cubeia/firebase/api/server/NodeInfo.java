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
package com.cubeia.firebase.api.server;

import com.cubeia.firebase.api.util.Arguments;

/**
 * This small helper class identifies a node within 
 * a server by its type and id.
 * 
 * @author Larsan
 */
public final class NodeInfo {
	
	private final NodeType type;
	private final String id;
	
	/**
	 * Create a new node info with a type and an id.
	 * 
	 * @param type Node type, must not be null
	 * @param id Node id, must not be null
	 */
	public NodeInfo(NodeType type, String id) {
		Arguments.notNull(type, "type");
		Arguments.notNull(id, "id");
		this.type = type;
		this.id = id;
	}
	
	/**
	 * @return The node id, never null
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return The node type, never null
	 */
	public NodeType getType() {
		return type;
	}
	
	
	// --- OBJECT METHODS --- //
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof NodeInfo)) return false;
		NodeInfo i = (NodeInfo)obj;
		return i.type == type && i.id.equals(id);
	}
	
	@Override
	public int hashCode() {
		return 35 ^ type.hashCode() ^ id.hashCode();
	}
	
	@Override
	public String toString() {
		return type.toString() + ":" + id;
	}
}
