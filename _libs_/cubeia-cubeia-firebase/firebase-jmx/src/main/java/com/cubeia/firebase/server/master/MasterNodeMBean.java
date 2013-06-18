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

/**
 * This MBean represent a game node mounted on a server. Master
 * nodes does not have coordinators as other nodes, instead they
 * are arranged in primary/slave relationships. 
 * 
 * <p>The {@link #getLocalAddress() local address} details the 
 * local socket address used for internal communications.
 * 
 * @author Larsan
 */
public interface MasterNodeMBean {

	/**
	 * This method returns true if the node is the Firebase
	 * primary master. There is only ever primary master per 
	 * type within a cluster.
	 * 
	 * @return True if the node is the primary master, false otherwise
	 */
	public boolean isPrimary();
	
	/**
	 * Get the node id. The id is used within the cluster for 
	 * communication and identification. If not set at startup it will 
	 * be auto-generated.
	 * 
	 * @return The server id, never null
	 */
	public String getNodeId();
	
	
	/**
	 * Returns the local socket address which is used for
	 * internal communications.
	 * 
	 * @return The local socket address as string, never null
	 */
	public String getLocalAddress();
	
}
