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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.CleanupPlayerAction;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.JoinResponseAction;
import com.cubeia.firebase.api.action.PlayerInfoAction;
import com.cubeia.firebase.api.action.PlayerStatusAction;
import com.cubeia.firebase.api.action.ProbeAction;
import com.cubeia.firebase.api.action.ReserveSeatRequestAction;
import com.cubeia.firebase.api.action.ReserveSeatResponseAction;
import com.cubeia.firebase.api.action.ScheduledGameAction;
import com.cubeia.firebase.api.action.SeatInfoAction;
import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.TableChatAction;
import com.cubeia.firebase.api.action.TableQueryRequestAction;
import com.cubeia.firebase.api.action.TableQueryResponseAction;
import com.cubeia.firebase.api.action.UnWatchAction;
import com.cubeia.firebase.api.action.UnWatchResponseAction;
import com.cubeia.firebase.api.action.WatchAction;
import com.cubeia.firebase.api.action.WatchResponseAction;
import com.cubeia.firebase.api.common.ActionUtils;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.game.lobby.DefaultLobbyMutator;
import com.cubeia.firebase.api.game.lobby.LobbyTableAccessor;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.rule.SeatingRules;
import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.SeatRequest;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableSeatingMap;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.Enums.WatchResponseStatus;

/**
 * Default implementation of handling actions on a table.
 * 
 *
 * @author Fredrik
 */
public abstract class AbstractTableActionHandler extends AbstractActionHandler {   
	
	/**
	 * FIXME: Make these times configurable
	 */
	public static final int DEFAULT_PLAYER_RESERVATION_TIMEOUT_MS = 30*1000; // 30 sec
	public static final int DEFAULT_PLAYER_RECONNECT_TIMEOUT_MS = 120*1000;  // 120 sec

	private final static transient Logger log = Logger.getLogger(AbstractTableActionHandler.class);
	
	/** 
	 * Seating rules for the table, if not specified the
	 * DefaultSeatingRules will be used.
	 * 
	 * Inject this.
	 */
	protected SeatingRules seatingRules;
	
	/** The table to handle */
	protected final Table table;

	/** Used for updating the lobby. */
	protected final DefaultLobbyMutator mut;

	/** Used for accessing the lobby. */
	protected final LobbyTableAccessor acc;
	
	/** Defines if chat is allowed. */
	private boolean chatAllowed = true;
	
	/** Defines whether we should update lobby when a watcher joins. */
	private boolean updateLobbyOnWatcherJoin = true;
	
	/** Defines whether we should update lobby when a watcher leaves. */
    private boolean updateLobbyOnWatcherLeft = true;

    /** The set of players at the table handling the action for. */
	protected final TablePlayerSet playerSet;

	/** Used for accessing services. */
	private ServiceRegistry serviceRegistry;
	
	private int playerReservationTimeout = DEFAULT_PLAYER_RESERVATION_TIMEOUT_MS;
	private int playerReconnectTimeout = DEFAULT_PLAYER_RECONNECT_TIMEOUT_MS;
	

	/*------------------------------------------------
	 
		CONSTRUCTOR(S)

	 ------------------------------------------------*/
	
	/**
	 * If the mutator given to this constructor is non-null it will be used to
	 * update the lobby data when a player leaves/joins.
	 * 
	 * @param table Table for the handler, must not be null
	 * @param acc Lobby table accessor, may be null (only if "mut" is also null)
	 * @param mut Lobby mutator, may be null
	 */
    public AbstractTableActionHandler(Table table, LobbyTableAccessor acc, DefaultLobbyMutator mut) {
    	this.table = table;
    	playerSet = table.getPlayerSet();
		this.mut = mut;
		this.acc = acc;
    }

	/*------------------------------------------------
	 
		ACCESSORS AND MUTATORS

	 ------------------------------------------------*/
    
	/**
	 * @return Returns the table.
	 */
	public Table getTable() {
		return table;
	}

	public void requestActions(Table table) {
	    // Do nothing.
	}	
	
