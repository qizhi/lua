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

import java.io.Serializable;

import com.cubeia.firebase.api.action.UnseatPlayersMttAction.Reason;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TournamentTableListener;

/**
 * This is an adapter class for a tournament table listener.
 * 
 * @author Lars J. Nilsson
 */
public class TournamentTableListenerAdapter extends TableListenerAdapter implements TournamentTableListener {

	@Override
	public void tournamentPlayerJoined(Table table, GenericPlayer player, Serializable serializable) { }

	@Override
	public void tournamentPlayerRemoved(Table table, int playerId, Reason reason) { }

	@Override
	public void tournamentPlayerRejoined(Table table, GenericPlayer player) { }

}
