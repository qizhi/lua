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
import com.cubeia.firebase.server.ClusterRole;
import com.cubeia.firebase.service.messagebus.MBusDetails;

public final class ClusterParticipant implements Serializable {

	private static final long serialVersionUID = -1321663074276322094L;
	
	private final ClusterRole role;
	private volatile boolean isCoordinator;
	private final NodeId id;

	private final MBusDetails mbus;
 
	public ClusterParticipant(NodeId id, ClusterRole role, MBusDetails mbus, boolean isCoordinator) {
		Arguments.notNull(role, "role");
		Arguments.notNull(id, "id");
		this.mbus = mbus;
		this.id = id;
		this.isCoordinator = isCoordinator;
		this.role = role;
	}
	
	public ClusterParticipant(ClusterParticipant clone) {
		Arguments.notNull(clone, "clone");
		this.isCoordinator = clone.isCoordinator;
		this.id = clone.id;
		this.mbus = clone.mbus;
		this.role = clone.role;
	}
	
	/*public ClusterParticipant(ClusterParticipant p, MBusDetails mbus, boolean isCoordinator) {
		Arguments.notNull(p, "p");
		this.mbus = mbus;
		this.id = p.id;
		this.isCoordinator = isCoordinator;
		this.role = p.role;
		this.address = p.address;
	}*/
	
	public MBusDetails getMBusDetails() {
		return mbus;
	}

	public String getId() {
		return id.id;
	}
	
	public NodeId getNodeId() {
		return id;
	}
	
	public boolean isMaster() {
		return role.equals(ClusterRole.MASTER_NODE);
	}
	
	public boolean isCoordinator() {
		return isCoordinator;
	}

	public ServerId getServerId() {
		return id.server;
	}

	public ClusterRole getRole() {
		return role;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ClusterParticipant)) return false;
		ClusterParticipant node = (ClusterParticipant)obj;
		if(!node.role.equals(role)) return false;
		return node.id.equals(id);
	}
	
	@Override
	public int hashCode() {
		return role.hashCode() ^ id.hashCode();
	}
	
	@Override
	public String toString() {
		return "{id: " + id + "; role: " + role + "; isCoordinator: " + isCoordinator + "}";
	}
	
	
	// --- PACKAGE ACCESS --- //
	
	void setIsCoordinator(boolean isCoordinator) {
		this.isCoordinator = isCoordinator;
	}
}