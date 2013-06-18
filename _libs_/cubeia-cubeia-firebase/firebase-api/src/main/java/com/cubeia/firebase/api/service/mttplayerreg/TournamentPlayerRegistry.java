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
package com.cubeia.firebase.api.service.mttplayerreg;

import com.cubeia.firebase.api.service.Contract;

/**
 * This object is for mapping players to tournament in the 
 * system state. Tournament are responsible for keeping this mapping up
 * to date when the system starts, when players register/unregister or
 * when a tournament is destroyed.
 * 
 * @author Larsan
 */
public interface TournamentPlayerRegistry extends Contract {

	/**
	 * Register a player in a tournament. This method should be called when
	 * a player is registered or when the system starts and a tournament is 
	 * populated from the backend. 
	 * 
	 * @param playerId Player to register
	 * @param mttId Tournament id
	 */
	public void register(int playerId, int mttId);
	
	
	/**
	 * Unregister a player from a tournament. THis method should be called when 
	 * a player is unregistered or leaves the tournament.
	 * 
	 * @param playerId Player to unregister
	 * @param mttId Tournament id
	 */
	public void unregister(int playerId, int mttId);
	
	
	/**
	 * Unregister all players from a tournament. This method should be
	 * called when a tournament is destroyed.
	 * 
	 * @param mttId Tournament id
	 */
	public void unregisterAll(int mttId);
	
	
	/**
	 * This method returns all tournaments a player is currently
	 * registered in. If the player is not found in the registry this
	 * method returns an empty array.
	 * 
	 * @param playerId Player to get tournaments for
	 * @return All tournaments a player is registered for, or an empty array, never null
	 */
	public int[] getTournamentsForPlayer(int playerId);
	
	
	/**
	 * This method returns all players currently registered for a
	 * tournament. If the tournament is not found in the registry this
	 * method returns an empty array.
	 * 
	 * @param mttId Tournament to get players for
	 * @return All players registered for a tournament, or an empty array, never null
	 */
	public int[] getPlayersForTournament(int mttId);
	
}
