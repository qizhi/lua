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

import static com.cubeia.firebase.api.action.UnseatPlayersMttAction.Reason.OUT;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.LeaveResponseAction;
import com.cubeia.firebase.api.action.MttPickedUpAction;
import com.cubeia.firebase.api.action.MttSeatedAction;
import com.cubeia.firebase.api.action.RemovePlayerAction;
import com.cubeia.firebase.api.action.SeatInfoAction;
import com.cubeia.firebase.api.action.SeatPlayersMttAction;
import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction;
import com.cubeia.firebase.api.action.SeatPlayersMttAction.PlayerContainer;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction.Reason;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttSeatingFailedAction;
import com.cubeia.firebase.api.game.lobby.DefaultLobbyMutator;
import com.cubeia.firebase.api.game.lobby.LobbyTableAccessor;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TournamentTableListener;

/**
 * Handles generic table actions for tournaments.
 *
 */
public class MttTableActionHandler extends AbstractTableActionHandler {

	private static transient Logger log = Logger.getLogger(MttTableActionHandler.class);
	
	/**
	 * Constructor.
	 * 
	 * @param table
	 * @param acc
	 * @param mut
	 */
	public MttTableActionHandler(Table table, LobbyTableAccessor acc, DefaultLobbyMutator mut) {
		super(table, acc, mut);
	}

	/**
	 * Handles a {@link JoinRequestAction} for a tournament table. The request will only be allowed if
	 * the player is already seated at this table.
	 */
    @Override
    public void visit(JoinRequestAction action) {
    	// only allow join if already seated
    	int seatId = getSeatIdForPlayer(action.getPlayerId());
    	boolean allowed = (seatId == -1) ? false : true;
    	
    	// default response is DENIED (2)
        InterceptionResponse allowedByInterceptor = new InterceptionResponse(false, -1);
        
        // Send the response before we tell the game that a player has joined.
        sendJoinResponse(action.getPlayerId(), action.getTableId(), seatId, allowed, allowedByInterceptor.getResponseCode());        
        
        // Add the player.
        if (allowed) {
            // Handle "seating"
            GenericPlayer player = seatMttPlayer(seatId, action.getPlayerId(), action.getNick());            

            // Report to the client registry (See Trac ticket #143 for the reason why we do this here) 
            log.debug("Register player["+action.getPlayerId()+"] to table["+table.getId()+"]");
            registerPlayerToTable(table.getId(), action.getPlayerId(), seatId, table.getMetaData().getMttId(), false);
            
            // Notify listener.
            notifyPlayerRejoined(player);
            
            // Update lobby if requested.
            if (mut != null && acc != null) {
            	mut.updateTable(acc, table);
            }
        }
    }
    
    /**
     * <p>Request for leaving a tournament table.</p>
     * 
     * <p>Since this is a tournament table we will not consult the table interceptor. The leave request
     * will never result in a player leaving the table (this would invalidate the tournament seating.
     * Instead we will set the player status to LEAVING. This is so the tournament will be aware of the client leaving the system.</p>
     * 
     * <p>This scenario will primarily happen when a client closes a table and an explicit leave request 
     * is sent from the client or when a client logs out with the leave_tables flag set to true (see visit(RemovePlayerAction)).</p> 
     * 
     * <p>The returned status will always be DENIED or FAILED. FAILED will only be sent in the event that the player
     * was not found at the table. This can happen if the player has been balanced to another table before the leave
     * request was received by the tournament table. However, the newly placed player will then be in a status different
     * from CONNECTED so the table will still be able to detect a non-connected player.</p>
     * 
     * @see com.cubeia.firebase.api.action.visitor.DefaultActionVisitor#visit(com.cubeia.firebase.api.action.LeaveAction)
     */
    @Override
    public void visit(LeaveAction action) {
        int pid = action.getPlayerId();
        Status status = handlePlayerLeaving(pid);
        
        // Send the response
        LeaveResponseAction response = new LeaveResponseAction(action.getPlayerId(), action.getTableId(), status.ordinal());
        response.setResponseCode(-1);
        getNotifier().sendToClient(action.getPlayerId(), response);
    }
    
