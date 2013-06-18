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
 * This interface represents an active connection to the 
 * cluster. It can be used to send and receive commands. 
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
public interface ClusterConnection {

	/**
	 * @return The current number of active members in the command cluster, never negative
	 */
	public int countMembers();

	
	/**
	 * @return The addresses of the command cluster members, never null
	 */
	public SocketAddress[] getMembersInNetworkOrder();
	
	
	/**
	 * @return The local connection address, never null
	 */
	public SocketAddress getLocalAddress();
	
	
	/**
	 * @return The connection mcast address, never null
	 */
	public SocketAddress getMCastAddress();
	
	
	/**
	 * @return The command dispatcher for this connection, never null
	 */
	public CommandDispatcher getCommandDispatcher();
	
	
	/**
	 * @return the command receiver for this connection, never null
	 */
	public CommandReceiver getCommandReceiver();
	
	
	/**
	 * @param list A listener for cluster members events, must not be null
	 */
	public void addClusterListener(ClusterListener list);
	
	
	/**
	 * @param list A listener for cluster members events, must not be null
	 */
	public void removeClusterListener(ClusterListener list);
	
}
