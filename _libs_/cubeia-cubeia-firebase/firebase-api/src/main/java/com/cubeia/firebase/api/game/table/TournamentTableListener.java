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

import java.io.Serializable;

import com.cubeia.firebase.api.action.UnseatPlayersMttAction.Reason;
import com.cubeia.firebase.api.game.player.GenericPlayer;

public interface TournamentTableListener extends TableListener {

	/**
	 * Invoked when a player has been placed at this table by the tournament manager.
	 * 
	 * @param table
	 * @param player
	 * @param playerData game specific player data 
	 */
	public void tournamentPlayerJoined(Table table, GenericPlayer player, Serializable serializable);

	/**
	 * Invoked when a player has been removed from the table by the tournament manager.
	 * 
	 * @param table
	 * @param playerId
	 * @param reason the reason for the player being removed
	 */
	public void tournamentPlayerRemoved(Table table, int playerId, Reason reason);

	/**
	 * Invoked when a player has rejoined the table. This means that a player who has been disconnected
	 * is now back. Note that this method will only be invoked for players already sitting at this table. 
	 * 
	 * Typical action to take is to stop auto acting when it is this player's turn. 
	 * 
	 * @param table
	 * @param player
	 * @param playerData game specific player data
	 */
	public void tournamentPlayerRejoined(Table table, GenericPlayer player);

}