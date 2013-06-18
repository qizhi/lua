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
package com.cubeia.firebase.api.game.table;

import java.util.List;

import com.cubeia.firebase.io.protocol.Param;

/**
 * Provides extended, game specific details for players and table. If needed
 * this interface should be implemented by the game.
 */
public interface ExtendedDetailsProvider {

	/**
	 * Gets a map of details for a given player. Should return a list of {@link Param}s containing
	 * the details. The values can be either {@link String}s or {@link Integer}s.
	 * 
	 * If details are requested for a player who cannot be located null can safely be returned.
	 * 
	 * @param table the table where the player should be seated
	 * @param playerId the playerId to get details for
	 * @param fromLobby boolean indicating if the details are requested from the lobby or from within the game
	 * @return a {@link List} of parameters, or null if no details are available.
	 */
	public List<Param> getExtendedDetails(Table table, int playerId, boolean fromLobby);

}
