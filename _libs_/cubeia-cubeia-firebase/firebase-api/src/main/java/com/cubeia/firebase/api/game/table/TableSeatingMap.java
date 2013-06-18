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

/**
 * THis map contains references between players and their current 
 * seat at a particular table. Seats are identified by index, and the
 * indexing starts at zero.
 *
 * @author lars.j.nilsson
 */
public interface TableSeatingMap {
	
	/**
	 * This method returns the number of seats at 
	 * a table.
	 * 
	 * @return The number of seats at the table
	 */
    public int getNumberOfSeats();
    
    
    /**
     * This method iterates all available seats and returns
     * the first vacant seat index. If no seats are vacant this
     * method return -1.
     * 
     * @return The first available seat, or -1
     */
    public int getFirstVacantSeat();

    
    /**
     * This method returns true if a specific seat is vacant. The
     * seats are indexed by zero.
     * 
     * @param seatId Seat id, indexed from zero
     * @return True if the seat is vacant, -1 if not or if it does not exist
     */
	public boolean isSeatVacant(int seatId);

	
	/**
	 * This method returns a seat object from the map. Seats
	 * are indexed by zero. If the seat does not exist, null will be
	 * returned.
	 * 
	 * @param seatId Seat id, indexed from zero
	 * @return A seat object, or null if not found
	 */
	public Seat<GenericPlayer> getSeat(int seatId);
	
	
	/**
	 * This method iterates through all available seats and 
	 * counts the number of seated players.
	 * 
	 * @return The number of seated players
	 */
	public int countSeatedPlayers();
	
	
	/**
	 * This method checks if any seats at the table
	 * are vacant. 
	 * 
	 * @return True if the table has a vacant seat, false otherwise
	 */
	public boolean hasVacantSeats();
	
}