    /**
     * <p>The system requested a player to be removed from the table.</p>
     * 
     * <p>We will not send a response here since the client is most likely
     * not connected anymore.</p>
     * 
     */
    @Override
    public void visit(RemovePlayerAction action) {
        int pid = action.getPlayerId();
        handlePlayerLeaving(pid);
    }

    /**
     * If the player is at the table then set status LEAVING, notify the listener and return status.
     * 
     * @param pid
     * @return DENIED if player present, FAILED if player not found at table.
     */
    private Status handlePlayerLeaving(int pid) {
        GenericPlayer player = playerSet.getPlayer(pid);
        
        // Default to DENIED status. We will only change this is player is not found at table
        Status status = Status.DENIED;
        if (player != null) {
            player.setStatus(PlayerStatus.LEAVING);

            // Notify listener.
            TableListener listener = table.getListener();
            if (listener != null) {
                listener.playerStatusChanged(table, pid, player.getStatus());
            }
        } else {
            status = Status.FAILED;
        }
        
        return status;
    }
    
    
	
    /**
     * Gets the seatId where the given player sits at this table.
     * 
     * @param playerId
     * @return the seatId or -1 if the player does not sit at this table
     */
	private int getSeatIdForPlayer(int playerId) {
		TablePlayerSet playerSet = table.getPlayerSet();
        GenericPlayer player = playerSet.getPlayer(playerId);
        if (player == null) {
			return -1;        	
        }
        return player.getSeatId();
	}

	/**
	 * Seats players at the table. This is called from the tournament manager and not from the client.
	 * 
	 */
	@Override
	public void visit(SeatPlayersMttAction action) {
		for (PlayerContainer seating : action.getPlayers()) {
			int seatId = seating.getSeatId() == -1 ? getFirstVacantSeatId() : seating.getSeatId();
			
			// Verify that we got a proper seat id.
			if (seatId < 0 || !table.getPlayerSet().getSeatingMap().getSeat(seatId).isVacant()) {
			    // We have failed to find a vacant seat. Log a warning and continue... 
			    log.warn("A tournament seating could not find a vacant seat for player["+seating.getPlayerId()+":"+seating.getNick()+"] at table["+table.getId()+"]. Action: "+action+" Seats["+table.getPlayerSet().getSeatingMap().getNumberOfSeats()+"] PlayersBefore["+table.getPlayerSet().getPlayerCount()+"]");
			    if (seatId >= 0 && !table.getPlayerSet().getSeatingMap().getSeat(seatId).isVacant()) {
			        log.info("Table["+table.getId()+"] seatId["+seatId+"] is occupied by: "+table.getPlayerSet().getSeatingMap().getSeat(seatId).getPlayer());
			    }
			    
                // Send a seating failed action to the tournament coordinator
			    MttAction failedAction = new MttSeatingFailedAction(action.getMttId(), seating.getPlayerId(), action.getTableId(), seating.getSeatId());
			    table.getTournamentNotifier().sendToTournament(failedAction);
			    
			    continue;
			}
			
			// Register the player at the table.
			registerPlayerToTable(action.getTableId(), seating.getPlayerId(), seatId, table.getMetaData().getMttId(), false);
			
	        // Send the response before we tell the game that a player has joined.
			sendMttSeatedAction(seating.getPlayerId(), table.getMetaData().getMttId(), action.getTableId(), seatId); 
			GenericPlayer player = seatPlayer(seatId, seating.getPlayerId(), seating.getNick(), PlayerStatus.DISCONNECTED);
	        
	        // Send a regular seating request to all other players.
			SeatInfoAction seatInfo = createSeatInfoAction(player, false);
	        getNotifier().notifyAllPlayersExceptOne(seatInfo, seating.getPlayerId());
	        
	        // log.debug("Seated MTT Player: pid["+seating.getPlayerId()+"] nick["+seating.getNick()+"] tid["+action.getTableId()+"] seat["+seating.getSeatId()+"]");
	        
            // Notify listener.
            notifyPlayerSeated(seating, player);
	        
            // Update lobby if requested.
            if (mut != null && acc != null) {
            	mut.updateTable(acc, table);
            }
		}
	}