	/**
	 * Set the player reconnect timeout in milliseconds. Default value
	 * for this timeout is 2 minutes (120000 millis). If set to -1 the 
	 * default value will be used.
	 * 
	 * @param millis Timeout in millis, or -1 for default value
	 */
	public void setPlayerReconnectTimeout(int millis) {
		if(millis != -1) {
			this.playerReconnectTimeout = millis;
		} else {
			this.playerReconnectTimeout = DEFAULT_PLAYER_RECONNECT_TIMEOUT_MS;
		}
	}
	
	
	/**
	 * Set the player reservation timeout in milliseconds. Default value
	 * for this timeout is 30 seconds (30000 millis). If set to -1 the 
	 * default value will be used.
	 * 
	 * @param millis Timeout in millis, or -1 for default value
	 */
	public void setPlayerReservationTimeout(int millis) {
		if(millis == -1) {
		    this.playerReservationTimeout = DEFAULT_PLAYER_RESERVATION_TIMEOUT_MS;
		} else {
		    this.playerReservationTimeout = millis;
		}
	}
	
	
	 /**
	 * @return the seatingRules
	 */
	public SeatingRules getSeatingRules() {
		return seatingRules;
	}

	/**
	 * @param seatingRules the seatingRules to set
	 */
	public void setSeatingRules(SeatingRules seatingRules) {
		this.seatingRules = seatingRules;
	}
    
	/**
	 * Injection needed
	 * @param registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	
	/*------------------------------------------------
	 
		VISITOR METHODS

	 ------------------------------------------------*/
	

	
    /**
     * The correct return sequence should be:
     * 
     * 1. WatchResponse
     * 2. PlayerInfos
     * 3. TableListener.notifyWatcherJoined
     * 
     * @see se.tain.game.engine.visitor.GameActionVisitor#visit(se.tain.game.engine.action.WatchAction)
     */
    @Override
    public void visit(WatchAction action) {
    	GenericPlayer player = table.getPlayerSet().getPlayer(action.getPlayerId());
    	WatchResponseStatus status = WatchResponseStatus.FAILED;
        
    	// Only add a watcher if not a player at the table
    	if (player == null) {
            status = WatchResponseStatus.OK;
	    	addWatcher(action.getPlayerId());
	    	registerWatcherToTable(table.getId(), action.getPlayerId(), false);
    	} else {
            status = WatchResponseStatus.DENIED_ALREADY_SEATED;
        }
        
    	// Send response to client
        WatchResponseAction response = new WatchResponseAction(action.getTableId(), status);
        getNotifier().sendToClient(action.getPlayerId(), response);   
        
        if (status == WatchResponseStatus.OK) {
	    	// Send player list to new watcher after we have sent a watch response
	    	sendGameState(action.getPlayerId());
	    	
	        // Notify listener, must be done 
	        TableListener listener = table.getListener();
	        if (listener != null) {
	        	listener.watcherJoined(table, action.getPlayerId());
	        }
	    	
            // Update lobby if requested.
            if (updateLobbyOnWatcherJoin && mut != null && acc != null) {
            	mut.updateTable(acc, table);
            }
        }
    }
    
    @Override
    public void visit(UnWatchAction action) {
    	table.getWatcherSet().removeWatcher(action.getPlayerId());
    	if (!action.isByServer()) {
    		// Only remove table association if this is a player-generated action
    		registerWatcherToTable(table.getId(), action.getPlayerId(), true);
    	}
    	
        // Notify listener
        TableListener listener = table.getListener();
        if (listener != null) {
        	listener.watcherLeft(table, action.getPlayerId());
        }
        
        // Update lobby if requested.
        if (updateLobbyOnWatcherLeft && mut != null && acc != null) {
        	mut.updateTable(acc, table);
        }
        
        // Only send response if not a server-originated request
        if (!action.isByServer()) {
	        Status status = Status.OK;
	        UnWatchResponseAction response = new UnWatchResponseAction(action.getTableId(),status.ordinal());
	        getNotifier().sendToClient(action.getPlayerId(), response);
        }
    }
    
    @Override
    public void visit(PlayerStatusAction action) {
    	GenericPlayer player = table.getPlayerSet().getPlayer(action.getPlayerId());
    	
    	if (player != null) {
    		PlayerStatus status = action.getStatus();
    		// Only trigger changes if the status *is* different
    		if (!player.getStatus().equals(status)) {
	    		player.setStatus(status);
	    		scheduleStatusTimeout(player, status);
	    		
		        // Notify listener
		        TableListener listener = table.getListener();
		        if (listener != null) {
		        	listener.playerStatusChanged(table, action.getPlayerId(), action.getStatus());
		        }
    		}
    	}
    }

	
    
