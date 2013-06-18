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
package com.cubeia.firebase.server.util;

import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.server.ClusterRole;

public class NodeRoles {

	private NodeRoles() { }
	
	public static Namespace getContextNamespace(ClusterRole role) {
		if(role == ClusterRole.CLIENT_NODE) return new Namespace("node.client");
		else if(role == ClusterRole.GAME_NODE) return new Namespace("node.game");
		else if(role == ClusterRole.MANAGER_NODE) return new Namespace("node.manager");
		else if(role == ClusterRole.SERVER_NODE) return new Namespace("server");
		else return new Namespace("node.master");
	}
	
	public static Namespace getNodeNamespace(ClusterRole role, String id) {
		return new Namespace(getContextNamespace(role).toString() + "." + id);
	}
}
