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
package com.cubeia.firebase.api.util;

import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;

/**
 * This is an adapter class for a table listener. 
 * 
 * @author Lars J. Nilsson
 */
public class TableListenerAdapter implements TableListener {

	@Override
	public void playerJoined(Table table, GenericPlayer player) { }

	@Override
	public void playerLeft(Table table, int playerId) { }

	@Override
	public void watcherJoined(Table table, int playerId) { }

	@Override
	public void watcherLeft(Table table, int playerId) { }

	@Override
	public void playerStatusChanged(Table table, int playerId, PlayerStatus status) { }

	@Override
	public void seatReserved(Table table, GenericPlayer player) { }

}