    /**
     * Return the probe
     */
    @Override
    public void visit(ProbeAction action) {
        // Return the probe
    	ActionUtils.checkAddTimestamping(action, getClass());
    	getNotifier().sendToClient(action.getPlayerId(), action);
    }
    
    /**
     * Send the chat message to all participants
     */
    @Override
    public void visit(TableChatAction action) {
    	if (chatAllowed) {
    		// Ticket #367: send to all, including the sender...
    		// getNotifier().notifyAllPlayersExceptOne(action, action.getPlayerId());
    		getNotifier().notifyAllPlayers(action);
    	}
    }
    
    @Override
    public void visit(TableQueryRequestAction action) {
    	log.debug("TableQueryRequestAction received");
    	List<SeatInfoAction> seatInfos = new ArrayList<SeatInfoAction>(table.getPlayerSet().getPlayerCount());
    	for (GenericPlayer p : table.getPlayerSet().getPlayers()) {
    		seatInfos.add(createSeatInfoAction(p, true));
    	}
    	TableQueryResponseAction response = new TableQueryResponseAction(action.getPlayerId(), action.getTableId(), seatInfos);
    	response.setStatus(Status.OK);
    	table.getNotifier().sendToClient(action.getPlayerId(), response);
    }        
    
    
    /**
     * This is a request to clean up a player object in a transient state (i.e. timeout).
     * Examine if we should handle this action.
     * 
     */
    @Override
    public void visit(CleanupPlayerAction action) {
    	GenericPlayer player = table.getPlayerSet().getPlayer(action.getPlayerId());
    	if (player != null) {
    		//  Check if status is the same as when the check was scheduled.
    		if (player.getStatus().equals(action.getCheckStatus())) {
    			
    			// Apply different strategies for different statuses
    			if (action.getCheckStatus().equals(PlayerStatus.WAITING_REJOIN)) {
    				// WAIT_REJOIN
	    			// We will set the player to DISCONNECTED and the game will
	    			// have to remove the player when applicable
	    			player.setStatus(PlayerStatus.DISCONNECTED);
	    			
	    			// Remove table association for the client
	    			registerPlayerToTable(table.getId(), player.getPlayerId(), -1, -1, true);
	    			
	    	        // Notify listener
	    	        TableListener listener = table.getListener();
	    	        if (listener != null) {
	    	        	listener.playerStatusChanged(table, player.getPlayerId(), PlayerStatus.DISCONNECTED);
	    	        }
	    	        
    			} else {
    				// RESERVED
    				// We will simply remove the player. No listener is informed
    				// (the player never really entered the game).
    				table.getPlayerSet().removePlayer(player.getPlayerId());
    				
    				// Notify all players & watchers
    				// Ticket #524 - Since we do not notify when a seat is reserved we should not
    				// notify when a reservation is removed. We might change this later /FJ
    				// LeaveAction notifyAction = new LeaveAction(player.getPlayerId(), table.getId());
    				// getNotifier().notifyAllPlayersExceptOne(notifyAction, player.getPlayerId());  
    			}
    		}
    	}
    }
    
    
    /**
     * Inspect and only handle scheduled internal events.
     */
    @Override
    public void visit(ScheduledGameAction action) {
    	action.getScheduledAction().visit(this);
    }
    
	
	/*------------------------------------------------
	 
		PROTECTED METHODS
		
		Methods that are available to the 
		implementing table handlers.

	 ------------------------------------------------*/

    /**
     * Sends a response packet for a join request.
     * 
     * @param action
     * @param accepted
     * @param responseCode 
     */
    protected JoinResponseAction sendJoinResponse(int pid, int tid, int seat, boolean accepted, int responseCode) {
        Status status = accepted ? Status.OK : Status.DENIED;
        JoinResponseAction response = new JoinResponseAction(pid, tid, seat, status.ordinal());
        response.setResponseCode(responseCode);
        getNotifier().sendToClient(pid, response);
        return response;
    }
    
    /**
     * Sends a response packet for a reserve seat request.
     * 
     * @param action
     * @param accepted
     * @param responseCode 
     */
    protected ReserveSeatResponseAction sendReserveSeatResponse(ReserveSeatRequestAction action, boolean accepted, int responseCode) {
        Status status = accepted ? Status.OK : Status.DENIED;
        ReserveSeatResponseAction response = new ReserveSeatResponseAction(action.getPlayerId(), action.getTableId(), action.getSeatId(), status.ordinal());
        response.setResponseCode(responseCode);
        response.setWaitingList(action.isWaitingList());
        response.setWaitingListId(action.getWaitingListId());
        response.setWaitingListSequence(action.getWaitingListSequence());
        getNotifier().sendToClient(action.getPlayerId(), response);
        return response;
    }

