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
 * This listener interfaced is used by services that wishes to be notified
 * by the server when nodes are added or removed.
 * 
 * <p><b>NB: </b>Service must not block on calls to this interface, extended
 * tasks should be performed in the background.
 * 
 * @author Larsan
 */
public interface NodeListener {

	/**
	 * This method is called by the server when a node is 
	 * initialized. The "isPre" parameter shows if this call is 
	 * made before or after init.
	 * 
	 * @param info Node info, never null
	 * @param isPre True if this is before init, false if it is after
	 */
	public void nodeInit(NodeInfo info, boolean isPre);
	
	
	/**
	 * This method is called by the server when a node is 
	 * started. The "isPre" parameter shows if this call is 
	 * made before or after start.
	 * 
	 * @param info Node info, never null
	 * @param isPre True if this is before start, false if it is after
	 */
	public void nodeStart(NodeInfo info, boolean isPre);
	
	
	/**
	 * This method is called by the server when a node is 
	 * stopped. The "isPre" parameter shows if this call is 
	 * made before or after stop.
	 * 
	 * @param info Node info, never null
	 * @param isPre True if this is before stop, false if it is after
	 */
	public void nodeStop(NodeInfo info, boolean isPre);
	
	
	/**
	 * This method is called by the server when a node is 
	 * destroyed. The "isPre" parameter shows if this call is 
	 * made before or after destroy.
	 * 
	 * @param info Node info, never null
	 * @param isPre True if this is before destroy, false if it is after
	 */
	public void nodeDestroy(NodeInfo info, boolean isPre);
	
}
