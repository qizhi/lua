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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Holds the seating of players in a tournament.
 *
 */
public class TournamentSeating implements Serializable {
    
	private static final Logger log = Logger.getLogger(TournamentSeating.class);
	
	private static final long serialVersionUID = 1L;
    
    /** Maps tableId to a table seating. */
    private Map<Integer, TableSeating> tableToSeatingMap = new HashMap<Integer, TableSeating>();
    
    /**
     * Constructor.
     */
    public TournamentSeating() {
    }

    /**
     * Copy constructor.
     * @param ts tournament seating to copy
     */
    protected TournamentSeating(TournamentSeating ts) {
        for (Map.Entry<Integer, TableSeating> e : ts.tableToSeatingMap.entrySet()) {
            tableToSeatingMap.put(e.getKey(), new TableSeating(e.getValue()));
        }
    }
    
    /**
     * Returns the table seating for the given table.
     * @param tableId the table id
     * @return the seating
     */
    public TableSeating getTableSeating(Integer tableId) {
        return tableToSeatingMap.get(tableId);
    }
    
    /**
     * Get a all table seatings.
     * @return all table seatings
     */
    public Collection<TableSeating> getTableSeatings() {
        return Collections.unmodifiableCollection(tableToSeatingMap.values());
    }
    
    /**
     * Adds a player to a table. 
     * 
     * If the destination table does not exist, a new entry will be created.
     * 
     * @param playerId
     * @param tableId
     * @param seat
     */
    public void addPlayerToTable(int playerId, int tableId, int seat) {
        TableSeating tableSeating = tableToSeatingMap.get(tableId);
        if (tableSeating == null) {
            tableSeating = new TableSeating(tableId);
            tableToSeatingMap.put(tableId, tableSeating);
        }
        
        tableSeating.addPlayer(playerId, seat);
    }
    
    /**
     * Gets a {@link Set} of the <code>playerId</code>s of all players in this seating.
     * 
     * The {@link Set} is defensively created, so modifying the {@link Set} has no
     * effect on the seating.
     * 
     * @return A {@link Set} of <code>playerId</code>s of all players in this seating.
     *         
     */
    public Set<Integer> getAllPlayers() {
        Set<Integer> playerIds = new HashSet<Integer>();
        for (TableSeating ts : tableToSeatingMap.values()) {
            playerIds.addAll(ts.getPlayers());
        }
        return playerIds;
    }
    
    /**
     * Returns all tables registered with this tournament seating.
     * @return all tables
     */
    public Set<Integer> getAllTables() {
        return Collections.unmodifiableSet(tableToSeatingMap.keySet());
    }
    
    /**
     * Returns true if the seating has orphan(s). An orphan is a single player on some table playing with himself.
     * @return true if this seating contains one or more orphans.
     */
    public boolean hasOrphans() {
        for (TableSeating ts : tableToSeatingMap.values()) {
            if (ts.getPlayers().size() == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((tableToSeatingMap == null) ? 0 : tableToSeatingMap
                        .hashCode());
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
        final TournamentSeating other = (TournamentSeating) obj;
        if (tableToSeatingMap == null) {
            if (other.tableToSeatingMap != null)
                return false;
        } else if (!tableToSeatingMap.equals(other.tableToSeatingMap))
            return false;
        return true;
    }

    /**
     * Removes a player from the table he is seated at.
     * 
     * @param playerId
     * @return true if the player was removed, false otherwise, meaning the player was not found
     */
    public boolean removePlayer(int playerId) {
        for (TableSeating ts : tableToSeatingMap.values()) {
            Integer removedPlayer = ts.removePlayer(playerId);
            if (removedPlayer != null) {
            	return true;
            }
        }
        return false;
    }
    
    /**
     * Removes a table from the seating. If the table does not exist in the seating map, no action is taken.
     * 
     * @param tableId
     * @throws IllegalStateException if the table is not empty
     */
    public void removeTable(int tableId) {
    	if (tableToSeatingMap.get(tableId) != null && tableToSeatingMap.get(tableId).getPlayers().size() != 0) {
    		throw new IllegalStateException("Tried to remove table " + tableId + " which is not empty.");
    	}
    	tableToSeatingMap.remove(tableId);
    }

    /**
     * Gets the tableId where the given player is seated.
     * 
     * @param playerId
     * @return the tableId, or null if the player is not seated at any table
     */
    public Integer getTableByPlayer(int playerId) {
        for (TableSeating ts : getTableSeatings()) {
            if (ts.getPlayers().contains(playerId)) {
                return ts.getTableId();
            }
        }
        log.warn("Player " + playerId + " does not sit at any table, returning null.");
        return null;
    }

    /**
     * Removes tables from the seating map. If one or more tables do not exist in the seating map, the tables are ignored.
     * 
     * @param tables
     * @throws IllegalStateException if any of the tables are not empty. In this case, no tables at all will be removed.
     */
	public void removeTables(Collection<Integer> tables) {
		for (Integer tableId : tables) {
	    	if (tableToSeatingMap.get(tableId) != null && tableToSeatingMap.get(tableId).getPlayers().size() != 0) {
	    		throw new IllegalStateException("Tried to remove table " + tableId + " which is not empty.");
	    	}			
		}

		for (Integer tableId : tables) {
			removeTable(tableId);
		}
	}
}
