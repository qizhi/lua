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

import com.cubeia.firebase.api.common.Attribute;

/**
 * <p>Value object containing information for a join seat request.</p>
 * 
 * <p>The parameter list will be empty for all reserve seat requests.</p>
 * 
 * @author Fredrik
 *
 */
public class SeatRequest {
	
	private final int seat;
	private final int playerId;
	private final List<Attribute> parameters;

	public SeatRequest(int seat, int playerId, List<Attribute> parameters) {
		this.seat = seat;
		this.playerId = playerId;
		this.parameters = parameters;
	}

	public String toString() {
		return "SeatRequest pid["+playerId+"] seat["+seat+"] params"+parameters;
	}
	
	public List<Attribute> getParameters() {
		return parameters;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getSeat() {
		return seat;
	}
	
}
