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
package com.cubeia.firebase.server.jmx;

/**
 * This JMX interface connects to the local server instance. It can be used 
 * to add and remove nodes within the server.
 * 
 * @author Larsan
 */
public interface LocalServerMBean {
	
	/**
	 * This method returns the status of the current server as, "INITIALIZING",
	 * "STARTING", "STARTED", "STOPPING" or "DESTROYING". This enumeration can be found
	 * in the Firebase AI "ServerStatus" class. 
	 * 
	 * <p>External systems that integrate with Firbase can use this status to determine
	 * if the system is available ("STARTED").
	 */
	public String getServerStatus();

	/**
	 * Add a node type to the local server. If the server is running the node
	 * will be initiated and started. Please note that you cannot have more than one node per 
	 * type per server and that you cannot add a master node if the server is already running.
	 * 
	 * @param nodeType Node type to add to the server, must be null or one of "game", "master", "mtt, "client" or "singleton"
     * @param id New node id, must not be null unless type is "singleton"
     */
	public void addNode(String nodeType, String id);
	
	
	/**
	 * Remove a node from the local server. If the server is running and
	 * contains a node with the given id, the node will be stopped and 
	 * destroyed.
	 * 
	 * @param id Id of the node to remove, must not be null
	 */
	public void removeNode(String id);
	
	
	/**
	 * This method returns an array of node types which have been added. It 
	 * should never contain more than one "master".
	 * 
	 * @return An array of node type strings, "master", "game", "mtt" or "client"
	 */
	
	public String[] getLiveNodes();

}
