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
package com.cubeia.firebase.api.game.activator;

import com.cubeia.firebase.api.common.Attribute;

/**
 * Activators may want to support table creation requested by 
 * clients or other external sources, in which case this interface
 * must be implemented. 
 * 
 * @author lars.j.nilsson
 */
public interface RequestAwareActivator {

	/**
	 * Then a creation request is received, this method will be invoked. The 
	 * activator should either deny the request by throwing indicated exception 
	 * or return a creation participant for the request.
	 * 
	 * @param pid Player id that requested the table creation
	 * @param seats Number of seats of the requested table, -1 if not applicable
	 * @param attributes Optional attributes provided by the player, may be null
	 * @return A creation participant for the request, never null
	 * @throws CreationRequestDeniedException If the request is denied
	 */
	public RequestCreationParticipant getParticipantForRequest(int pid, int seats, Attribute[] attributes) throws CreationRequestDeniedException;

}
