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
package com.cubeia.firebase.api.mtt;

import com.cubeia.firebase.api.mtt.activator.CreationParticipant;
import com.cubeia.firebase.api.mtt.lobby.MttLobbyObject;

/**
 * Interface for creating new tournament instances.
 * 
 * @author Fredrik
 */
public interface MttFactory {
	
	/**
	 * Create a new tournament instance and return the related mtt lobby object.  
	 * Mtt lobby objects references the concrete mtt state object by the mtt instance id.
	 * 
	 * @param mttLogicId Tournament logic id
	 * @param name Tournament name, must not be null
	 * @param creator Helper object, may be null
	 * @return A lobby representation of the mtt
	 */
	public MttLobbyObject createMtt(int mttLogicId, String name, CreationParticipant creator);

	/**
     * Get the lobby object of a specific instance. This method returns
     * null if the instance cannot be found.
     * 
     * @param mttInstanceId Tournament instance id
     * @return A tournament object, or null if not found
     */
    public MttLobbyObject getTournamentInstance(int mttInstanceId);
	
	/**
     * Get a list of all tournament instances in the lobby. 
     * This method is most likely rather slow and
     * should be used sparingly by the activator.
     * 
     * @return An array of lobby tournament objects, never null
     */
    public MttLobbyObject[] listTournamentInstances();
    
	/**
     * Get a list of tournament instances in the lobby, belonging
     * to a specified tournament logic. 
	 *
     * This method is most likely rather slow and
     * should be used sparingly by the activator.
     * 
     * @param mttLogicId Tournament logic id
     * @return An array of lobby tournament objects, never null
     */
    public MttLobbyObject[] listTournamentInstances(int mttLogicId);    
	
	/**
	 * This method destroys an mtt instance and removes all table 
	 * still in the system. This method will also remove all players from the
	 * system player registry associated with this tournament instance.
	 * 
	 * @param gameId Id of the game, is used to destroy tables
	 * @param mttInstanceId Tournament instance id
	 */
    public void destroyMtt(int gameId, int mttInstanceId);
}
