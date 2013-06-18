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
package com.cubeia.firebase.service.conn.local;

import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ClusterListener;
import com.cubeia.firebase.service.conn.CommandDispatcher;
import com.cubeia.firebase.service.conn.CommandReceiver;

/**
 * A simple local connections which trivially wraps a {@link LocalCommandHub}.
 * 
 * @author Lars J. Nilsson
 * @see LocalCommandHub
 */
public class LocalConnection implements ClusterConnection {
	
	private final LocalCommandHub hub;
	private final SocketAddress bindAddress;
	private final SocketAddress mcastAddress;
	
	public LocalConnection(SocketAddress bindAddress, SocketAddress mcastAddress) {
		hub = new LocalCommandHub(bindAddress);
		this.bindAddress = bindAddress;
		this.mcastAddress = mcastAddress;
	}

	public void stop() {
		hub.stop();
	}

	@Override
	public void addClusterListener(ClusterListener list) {  }
	
	@Override
	public void removeClusterListener(ClusterListener list) { }

	@Override
	public int countMembers() {
		return 1; // Always one member
	}

	@Override
	public CommandDispatcher getCommandDispatcher() {
		return hub;
	}

	@Override
	public CommandReceiver getCommandReceiver() {
		return hub;
	}

	@Override
	public SocketAddress getLocalAddress() {
		return bindAddress;
	}

	@Override
	public SocketAddress getMCastAddress() {
		return mcastAddress;
	}

	@Override
	public SocketAddress[] getMembersInNetworkOrder() {
		return new SocketAddress[] { getLocalAddress() };
	}
}