	/**
	 * Notifies the tournament table listener that a player has been seated at the table.
	 * 
	 * @param seating
	 * @param player
	 */
	private void notifyPlayerSeated(PlayerContainer seating, GenericPlayer player) {
		TournamentTableListener tournamentListener = getTournamentTableListener(table);
		if(tournamentListener != null) {
			tournamentListener.tournamentPlayerJoined(table, player, seating.getPlayerData());
		}
	}

	/**
	 * Notifies the tournament table listener that a player has rejoined his seat at the table.
	 * 
	 * @param player
	 */
	private void notifyPlayerRejoined(GenericPlayer player) {
		TournamentTableListener tournamentListener = getTournamentTableListener(table);
		if(tournamentListener != null) {
			tournamentListener.tournamentPlayerRejoined(table, player);
		}
	}	
	
	/**
	 * Gets the tournament table listener.
	 * 
	 * @param table
	 * @return never null
	 * @throws ClassCastException if the {@link TableListener} is not a subclass of {@link TournamentTableListener}
	 */
	private TournamentTableListener getTournamentTableListener(Table table) {
		TableListener listener = table.getListener();
		if (listener == null || listener instanceof TournamentTableListener) {
			return (TournamentTableListener) listener;			
		} else {
			throw new ClassCastException("Table listener must be a tournament table listener.");
		}		
	}

	/**
	 * Unseats players from this table. This is called by the tournament manager.
	 * 
	 */
	@Override
	public void visit(UnseatPlayersMttAction action) {
	    // Unseat the players in two steps, first notify the other players, 
	    // then do the actual removal and sending the pickup actions
		for (UnseatPlayersMttAction.PlayerContainer pickup : action.getPlayers()) {
			notifyPlayerUnseated(pickup, action.getReason());
			
			// Send a regular player left to all other players.
	        LeaveAction notification = new LeaveAction(pickup.getPlayerId(), action.getTableId());  
	        getNotifier().notifyAllPlayersExceptOne(notification, pickup.getPlayerId());
	        
	        // log.debug("Picked up MTT Player: pid["+pickup.getPlayerId()+"] tid["+action.getTableId()+"] reason["+action.getReason()+"]");
		}
		
		// send pickup actions and do actual removal
		for (UnseatPlayersMttAction.PlayerContainer pickup : action.getPlayers()) {
			// Unregister the player from the table. seatId is not used when removing players 
			registerPlayerToTable(action.getTableId(), pickup.getPlayerId(), -1, table.getMetaData().getMttId(), true);

            removePlayer(pickup.getPlayerId());
            
            // Send a picked up action to the affected player
            boolean keepWatching = action.getReason() == OUT ? true : false;
            sendMttPickedUpAction(pickup.getPlayerId(), table.getMetaData().getMttId(), action.getTableId(), keepWatching); 
            
            if (keepWatching) {
            	registerWatcherToTable(action.getTableId(), pickup.getPlayerId(), false);
                addWatcher(pickup.getPlayerId());
            }
		}
		
        // Update lobby if requested.
        if (mut != null && acc != null) {
            mut.updateTable(acc, table);
        }
	}
	
	/**
	 * Notifies the tournament table listener that a player has left.
	 * 
	 * @param pickup
	 * @param reason
	 */
	private void notifyPlayerUnseated(com.cubeia.firebase.api.action.UnseatPlayersMttAction.PlayerContainer pickup, Reason reason) {
		TournamentTableListener tournamentListener = getTournamentTableListener(table);
		if(tournamentListener != null) {
			tournamentListener.tournamentPlayerRemoved(table, pickup.getPlayerId(), reason);
		}
	}

	/**
     * Sends a seated by tournament notification action to the seated player
     * 
     * @param action
     * @param accepted
     */
    protected void sendMttSeatedAction(int pid, int mttid, int tid, int seat) {
        MttSeatedAction response = new MttSeatedAction(pid, mttid, tid, seat, Status.OK.ordinal());
        getNotifier().sendToClient(pid, response);
    }
    
    /**
     * Sends a picked up by tournament notification action to the seated player
     * 
     * @param action
     * @param accepted
     */
    protected void sendMttPickedUpAction(int pid, int mttid, int tid, boolean keepWatching) {
        MttPickedUpAction response = new MttPickedUpAction(pid, mttid, tid, keepWatching);
        getNotifier().sendToClient(pid, response);
    }
}
