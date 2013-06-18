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
package com.cubeia.firebase.server.command;

import com.cubeia.firebase.server.layout.ClusterLayout;
import com.cubeia.firebase.server.node.Node;
import com.cubeia.firebase.server.node.NodeContext;
import com.cubeia.firebase.service.conn.ClusterConnection;

/**
 * A common context for command handlers. Tied to node context
 * via node types.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */

// TODO: Refarctor to remove node dependency
public interface CommandHandlerContext<T extends Node<?>> extends NodeContext {
	
	/**
	 * @return A connection to the cluster, never null
	 */
	public ClusterConnection getCluster();
	
	
	/**
	 * @param haltId Id of the arrived halt message
	 * @param message Readable message for the cluster halt
	 */
	public void halt(long haltId, String message);
	
	
	/**
	 * @param haltId Id corresponding the preceedeing halt id
	 * @param layout New cluster layout, never null
	 */
	public void resume(long haltId, ClusterLayout layout);

	
	/**
	 * @return Node this halder is acting for, never null
	 */
	public T getNode();
	
}
