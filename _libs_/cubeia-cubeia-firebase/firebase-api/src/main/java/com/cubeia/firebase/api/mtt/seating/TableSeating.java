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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents the seating of a number of players at a table.
 *
 */
public final class TableSeating implements Externalizable {
    
	private static final long serialVersionUID = 1L;
    
	/** The table which seating is represented. */
	private Integer tableId;
    
	/** Maps playerId to seatId. */
	private Map<Integer, Integer> playerToSeatMap = new HashMap<Integer, Integer>();
    
	/**
     * Default constructor needed for custom serialization
     * 
     */
    public TableSeating() {}
	
    /**
     * Create a table seating with the given table id and number of seats.
     * @param tableId the table id
     * @param numberOfSeats number of seats
     */
    public TableSeating(Integer tableId) {
        this.tableId = tableId;
    }
    
    /**
     * Copy constructor.
     * @param tableSeating table seating to copy
     */
    protected TableSeating(TableSeating tableSeating) {
        this.tableId = tableSeating.tableId;
        playerToSeatMap.putAll(tableSeating.playerToSeatMap);
    }

    /**
     * Returns the table id.
     * @return
     */
    public Integer getTableId() {
        return tableId;
    }
    
    /**
     * Get all players on this table seating.
     * @return all players
     */
    public Collection<Integer> getPlayers() {
        if (playerToSeatMap.isEmpty()) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(playerToSeatMap.keySet());
        }
    }
    
    /**
     * Add a player with an undecided (-1) seat.
     * @param playerId the player to add
     */
    protected void addPlayer(Integer playerId) {
        addPlayer(playerId, -1);
    }

    /**
     * Add a player to the given seat position.
     * @param playerId the player id
     * @param seat the seat position
     */
    protected void addPlayer(Integer playerId, Integer seat) {
      if (seat  !=  null  &&  seat != -1  &&  playerToSeatMap.containsValue(seat)) {
          throw new IllegalStateException("seat already occupied");
      }
      
      playerToSeatMap.put(playerId, seat);
    }
    
    /**
     * Removes a player from this seating.
     * 
     * @param playerId
     * @return the playerId of the removed player, or null if no player was removed, meaning the player was not found. 
     */
    protected Integer removePlayer(Integer playerId) {
        return playerToSeatMap.remove(playerId);
    }
    
    /**
     * Removes a player from this seating.
     * 
     * @param playerId
     * @param seatId the seatId where the player sits
     * @return the playerId of the removed player
     * @throws IllegalArgumentException if the playerId does not exist in this seating or if the seatIds do not match
     */
    protected Integer removePlayer(Integer playerId, Integer seatId) {
        if (!playerToSeatMap.containsKey(playerId)  ||  playerToSeatMap.get(playerId) != seatId) {
            throw new IllegalStateException("player was not seated on the given position");
        }
        
        return removePlayer(playerId);
    }
    
    /**
     * Removes players from this seating. Players who are not seated will be ignored.
     * 
     * @param players the playerIds to remove
     */
    protected void removePlayers(Collection<Integer> players) {
        for (Integer pId : players) {
            removePlayer(pId);
        }
    }
    
    /**
     * Removes all players from this seating.
     */
    protected void removeAllPlayers() {
        playerToSeatMap.clear();
    }
    
    
    

    /*------------------------------------------------

        SERIALIZING METHODS

     ------------------------------------------------*/
    
    /**
     * Externalizable implementation.
     * NOTE: This method will be used instead of regular java serialization.
     * We have implemented this for more efficient serializing, i.e. less data.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        tableId = in.readInt();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            int pid = in.readInt();
            int seat = in.readInt();
            playerToSeatMap.put(pid, seat);
        }
    }

    /**
     * Externalizable implementation.
     * NOTE: This method will be used instead of regular java serialization.
     * We have implemented this for more efficient serializing, i.e. less data.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(tableId);
        out.writeInt(playerToSeatMap.size());
        for (Entry<Integer, Integer> entry : playerToSeatMap.entrySet()) {
            out.writeInt(entry.getKey());
            out.writeInt(entry.getValue());
        }
    }
}