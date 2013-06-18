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

/**
 * This interface adds methods for invite-lists to the creation
 * participant. It is used when external parties, must probably clients,
 * requests tables to be created.
 * 
 * @author larsan
 */
public interface RequestCreationParticipant extends CreationParticipant {

	/**
	 * Invited players may have seats reserved for them. If this method returns
	 * false seats will not be reserved. However, the inviter will always be seated. 
	 * 
	 * @return True if the invitees should have seats reserved, false otherwise 
	 */
	public boolean reserveSeatsForInvitees();
	
	
	/**
	 * This method gives the participant an ability to modify or completely 
	 * re-create the list of invitees. NB: If the request does not contain any
	 * invitees the argument will to this method will be null. If no players 
	 * should be invited, this method may return null or an empty array.
	 * 
	 * @param invitees The invitees of the request, may be null
	 * @return A list of invitees, or return null
	 */
	public int[] modifyInvitees(int[] invitees);
	
}