	protected InterceptionResponse checkJoinInterceptor(Table table, int seatId, int playerId, List<Attribute> parameters) {
		TableInterceptor interceptor = table.getInterceptor();
		if (interceptor != null)  {
			SeatRequest request = new SeatRequest(seatId, playerId, parameters);
			InterceptionResponse r = interceptor.allowJoin(table, request);
			return checkNullInterceptionResponse(r);
		} else {
			return new InterceptionResponse(true, -1);
		}
	}

	protected InterceptionResponse checkReservationInterceptor(Table table, int seatId, int playerId, List<Attribute> parameters) {
		TableInterceptor interceptor = table.getInterceptor();
		if (interceptor != null)  {
			SeatRequest request = new SeatRequest(seatId, playerId, parameters);
			InterceptionResponse r = interceptor.allowReservation(table, request);
			return checkNullInterceptionResponse(r);
		} else {
			return new InterceptionResponse(true, -1);
		}
	}	
	
	/**
     * Gets the seatId of the first vacant seat.
     * 
     * @return the seatId of the first vacant seat, or -1 if none found.
     */
    protected int getFirstVacantSeatId() {
        TableSeatingMap seatingMap = table.getPlayerSet().getSeatingMap();
        return seatingMap.getFirstVacantSeat();
    }
    
    /**
     * This method will also remove the player as watcher for good measure.
     * 
     * @param playerId
     */
    protected void removePlayer(int playerId) {
		table.getPlayerSet().removePlayer(playerId);		
		table.getWatcherSet().removeWatcher(playerId);
	}
	
    protected void addWatcher(int pid) {
		table.getWatcherSet().addWatcher(pid);
	}
     
	/**
	 * Create a player object and seat him at the given seat.
	 * 
	 * @param action
	 * @param seatId
	 * @return
	 */
	protected GenericPlayer seatPlayer(int seatId, int pid, String nick, PlayerStatus status) {
		// Create a new player and seat him.
		GenericPlayer player = new GenericPlayer(pid, nick);
		// Add player and remove him as watcher (if applicable)
		table.getPlayerSet().addPlayer(player, seatId);
		
		/*
		 * Used by mtt to seat players at status "disconnected".
		 */
		if(status != null) {
			player.setStatus(status);
		}
		
		if (!table.getWatcherSet().isWatching(player.getPlayerId())) {
			// If the player has not watched the game, we will send out
			// the player list.
			sendGameState(player.getPlayerId());
		}
		
		table.getWatcherSet().removeWatcher(player.getPlayerId());
		return player;
	}
	
	/**
	 * Handle seat/join for an MTT player a player object and seat him at the given seat.
	 * 
	 * @param action
	 * @param seatId
	 * @return
	 */
	protected GenericPlayer seatMttPlayer(int seatId, int pid, String nick) {
		GenericPlayer player = table.getPlayerSet().getPlayer(pid);
		if(player == null) {
			log.warn("Could not find player " + pid + " in player set, although he is reported as seated!");
			// Create a new player and seat him.
			player = new GenericPlayer(pid, nick);
			// Add player and remove him as watcher (if applicable)
			table.getPlayerSet().addPlayer(player, seatId);
		}
		
		/*
		 * The status has to be set to CONNECTED, otherwise nothing will be sent to the client.
		 * TODO: Should we report a status changed?
		 */ 
		player.setStatus(PlayerStatus.CONNECTED);
		sendGameState(pid);
		
		// This really shouldn't be possible, but let's be defensive...
		table.getWatcherSet().removeWatcher(pid);
		return player;
	}
	
	/**
	 * Create a player object and seat him at the given seat.
	 * 
	 * @param action
	 * @param seatId
	 * @return
	 */
	protected GenericPlayer reserveSeat(int seatId, int pid) {
		// Create a new player and seat him. No nick is used for reservations.
		GenericPlayer player = new GenericPlayer(pid, "");
		player.setStatus(PlayerStatus.RESERVATION);
		
		// Add player and remove him as watcher (if applicable)
		table.getPlayerSet().addPlayer(player, seatId);
		
		table.getWatcherSet().removeWatcher(player.getPlayerId());
		return player;
	}
	
