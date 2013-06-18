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

import com.cubeia.firebase.api.game.player.Player;

/**
 * 
 * @author Fredrik
 * @param <T>
 */
public class GameSeat<T extends Player> implements Seat<T>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private int playerId = 0;
	private int seatId;
	private T player;
	
	/**
	 * Empty constructor needed by serializer
	 */
	public GameSeat() {}
	
	public GameSeat(int seatId) {
		this.seatId = seatId;
	}
	
	public GameSeat(Seat<T> seat) {
		playerId = seat.getPlayerId();
		seatId = seat.getId();
		player = seat.getPlayer();
	}
	
	public boolean isVacant() {
		return (!isOccupied());
	}

	public boolean isOccupied() {
		return player != null;
	}

	public void seat(T player) {
		this.playerId = player.getPlayerId();
		this.player = player;
	}

	public int getPlayerId() {
		return playerId;
	}
	
	public T getPlayer() {
		return player;
	}

	public int getId() {
		return seatId;
	}

	public String toString() {
		return "Seat: id: " + seatId + " player: " + player;
	}

	public void clear() {
		this.player = null;
		this.playerId = -1;
	}
}
