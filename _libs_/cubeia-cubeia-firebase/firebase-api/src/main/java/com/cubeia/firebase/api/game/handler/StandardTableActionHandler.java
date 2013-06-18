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
package com.cubeia.firebase.api.game.handler;

import java.util.Collections;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.LeaveResponseAction;
import com.cubeia.firebase.api.action.RemovePlayerAction;
import com.cubeia.firebase.api.action.ReserveSeatRequestAction;
import com.cubeia.firebase.api.action.SeatPlayersMttAction;
import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.game.lobby.DefaultLobbyMutator;
import com.cubeia.firebase.api.game.lobby.LobbyTableAccessor;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;

/**
 * Table action handler used for standard tables, i.e. cashgame tables.
 * 
 * This handler will ignore mtt seating requests.
 *
 * @author Fredrik
 */
public class StandardTableActionHandler extends AbstractTableActionHandler {

	private static transient Logger log = Logger.getLogger(StandardTableActionHandler.class);
	
	
	/*------------------------------------------------
		 
		CONSTRUCTOR(S)
	
	 ------------------------------------------------*/
	
	
	public StandardTableActionHandler(Table table, LobbyTableAccessor acc, DefaultLobbyMutator mut) {
		super(table, acc, mut);
	}

	
	
	
	/*------------------------------------------------
		 
		VISITOR METHODS
		
		Handles actions that are not covered in
		the abstract super class.
	
	 ------------------------------------------------*/
	
	
	/**
	 * Join a player at a regular table.
	 * 
     */
    @Override
    public void visit(JoinRequestAction action) {
    	boolean allowedBySeatingRules = seatingRules.actionAllowed(action, table);
        
    	// default response is DENIED (2)
        InterceptionResponse allowedByInterceptor = new InterceptionResponse(false, -1);
        
        if (allowedBySeatingRules) {
            // Get the seatId, or find the id of the first empty seat if the seatId is -1.
            int seatId = action.getSeatId() == -1 ? getFirstVacantSeatId() : action.getSeatId();
            action.setSeatId(seatId);
            
            // If we have an interceptor, we should consult it
            allowedByInterceptor = checkJoinInterceptor(table, action.getSeatId(), action.getPlayerId(), action.getParameters());            
        }
        
        boolean allowed = allowedBySeatingRules && allowedByInterceptor.isAllowed();
        
        // Send the response before we tell the game that a player has joined.
        sendJoinResponse(action.getPlayerId(), action.getTableId(), action.getSeatId(), allowed, allowedByInterceptor.getResponseCode());        
        
        // Add the player.
        if (allowedBySeatingRules && allowedByInterceptor.isAllowed()) {
            // Create player and seat him
            GenericPlayer player = seatPlayer(action.getSeatId(), action.getPlayerId(), action.getNick(), null);

            // Report to the client registry (See Trac ticket #143 for the reason why we do this here) 
            registerPlayerToTable(table.getId(), action.getPlayerId(), action.getSeatId(), table.getMetaData().getMttId(), false);
            
            getNotifier().notifyAllPlayersExceptOne(action, action.getPlayerId());
            
            // Notify listener.
            TableListener listener = table.getListener();
            if (listener != null) {
            	listener.playerJoined(table, player);
            }
            
            // Update lobby if requested.
            if (mut != null && acc != null) {
            	mut.updateTable(acc, table);
            }
        }
    }
    

    /**
     * <p>Request for leaving a table.</p>
     * 
     * <p>If the request is denied by the interceptor then we will set the status of the
     * player to LEAVING.</p>
     * 
     * 
     * @see com.cubeia.firebase.api.action.visitor.DefaultActionVisitor#visit(com.cubeia.firebase.api.action.LeaveAction)
     */
    @Override
    public void visit(LeaveAction action) {
        InterceptionResponse allowed = new InterceptionResponse(true, -1);
        Status status = Status.FAILED;
        
        int pid = action.getPlayerId();
        
        // Check if player is seated at table before anything else (Ticket #526)
        if (table.getPlayerSet().getPlayer(pid) != null) {
        	if (seatingRules.actionAllowed(action, table)) {
                allowed = handleRemovePlayer(pid);
        	} 
        	status = allowed.isAllowed() ? Status.OK : Status.DENIED;
        	 
        } else {
            // We have received a leave action for a player that is not seated at the table.
            // Set the response status to denied.
            allowed = new InterceptionResponse(false, -1);
        }
    	
    	// Send the response
    	LeaveResponseAction response = new LeaveResponseAction(action.getPlayerId(), action.getTableId(), status.ordinal());
    	response.setResponseCode(allowed.getResponseCode());
        getNotifier().sendToClient(action.getPlayerId(), response);
    }


