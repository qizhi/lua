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
package com.cubeia.firebase.api.mtt.seating;

import java.io.Serializable;

import com.cubeia.firebase.api.action.SeatPlayersMttAction;
import com.cubeia.firebase.api.game.table.Table;

/**
 * Represents a seating of a player at a table. Used for telling Firebase to seat a player at a {@link Table}. 
 * 
 * Holds the <code>playerId</playerId> of the seated player, the <code>tableId</code> of the table where
 * he is seated and the <code>seatId</code> which represents the seat at the table. Additional game specific <code>playerData</code> can be added
 * if the table where the player is being seated needs to know more details. For example, if a player is moved from
 * one table to another, the player's current balance might be attached in the <code>playerData</code>.
 * <p>
 * Instances of this class have a short life cycle within Firebase and will not be serialized. They are only used for creating a message
 * which will then be sent to the {@link Table} (in a {@link SeatPlayersMttAction}), after which the instance will be garbage collected.
 * <p>
 * NOTE. When attaching game specific <code>playerData</code> the class loader will complain (with a {@link ClassNotFoundException})
 * if the class has not been loaded from the top level class loader. The workaround for this is currently to put the jar containing 
 * the class under lib/common.
 *
 */
public final class SeatingContainer {
    
	private int playerId;    
	private int tableId;
	private int seatId;
    
	private Serializable playerData;

	/**
	 * Constructor.
	 * 
	 * @param playerId
	 * @param tableId
	 * @param seatId the seatId or -1 for first available seat.
	 * @param playerData
	 */
    public SeatingContainer(int playerId, int tableId, int seatId, Serializable playerData) {   	
    	this.playerId = playerId;
    	this.tableId = tableId;
    	this.seatId = seatId;
    	this.playerData = playerData;
    }

    /**
     * Constructor.
     *  
     * @param playerId
     * @param tableId
     */
    public SeatingContainer(int playerId, int tableId) {
        this(playerId, tableId, -1, null);
    }
    
    /**
     * Constructor.
     * 
     * @param playerId
     * @param tableId
     * @param playerData game specific player data, may be null.
     */
    public SeatingContainer(int playerId, int tableId, Serializable playerData) {
        this(playerId, tableId, -1, playerData);
    }
    
    /**
     * Get the playerId.
     * 
     * @return the playerId
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Gets the tableId.
     * 
     * @return the tableId.
     */
    public int getTableId() {
        return tableId;
    }

    /**
     * Gets the seatId.
     * 
     * @return the seatId
     */
    public int getSeatId() {
        return seatId;
    }
    
    /**
     * Gets the playerData.
     * 
     * @return the playerData or null if not set
     */
    public Serializable getPlayerData() {
		return playerData;
	}    
    
    @Override
    public String toString() {
        return "pId = " + playerId + ", tId = " + tableId + ", sId = " + seatId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + playerId;
        result = prime * result + seatId;
        result = prime * result + tableId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SeatingContainer other = (SeatingContainer) obj;
        if (playerId != other.playerId)
            return false;
        if (seatId != other.seatId)
            return false;
        if (tableId != other.tableId)
            return false;
        return true;
    }    
}
