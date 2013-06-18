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
package com.cubeia.firebase.service.conn;

import com.cubeia.firebase.api.util.SocketAddress;

/**
 * This is an empty adapter.
 * 
 * @author Larsan
 */
//FIXME: Trac issue #417
public class ClusterConnectionAdapter implements ClusterConnection {

	public void addClusterListener(ClusterListener list) { }

	public int countMembers() {
		return 0;
	}

	public CommandDispatcher getCommandDispatcher() {
		return null;
	}

	public CommandReceiver getCommandReceiver() {
		return null;
	}

	public SocketAddress getLocalAddress() {
		return null;
	}

	public SocketAddress getMCastAddress() {
		return null;
	}

	public SocketAddress[] getMembersInNetworkOrder() {
		return null;
	}

	public void removeClusterListener(ClusterListener list) { }

}