    /**
     * The system requested a player to be removed from the table.
     * 
     * We will not send a response here since the client is most likely
     * not connected anymore.
     * 
     */
    @Override
    public void visit(RemovePlayerAction action) {
        int pid = action.getPlayerId();
    	if (seatingRules.actionAllowed(action, table)) {
            handleRemovePlayer(pid);
    	} 
    }
    
    
    @Override
    public void visit(SeatPlayersMttAction action) {
    	log.error("Error: You are trying to seat tournament players at a normal table. Tableid: " + table.getId());
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void visit(ReserveSeatRequestAction action) {
    	boolean allowedBySeatingRules = seatingRules.actionAllowed(action, table);
        
    	// default response is DENIED (2)
        InterceptionResponse allowedByInterceptor = new InterceptionResponse(false, -1);
        
        if (allowedBySeatingRules) {
            // Get the seatId, or find the id of the first empty seat if the seatId is -1.
            int seatId = action.getSeatId() == -1 ? getFirstVacantSeatId() : action.getSeatId();
            action.setSeatId(seatId);
            
            // If we have an interceptor, we should consult it
            // Reserve seat will use the same interceptor call as for a join request
            allowedByInterceptor = checkReservationInterceptor(table, action.getSeatId(), action.getPlayerId(), Collections.EMPTY_LIST);            
        }
        
        boolean allowed = allowedBySeatingRules && allowedByInterceptor.isAllowed();
        
        // Send the response before we tell the game that a player has reserved a seat (if applicable).
        sendReserveSeatResponse(action, allowed, allowedByInterceptor.getResponseCode());
        
        
        // Add the player.
        if (allowedBySeatingRules && allowedByInterceptor.isAllowed()) {
            // Create player and seat him (no nick for reservations)
            GenericPlayer player = reserveSeat(action.getSeatId(), action.getPlayerId());
            
            // Report to the client registry (See Trac ticket #143 for the reason why we do this here) 
            registerPlayerToTable(table.getId(), action.getPlayerId(), action.getSeatId(), table.getMetaData().getMttId(), false);
            
            scheduleStatusTimeout(player, PlayerStatus.RESERVATION);
            
            // Notify listener.
            TableListener listener = table.getListener();
            if (listener != null) {
            	listener.seatReserved(table, player);
            }
            
            // Update lobby if requested.
            if (mut != null && acc != null) {
            	mut.updateTable(acc, table);
            }
        }

    }


    
	/*------------------------------------------------
		 
		PRIVATE METHODS
		
	 ------------------------------------------------*/
    
    /**
     * Remove player and notify players if applicable by the table interceptor.
     * 
     * @param pid
     * @return the interceptor response
     */
	private InterceptionResponse handleRemovePlayer(int pid) {
		// If we have an interceptor, we should consult it
		InterceptionResponse allowed = checkLeaveInterceptor(table, pid);
		
		if (allowed.isAllowed()) {
		    // Trac #439: moved to top of block
		    // Notify listener
		    TableListener listener = table.getListener();
		    if (listener != null) {
		    	listener.playerLeft(table, pid);
		    }
			
			// Inform everyone.
			getNotifier().notifyAllPlayersExceptOne(new LeaveAction(pid, table.getId()), pid);  
			// Then remove player
			removePlayer(pid);
		    // Report to the client registry (See Trac ticket #143 for the reason why we do this here) 
			// Seat id does not matter
		    registerPlayerToTable(table.getId(), pid, -1, table.getMetaData().getMttId(), true);
			
		    /* Trac #439: moved to top of block
		     * Notify listener
		    TableListener listener = table.getListener();
		    if (listener != null) {
		    	listener.playerLeft(table, pid);
		    }*/
		    
		    // Update lobby if requested
		    if (mut != null && acc != null) {
		    	mut.updateTable(acc, table);
		    }
		    
		} else {
    		// Player was not allowed to leave so we need to set his status
    		// to Leaving and notify listeners.
    		GenericPlayer player = playerSet.getPlayer(pid);
    		if (player != null) {
    			player.setStatus(PlayerStatus.LEAVING);

                // Notify listener.
    			TableListener listener = table.getListener();
                if (listener != null) {
                	listener.playerStatusChanged(table, pid, player.getStatus());
                }
    		}
    	}
		
		return allowed;
	}
}
