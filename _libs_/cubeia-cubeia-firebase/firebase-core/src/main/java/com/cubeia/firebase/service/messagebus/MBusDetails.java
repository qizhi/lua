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
import java.util.EnumMap;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;

/**
 * This is a bean-like structure, used in handshakes to identify the message bus
 * "governing" a node. Each node belongs to a server and each server has a message bus;
 * this object is used to identify the message bus which belongs to the same server
 * as a given node.
 * 
 * @author Larsan
 */
public final class MBusDetails implements Serializable {

	private static final long serialVersionUID = -5991275399584114878L;
	
	private final EnumMap<EventType, SocketAddress> map = new EnumMap<EventType, SocketAddress>(EventType.class);
	
	/**
	 * @param type Type to get socket for, must not be null
	 * @return An address if one is in use, null otherwise
	 */
	public SocketAddress getSocketIdFor(EventType type) {
		Arguments.notNull(type, "type");
		return map.get(type);
	}
	
	/**
	 * @param type Type to add or remove socket for, must not be null
	 * @param a Address to use, if null if will be remove
	 */
	public void setSocketIdFor(EventType type, SocketAddress a) {
		Arguments.notNull(type, "type");
		if(a != null) {
			map.put(type, a);
		} else {
			map.remove(type);
		}
	}
}
