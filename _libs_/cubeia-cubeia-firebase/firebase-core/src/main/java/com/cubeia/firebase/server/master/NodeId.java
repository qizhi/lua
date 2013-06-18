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
package com.cubeia.firebase.server.master;

import java.io.Serializable;

import com.cubeia.firebase.api.util.Arguments;

public final class NodeId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final ServerId server;
	public final String id;

	public NodeId(ServerId server, String id) {
		Arguments.notNull(server, "server");
		Arguments.notNull(id, "id");
		this.server = server;
		this.id = id;	
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof NodeId)) return false;
		NodeId o = (NodeId)obj;
		return o.server.equals(server) && o.id.equals(id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode() ^ server.hashCode();
	}
	
	@Override
	public String toString() {
		return id + "[" + server.toString() + "]";
	}
}