	protected InterceptionResponse checkLeaveInterceptor(Table table, int playerId) {
		TableInterceptor interceptor = table.getInterceptor();
		if (interceptor != null)  {
			InterceptionResponse r = interceptor.allowLeave(table, playerId);
			return checkNullInterceptionResponse(r);
		} else {
			return new InterceptionResponse(true, -1);
		}
	}
    
	/**
	 * Register a client as player with a table. This will only be used for reconnection
	 * purposes in the client node.
	 * 
	 * @param tableid
	 * @param playerid
	 * @param seat
	 */
    protected void registerPlayerToTable(int tableid, int playerid, int seat, int mttId, boolean remove) {
    	if (serviceRegistry != null) {
	    	PublicClientRegistryService clientRegistry = serviceRegistry.getServiceInstance(PublicClientRegistryService.class);
			clientRegistry.registerPlayerToTable(tableid, playerid, seat, mttId, remove);
    	}
    }
    
    /**
	 * Register a client as watcher with a table. This will only be used for reconnection
	 * purposes in the client node.
	 * 
	 * @param tableid
	 * @param playerid
	 */
    protected void registerWatcherToTable(int tableid, int playerid, boolean remove) {
    	if (serviceRegistry != null) {
	    	PublicClientRegistryService clientRegistry = serviceRegistry.getServiceInstance(PublicClientRegistryService.class);
			clientRegistry.registerWatcherToTable(tableid, playerid, remove);
    	}
    }
    
    /**
     * We need to schedule a timeout for certain statuses
     * 
     * @param player
     * @param status
     */
    protected void scheduleStatusTimeout(GenericPlayer player, PlayerStatus status) {
    	CleanupPlayerAction schedule = new CleanupPlayerAction(player.getPlayerId(), table.getId(), status);
		if (status.equals(PlayerStatus.WAITING_REJOIN)) {
			table.getScheduler().scheduleAction(schedule, playerReconnectTimeout);
			
		} else if (status.equals(PlayerStatus.RESERVATION)) {
			table.getScheduler().scheduleAction(schedule, playerReservationTimeout);
		}
	}
    
    
	
	/*------------------------------------------------
	 
		PRIVATE METHODS
		
	 ------------------------------------------------*/
    
	/*
	 * If the response is null we return a new response with true/-1, else the
	 * original object
	 */
	private InterceptionResponse checkNullInterceptionResponse(InterceptionResponse r) {
		if(r == null) {
			return new InterceptionResponse(true, -1);
		} else {
			return r;
		}
	}
	
	/**
     * Sends the current game state to a player.
     * 
     */
	protected void sendGameState(int playerId) {
		List<GameAction> actions = new ArrayList<GameAction>(table.getPlayerSet().getPlayerCount());
		// Create player info actions.
		for (GenericPlayer player : table.getPlayerSet().getPlayers()) {
			actions.add(createSeatInfoAction(player, false));
		}
		getNotifier().sendToClient(playerId, actions);
	}

    /**
     * @param player
     * @param fromLobby specifies if the player info action is requested from the lobby or not
     * @return the created player info
     */
    protected SeatInfoAction createSeatInfoAction(GenericPlayer player, boolean fromLobby) {
        PlayerInfoAction playerInfo = new PlayerInfoAction(player.getPlayerId(), table.getId());
        setExtendedData(player.getPlayerId(), playerInfo, fromLobby);
        playerInfo.setNick(player.getName());
        SeatInfoAction seatInfo = new SeatInfoAction(player.getPlayerId(), table.getId());
        seatInfo.setSeatId(player.getSeatId());
        seatInfo.setStatus(player.getStatus());
        seatInfo.setPlayerInfo(playerInfo);
        return seatInfo;
    }   
    
    private void setExtendedData(int playerId, PlayerInfoAction playerInfo, boolean fromLobby) {
    	ExtendedDetailsProvider provider = table.getExtendedDetailsProvider();
		if (provider != null) {
    		List<Param> extendedDetails = provider.getExtendedDetails(table, playerId, fromLobby);
    		if (extendedDetails != null) {
    			playerInfo.setDetails(extendedDetails);
    		}
    	}
	}
    
	
    
   
}