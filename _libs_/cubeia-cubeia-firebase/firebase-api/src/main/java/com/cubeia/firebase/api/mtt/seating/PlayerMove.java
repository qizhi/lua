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

import com.cubeia.firebase.api.util.Arguments;

/**
 * This class represents a player move from one table to another.
 * @author w
 */
public class PlayerMove {
    private Integer playerId;
    private Integer fromTable;
    private Integer toTable;
    private Integer fromSeat;
    private Integer toSeat;
    
    /**
     * Constructor.
     * @param playerId the player id
     * @param fromTable the table to move from, may be null in case of initial seating
     * @param toTable the table to move to
     */
    public PlayerMove(Integer playerId, Integer fromTable, Integer toTable) {
        this(playerId, fromTable, toTable, null, null);
    }
    
    /**
     * Constructor.
     * @param playerId the player id
     * @param fromTable the table to move from, may be null in case of initial seating
     * @param toTable the table to move to
     * @param fromSeat the seat to move from
     * @param toSeat the seat to move to
     */
    public PlayerMove(Integer playerId, Integer fromTable, Integer toTable, Integer fromSeat, Integer toSeat) {
        Arguments.notNull(playerId, "playerId");
        Arguments.notNull(toTable, "toTable");
        
        this.playerId = playerId;
        this.fromTable = fromTable;
        this.toTable = toTable;
        this.fromSeat = fromSeat;
        this.toSeat = toSeat == null ? -1 : toSeat;
    }
    
    
    public Integer getPlayerId() {
        return playerId;
    }
    
    public Integer getFromTable() {
        return fromTable;
    }
    
    public Integer getToTable() {
        return toTable;
    }

    public Integer getFromSeat() {
        return fromSeat;
    }
    
    public Integer getToSeat() {
        return toSeat;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fromSeat == null) ? 0 : fromSeat.hashCode());
        result = prime * result
                + ((fromTable == null) ? 0 : fromTable.hashCode());
        result = prime * result
                + ((playerId == null) ? 0 : playerId.hashCode());
        result = prime * result + ((toSeat == null) ? 0 : toSeat.hashCode());
        result = prime * result + ((toTable == null) ? 0 : toTable.hashCode());
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
        final PlayerMove other = (PlayerMove) obj;
        if (fromSeat == null) {
            if (other.fromSeat != null)
                return false;
        } else if (!fromSeat.equals(other.fromSeat))
            return false;
        if (fromTable == null) {
            if (other.fromTable != null)
                return false;
        } else if (!fromTable.equals(other.fromTable))
            return false;
        if (playerId == null) {
            if (other.playerId != null)
                return false;
        } else if (!playerId.equals(other.playerId))
            return false;
        if (toSeat == null) {
            if (other.toSeat != null)
                return false;
        } else if (!toSeat.equals(other.toSeat))
            return false;
        if (toTable == null) {
            if (other.toTable != null)
                return false;
        } else if (!toTable.equals(other.toTable))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
    	return "playerId=" + playerId + " fromSeat=" + fromSeat + " toSeat=" + toSeat + " fromTable=" + fromTable + " toTable=" + toTable;
    }
}
