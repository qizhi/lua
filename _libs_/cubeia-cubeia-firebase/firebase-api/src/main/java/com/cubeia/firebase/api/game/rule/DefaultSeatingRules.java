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
package com.cubeia.firebase.api.game.rule;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.RemovePlayerAction;
import com.cubeia.firebase.api.action.ReserveSeatRequestAction;
import com.cubeia.firebase.api.action.WatchAction;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableSeatingMap;

/**
 * Default seating rules.
 * 
 * Will always allow watching and leaving, but disallow seating in occupied or
 * non existing seats.
 * 
 * @author Fredrik
 * 
 */
public class DefaultSeatingRules implements SeatingRules {

	private static transient Logger log = Logger.getLogger(DefaultSeatingRules.class);
	
    /**
     * Always allowed.
     * 
     * @see com.cubeia.firebase.api.game.rule.SeatingRules#actionAllowed(com.cubeia.firebase.api.action.LeaveAction,
     *      com.cubeia.firebase.api.game.table.Table)
     */
    public boolean actionAllowed(LeaveAction action, Table table) {
        return true;
    }

    /**
     * Always allowed.
     * 
     * @see com.cubeia.firebase.api.game.rule.SeatingRules#actionAllowed(com.cubeia.firebase.api.action.WatchAction,
     *      com.cubeia.firebase.api.game.table.Table)
     */
    public boolean actionAllowed(WatchAction action, Table table) {
        return true;
    }

	public boolean actionAllowed(RemovePlayerAction action, Table table) {
		return true;
	}
	
    /**
     * Returns <code>true</code> if the seat is vacant.
     * 
     * If the seatId is -1, true will be returned if there is any free seat.
     * 
     * @see com.cubeia.firebase.api.game.rule.SeatingRules#actionAllowed(com.cubeia.firebase.api.action.JoinRequestAction,
     *      com.cubeia.firebase.api.game.table.Table)
     */
    public boolean actionAllowed(JoinRequestAction action, Table table) {
    	// Check index first
    	TablePlayerSet playerSet = table.getPlayerSet();
        TableSeatingMap seating = playerSet.getSeatingMap();
    	if (action.getSeatId() >= seating.getNumberOfSeats()) {
    		if(log.isTraceEnabled()) {
    			log.trace("Player " + action.getNick() + "(" + action.getPlayerId() + ") was denied seat request for Table "+action.getTableId()+"("+action.getSeatId()+") since the requested seat id (" + action.getSeatId() + " is out of range (0-" + (seating.getNumberOfSeats() - 1) + ").");
    		}
    		return false;
    	}
    	
        boolean allowed = true;
        
        // First check so that player is not seated at different seat already
        int existingSeat = -1;
        GenericPlayer player = playerSet.getPlayer(action.getPlayerId());
        if (player != null) {
        	existingSeat = player.getSeatId();
        	// IF seat specified AND seat not the same as already taken seat
        	if (action.getSeatId() >= 0 && existingSeat != action.getSeatId()) {
        		allowed = false;	
        	} else {
        		// IF dynamic seat, set it to taken seat
        		action.setSeatId(existingSeat);
                return true;
        	}
        }

        if (!allowed) {
        	log.debug("Player "+action.getNick()+"("+action.getPlayerId()+") was denied seat request for Table "+action.getTableId()+"("+action.getSeatId()+") since he is seated at a different seat at the table ("+existingSeat+").");
        }
        
        // Check seats available and seat numbers
        allowed &= seating.getNumberOfSeats() > playerSet.getPlayerCount();
        
        if(!allowed) {
        	log.trace("Player " + action.getNick() + "(" + action.getPlayerId() + ") was denied seat request for Table "+action.getTableId()+"("+action.getSeatId()+") since there are no vacant seats available.");
        }

        // If dynamic seat or free seat
        if (action.getSeatId() == -1) {
            allowed &= tableHasAnyFreeSeats(seating);
            if(!allowed) {
            	if(log.isTraceEnabled()) {
            		log.trace("Player " + action.getNick() + "(" + action.getPlayerId() + ") was denied seat request for Table "+action.getTableId()+"("+action.getSeatId()+") since there are no vacant seats available.");
            	}
            }
        } else {
        	if (existingSeat == -1) {
        		allowed &= playerSet.getSeatingMap().isSeatVacant(action.getSeatId());   		
        		if(!allowed) {
                	if(log.isTraceEnabled()) {
                		log.trace("Player " + action.getNick() + "(" + action.getPlayerId() + ") was denied seat request for Table "+action.getTableId()+"("+action.getSeatId()+") since the seat is occupied.");
                	}
                }
        	}
         }
        
        return allowed;
    }

    /**
     * Checks if a table has any free seats.
     * 
     * @param table
     * @return <code>true</code> if the table has any free seats,
     *         <code>false</code> otherwise.
     */
    private boolean tableHasAnyFreeSeats(TableSeatingMap seating) {
    	return seating.hasVacantSeats();
    }

    
    
	public boolean actionAllowed(ReserveSeatRequestAction action, Table table) {
		// Check index first
    	TablePlayerSet playerSet = table.getPlayerSet();
        TableSeatingMap seating = playerSet.getSeatingMap();
    	if (action.getSeatId() >= seating.getNumberOfSeats()) {
    		return false;
    	}
    	
        boolean allowed = true;
        
        // First check so that player is not seated at different seat already.
        // If the player is seated already we will not allow a reserve at all.
        GenericPlayer player = playerSet.getPlayer(action.getPlayerId());
        if (player != null) {
        	allowed = false;
        }

        if (!allowed) {
        	log.debug("Player "+"("+action.getPlayerId()+") was denied reserve seat request for Table: "+action.getTableId()+"("+action.getSeatId()+") since he is already seated at a different seat at the table.");
        }
        
        // Check seats available
        allowed &= seating.getNumberOfSeats() > playerSet.getPlayerCount();

        // If dynamic seat or free seat
        if (action.getSeatId() == -1) {
            allowed &= tableHasAnyFreeSeats(seating);
        } else {
        	allowed &= playerSet.getSeatingMap().isSeatVacant(action.getSeatId());
         }
        
        return allowed;
	}


}