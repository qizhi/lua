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
package com.cubeia.firebase.server;

public enum ClusterRole {

	// NODES
	SERVER_NODE,
	GAME_NODE,
	CLIENT_NODE,
	MANAGER_NODE,
	MASTER_NODE,
	MTT_NODE,
	
	// SERVICES
	CLUSTER_CONFIG_SERVICE,
	MESSAGE_BUS_SERVICE;

	public static ClusterRole parse(String nodeType) {
		if(nodeType == null || nodeType.length() == 0) return null;
		if(nodeType.equals("master")) return ClusterRole.MASTER_NODE;
		if(nodeType.equals("client")) return ClusterRole.CLIENT_NODE;
		if(nodeType.equals("game")) return ClusterRole.GAME_NODE;
		if(nodeType.equals("manager")) return ClusterRole.MANAGER_NODE;
		if(nodeType.equals("mtt")) return ClusterRole.MTT_NODE;
		return null;
	}
	
	public boolean isNodeRole() {
		return !(this.equals(CLUSTER_CONFIG_SERVICE) || this.equals(MESSAGE_BUS_SERVICE));
	}
}
