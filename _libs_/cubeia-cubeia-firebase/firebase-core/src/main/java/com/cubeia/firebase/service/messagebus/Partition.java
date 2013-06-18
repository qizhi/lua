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
package com.cubeia.firebase.service.messagebus;

import java.io.Serializable;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;

/**
 * This class represents a mbus partition. The partition is a logical 
 * entry of a group of channels, by event type. for example, in a table game
 * spread over several nodes the partition usually represents the node, and the 
 * channels the individual tables. This is a immutable final bean holding the 
 * partition name and id.
 * 
 * @author Larsan
 */
public final class Partition implements Serializable {

	private static final long serialVersionUID = 8899086668561967493L;
	
	private final String id, name;
	private final EventType type;
	private final SocketAddress socketId;
	private final String serverId;

	/**
	 * @param type Event type, must not be null
	 * @param id Partition id, must not be null
	 * @param name Partition readable name, must not be null
	 * @param socketId Socket id, if available
	 * @param serverId Server id, ifavailable
	 */
	public Partition(EventType type, String id, String name, SocketAddress socketId, String serverId) {
		Arguments.notNull(id, "id");
		Arguments.notNull(name, "name");
		Arguments.notNull(type, "type");
		this.serverId = serverId;
		this.socketId = socketId;
		this.type = type;
		this.name = name;
		this.id = id;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public SocketAddress getSocketId() {
		return socketId;
	}
	
	
	/**
	 * @return The partition even type, never null
	 */
	public EventType getType() {
		return type;
	}
	
	
	/**
	 * @return The partition id, never null
	 */
	public String getId() {
		return id;
	}
	
	
	/**
	 * @return The partition readable name, never null
	 */
	public String getName() {
		return name;
	}
	
	
	// --- OBJECT METHODS --- //
	
	@Override
	//FIXME: Use type as well as id!!
	public boolean equals(Object obj) {
		if(!(obj instanceof Partition)) return false;
		else return ((Partition)obj).id.equals(id);
	}
	
	@Override
	//FIXME: Use type as well as id!!
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public String toString() {
		return "Partition '" + id + "' [" + name + ", " + type + "]";
	}
}
