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

import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;

/**
 * Listen to table events. This interface is accessed by Firebase
 * by one of two methods: 1) The game implements TableListenerProvider; or 
 * 2) the game implements TableListener directly. If the game implements both, the 
 * provider has precedence.
 * 
 * @author Fredrik
 */
public interface TableListener {
	
	/**
	 * A player has joined the table
	 * 
	 * @param table
	 * @param player
	 */
	public void playerJoined(Table table, GenericPlayer player);
	
	/**
	 * A player has left the table. This method is called before the player
	 * is removed from any registry, and before the leave action is sent to 
	 * the other players at the table.
	 * 
	 * @param table
	 * @param playerid
	 */
	public void playerLeft(Table table, int playerId);
	
	/**
	 * A watching player has joined the table
	 * 
	 * @param table
	 * @param player
	 */
	public void watcherJoined(Table table, int playerId);
	
	/**
	 * A watching player has left the table
	 * 
	 * @param table
	 * @param playerid
	 */
	public void watcherLeft(Table table, int playerId);
	
	/**
	 * <p>A Player Status has changed.</p> 
	 * 
	 * <p>This will typically occur when a player has disconnected without
	 * an explicit leave event (crashes, network failure etc).</p>
	 * 
	 * <p>If you implement a TableInterceptor that will deny players from leaving
	 * the table, then a status changed event will also be triggered.</p>
	 * 
	 * @param playerId
	 * @param status
	 */
	public void playerStatusChanged(Table table, int playerId, PlayerStatus status);

	/**
	 * <p>A player has reserved a seat at the table.</p>
	 * 
	 * <p>The player will not receive any game events until he has joined the table
	 * by accepting the reservation. When a player accepts and joins the table you
	 * will receive a playerJoined notification as usual.</p>
	 * 
	 * @param table
	 * @param player
	 */
	public void seatReserved(Table table, GenericPlayer player);
	
}
