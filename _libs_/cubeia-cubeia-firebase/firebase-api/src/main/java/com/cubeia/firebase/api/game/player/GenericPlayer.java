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
package com.cubeia.firebase.api.game.player;

import java.io.Serializable;

/**
 * <p>Model of a generic player.</p>
 * 
 * @author Fredrik
 */
public class GenericPlayer implements SeatedPlayer, Serializable {

	private static final long serialVersionUID = 4401397932284337605L;
	
	private int playerId;
	private String name;
	private int seatId = -1;
	private PlayerStatus status = PlayerStatus.CONNECTED;
	
	/**
	 * Empty constructor needed by serializer
	 */
	public GenericPlayer() {}
	
	public GenericPlayer(int playerId, String nick) {
		this.playerId = playerId;
		this.name = nick;
	}
	
    public GenericPlayer(GenericPlayer p) {
		this(p.playerId, p.name);
		status = p.status;
		seatId = p.seatId;
	}

	public String toString() {
        return name+"["+playerId+"]";
    }
    
	public int getPlayerId() {
		return playerId;
	}

	public String getName() {
		return name;
	}

	public int getSeatId() {
		return seatId;
	}

	public void setSeatId(int seatId) {
		this.seatId  = seatId;
	}
	
	public PlayerStatus getStatus() {
		return status;
	}

	public void setStatus(PlayerStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return playerId;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof GenericPlayer)) return false;
		return o.hashCode() == hashCode();
	}

	
}
