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
package com.cubeia.firebase.api.server;

/**
 * These statuses are used by the Firebase server to indicate
 * its current state. The states progress according to the enum
 * ordinals. 
 * 
 * @author Lars J. Nilsson
 */
public enum ServerStatus {

	/**
	 * Server is initializing all services and nodes.
	 */
    INITIALIZING,
    
    /**
     * All services and nodes have been initialized and the 
     * server is now starting them in their dependency order.
     */
    STARTING,
    
    /**
     * All services and nodes have been initialized and started. The
     * server is now accepting client connections.
     */
    STARTED,
    
    /**
     * The server has started to stop all services and nodes. Now new 
     * connections are accepted. 
     */
    STOPPING,
    
    /**
     * All services and nodes have been stopped and are now being
     * destroyed. The server is soon down completely.
     */
    DESTROYING
    
}
