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
package com.cubeia.firebase.api.mtt.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.SeatPlayersMttAction;
import com.cubeia.firebase.api.action.StartMttRoundAction;
import com.cubeia.firebase.api.action.StopMttRoundAction;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction;
import com.cubeia.firebase.api.action.UnseatPlayersMttAction.Reason;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.action.mtt.MttObjectAction;
import com.cubeia.firebase.api.action.mtt.MttRoundReportAction;
import com.cubeia.firebase.api.action.mtt.MttSeatingFailedAction;
import com.cubeia.firebase.api.action.mtt.MttTablesCreatedAction;
import com.cubeia.firebase.api.action.mtt.ScheduledMttAction;
import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.game.activator.MttAwareActivator;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.lobby.LobbyAttributeAccessor;
import com.cubeia.firebase.api.mtt.MTTLogic;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.api.mtt.model.MttPlayer;
import com.cubeia.firebase.api.mtt.seating.SeatingContainer;
import com.cubeia.firebase.api.mtt.seating.TableSeating;
import com.cubeia.firebase.api.mtt.seating.TournamentSeating;
import com.cubeia.firebase.api.mtt.support.jmx.JmxHandler;
import com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor;
import com.cubeia.firebase.api.mtt.support.registry.PlayerListener;
import com.cubeia.firebase.api.mtt.support.registry.PlayerRegistry;
import com.cubeia.firebase.api.mtt.support.tables.MttTableCreator;
import com.cubeia.firebase.api.mtt.support.visitor.MttDefaultVisitor;
import com.cubeia.firebase.api.util.Arguments;


/**
 * <p>MTT Support implements basic default behavior for MTTs.</p>
 * 
 * <p>Extending this class provides an easier entry for writing your own tournament logic.</p>
 * 
 * <p><em>Failed Seatings</em></br>
 * Failed seatings are exceptions and are not considered part of normal execution. However, since
 * the seating mechanism are controlled by third part we cannot guarantee that all seating requests
 * are valid (e.g. someone can try to seat players on non-valid seats). Should this happen, the table
 * will send a MttSeatingFailedAction to the tournament. If you are interested in handling this 
 * exception case then you need to override the visitor method for that action.</p>
 *
 * @author Fredrik
 */
public abstract class MTTSupport implements MTTLogic {

	private static transient Logger log = Logger.getLogger(MTTSupport.class);

	/** The delay after which a table is removed. Immediate removal won't work as there may be pending events for the table. */
	private static final long DELAYED_TABLE_REMOVAL_MS = 5000;

    /**
     * Holds JMX beans for all MTT Instances running under this MTT implementation.
     */
    private JmxHandler jmxHandler;
    
    private final ThreadLocal<MttNotifier> notifierLocal = new ThreadLocal<MttNotifier>();
    private final ThreadLocal<MttTableCreator> creator = new ThreadLocal<MttTableCreator>();
    
    /**
     * Default constructor.
     */
	public MTTSupport() {
		jmxHandler = new JmxHandler();
	}
	
	/**
	 * <p>The action will be handled by a default visitor.</p>
	 * 
	 * <p>Any MTT Transport Action or actions not handled
	 * by the default visitor (TBD) will be sent back to 
	 * the implementing MTT Support class for processing.</p>
	 */
	public final void handle(MttAction action, MttInstance mttInstance) {
		pre(mttInstance);
		try {
			MTTState state = mttInstance.getState();
			if (state instanceof MTTStateSupport) {
				MttDefaultVisitor visitor = new MttDefaultVisitor(this, mttInstance);
	
				visitor.registerListeners();
				
				action.accept(visitor);
				
				visitor.updateStats();
				visitor.clearListeners();
			} else {
				log.error("MTT Support only works for MTT Support State objects! Found: "+state);
			}
		} finally {
			post();
		}
	}

	/**
	 * Gets the {@link JmxHandler}.
	 * @return the {@link JmxHandler}, never null
	 */
	public JmxHandler getJmxHandler() {
		return jmxHandler;
	}
	
	/**
	 * The table creator was previously injected in the support for each event. From 
     * FB 1.6 the table creator  should be accessed via the mtt instance object.
	 * 
	 * @deprecated This method should not be used, and will be removed in FB 1.7
	 */
	@Deprecated
	public final void setTableCreator(MttTableCreator creator) {
		this.creator.set(creator);
	}
	
    /**
     * The notifier was previously injected in the support for each event. From 
     * FB 1.6 the notifier should be accessed via the mtt instance object.
     * 
     * @deprecated Use the accessor in the MTT instance object instead, will be removed in FB 1.7
     */
	@Deprecated
    public final MttNotifier getMttNotifier() {
        return notifierLocal.get();
    }
    
    /**
     * The notifier was previously injected in the support for each event. From 
     * FB 1.6 the notifier should be accessed via the mtt instance object.
     * 
     * @deprecated This method should not be used, and will be removed in FB 1.7
     */
    @Deprecated
    public final void setMttNotifier(MttNotifier notifier) {
        notifierLocal.set(notifier);
    }

	
	
	/*------------------------------------------------
	 
		IMPLEMENTATION METHODS
		
		These methods must be implemented by the 
		extending implementation.
		
		The public methods will be accessed by firebase 
		logic (e.g. internal action processors).
	
	 ------------------------------------------------*/
	
	/** 
	 * Handle a round report from a table within the given tournament 
	 * 
	 * @param action, the round report action
	 * @param instance, the tournament instance
	 */
	public abstract void process(MttRoundReportAction action, MttInstance mttInstance);
	
	/**
	 * Handle a tables created action. Currently this method will be called once for each
	 * batch of requested tables (and the action will contain a set of multiple table id). 
	 * However, implementations of this method  should not make assumptions that this is the 
	 * case as the system reserves the right to split batches into sub-sets for performance reasons.
	 * 
	 * @param action the action
	 * @param instance the tournament instance
	 */
    public abstract void process(MttTablesCreatedAction action, MttInstance instance);

    /**
     * Handle a scheduled tournament object action. This is a convenience method for
     * the {@link #process(ScheduledMttAction, MttInstance)}.
     * @param action the action
     * @param instance the tournament instance
     */
    public abstract void process(MttObjectAction action, MttInstance instance);
    
    /**
     * <p><em>Override this to handle failed seating</em></p> 
     * 
     * <p>A tournament seating at a table has failed. This most likely cause is that the seat was already taken.
     * To handle this event in your implementation you should override this method. You do not need to 
     * invoke super.process(...) unless you want to keep the log entry.</p> 
     * 
     * @param action, contains contextual information about the failed seating.
     * @param instance, the tournament.
     */
    public void process(MttSeatingFailedAction action, MttInstance instance) {
        log.error("A seating failed for mtt["+action.getMttId()+"] player["+action.getPlayerId()+"] table["+action.getTableId()+"] seat["+action.getSeatId()+"]. You can override process(MttSeatingFailedAction action...) in you MttSUpport implementation to handle this.");
    }
    
    /**
     * Handle a tournament data action. This default implementation doesn't do anything, override it
     * if you wish to handle tournament data actions.
     * @param action the data action
     * @param instance the mtt instance
     */
    public void process(MttDataAction action, MttInstance instance) {}
    
    /**
     * @see MTTLogic#tournamentCreated(MttInstance)
     */
    public abstract void tournamentCreated(MttInstance mttInstance);
    
    /**
     * @see MTTLogic#tournamentDestroyed(MttInstance)
     */
    public abstract void tournamentDestroyed(MttInstance mttInstance);
    
	/** Return a player listener or null */
	public abstract PlayerListener getPlayerListener(MTTStateSupport state);
	
	/** Return a player interceptor or null.
	 * If you override this and return null a the default behavior is to
	 * accept all registrations, including already registered players. 
	 * See {@link PlayerRegistry#register(MttInstance, MttPlayer)} and
	 * {@link PlayerRegistry#unregister(MttInstance, int)}.
	 */
	public abstract PlayerInterceptor getPlayerInterceptor(MTTStateSupport state);
		
	/**
	 * @param instance Instance to set lobby attribute on, must not be null
	 * @param attribute Attribute to set, must not be null
	 */
	public void setLobbyAttribute(MttInstance instance, Attribute attribute) {
		Arguments.notNull(attribute, "attribute");
		Arguments.notNull(instance, "instance");
		LobbyAttributeAccessor acc = instance.getLobbyAccessor();
		acc.setAttribute(attribute.name, attribute.value);
	}
	
	/**
	 * @param instance Instance to remove lobby attribute from, must not be null
	 * @param name Name of the attribute to remove, must not be null
	 */
	public void removeLobbyAttribute(MttInstance instance, String name) {
		Arguments.notNull(name, "name");
		Arguments.notNull(instance, "instance");
		LobbyAttributeAccessor acc = instance.getLobbyAccessor();
		acc.removeAttribute(name);
	}
	
	/**
	 * Creates tables for the given tournament. When the creation of the tables has finished,
	 * {@link #process(MttTablesCreatedAction, MttInstance)} will be called asynchronously.
	 * 
	 * @param state the tournament's state, cannot be null
	 * @param numberOfTables the number of tables, must be > 0
	 * @param tableBaseName the base name to prepend to the tables'
	 */
	public void createTables(MTTStateSupport state, int numberOfTables, String tableBaseName) {
	    creator.get().createTables(state.getGameId(), state.getId(), numberOfTables, state.getSeats(), tableBaseName, null);
	}
	
	/**
	 * Creates tables for the given tournament. When the creation of the tables has finished,
	 * {@link #process(MttTablesCreatedAction, MttInstance)} will be called asynchronously.
	 * 
	 * <p>The attachment is optional, but if provided must be serializable.</p>
	 * 
	 * @param state the tournament's state, cannot be null
	 * @param numberOfTables the number of tables, must be > 0
	 * @param tableBaseName the base name to prepend to the tables'
	 * @param attachment Game/MTT specific serializable attachment that will be sent to 
	 *   {@link MttAwareActivator#mttTableCreated(Table, int, Object, LobbyAttributeAccessor)}, may be null
	 */
	public void createTables(MTTStateSupport state, int numberOfTables, String tableBaseName, Object attachment) {
	    creator.get().createTables(state.getGameId(), state.getId(), numberOfTables, state.getSeats(), tableBaseName, attachment);
	}
	
	/**
	 * Closes the given tables.
	 * 
	 * It is the responsibility of the caller of this method to check that the tables are empty
	 * before removing them.
	 * 
	 * @param state the tournament's state, cannot be null
	 * @param tables a {@link Collection} of tableIds
	 */
	public void closeTables(MTTStateSupport state, Collection<Integer> tables) {
		state.getSeating().removeTables(tables);
        creator.get().removeTables(state.getGameId(), state.getId(), tables, DELAYED_TABLE_REMOVAL_MS);
	}

	/**
	 * Closes a table.
	 * 
	 * It is the responsibility of the caller of this method to check that the table is empty
	 * before removing it.
	 * 
	 * @param state the tournament's state, cannot be null
	 * @param tableId the id of the table to close
	 */
	public void closeTable(MTTStateSupport state, Integer tableId) {
		state.getSeating().removeTable(tableId);
		state.getTables().remove(tableId);
        creator.get().removeTables(state.getGameId(), state.getId(), Collections.singleton(tableId), DELAYED_TABLE_REMOVAL_MS);
	}
	
	/**
	 * Seats a number of players at a number of tables. 
	 * 
	 * Calling this method will lead to a number of {@link SeatPlayersMttAction}s being sent to a number of {@link Table}s.
	 * 
	 * The seating will be done asynchronously, so the caller of this method cannot expect that the {@link Table}s 
	 * have received the messages after returning from this call.
	 * 
	 * @param state the tournament's state, cannot be null
	 * @param seating a {@link Collection} of {@link SeatingContainer}s representing where each player should sit.
	 * @throws IllegalArgumentException if any player in the seating is not registered in this tournament
	 */
	public void seatPlayers(MTTStateSupport state, Collection<SeatingContainer> seating) {
	    Map<Integer, MttPlayer> playerMap = createPlayerMap(state);
	    Map<Integer, SeatPlayersMttAction> tableToPlayers = new HashMap<Integer, SeatPlayersMttAction>();
	    
	    // log.info("Seating players: ", new Exception());
	    
	    for (SeatingContainer sc : seating) {
	        SeatPlayersMttAction act = tableToPlayers.get(sc.getTableId());
	        if (act == null) {
	            act = new SeatPlayersMttAction(state.getId(), sc.getTableId());
	            tableToPlayers.put(sc.getTableId(), act);
	        }
	        MttPlayer mttPlayer = playerMap.get(sc.getPlayerId());
	        
	        // Check that the player is registered for this tournament.
	        if (mttPlayer == null) {
	        	throw new IllegalArgumentException("Cannot seat players, since player with id: " + sc.getPlayerId() + " has not been registered for this tournament.");
	        } else {
	        	act.addPlayer(sc.getPlayerId(), mttPlayer.getScreenname(), sc.getSeatId(), sc.getPlayerData());
	        }
	        
            state.getSeating().addPlayerToTable(sc.getPlayerId(), act.getTableId(), sc.getSeatId());
	    }
	    
	    for (SeatPlayersMttAction act : tableToPlayers.values()) {
	        getMttNotifier().notifyTable(act.getTableId(), act);
	    }
	}
	
	/**
	 * Unseats a number of players from a table. The {@link Reason} provided will be used for all players.
	 * 
	 * Calling this method will result in an {@link UnseatPlayersMttAction} being sent to the table, asynchronously.
	 * See {@link #seatPlayers(MTTStateSupport, Collection)} for more information.
	 * 
	 * @param state the state of the tournament, cannot be null
	 * @param tableId
	 * @param players
	 * @param reason The reason why these players are unseated. The same reason will be used for all players. 
	 */
	public void unseatPlayers(MTTStateSupport state, int tableId, Collection<Integer> players, UnseatPlayersMttAction.Reason reason) {
        UnseatPlayersMttAction action = new UnseatPlayersMttAction(state.getId(), tableId, reason);
        TournamentSeating parentSeating = state.getSeating();
        TableSeating seating = state.getSeating().getTableSeating(tableId);
        boolean doSend = false;
        for (int playerId : players) {
        	/*
        	 * Trac issue #352; Only act if the table contains 
        	 * the player
        	 */
        	if(seating.getPlayers().contains(playerId)) {
	        	parentSeating.removePlayer(playerId);
	        	action.addPlayer(playerId);
	        	doSend = true;
        	}
        }
        if(doSend) {
        	// There was at least one player removed
        	getMttNotifier().notifyTable(tableId, action);
        }
    }
       
	/**
	 * Moves a player from one table to another. The move is performed by first unseating the player from 
	 * the old table and then seating the player at the new table.
	 * 
	 * See {@link #seatPlayers(MTTStateSupport, Collection)} and {@link #unseatPlayers(MTTStateSupport, int, Collection, Reason)}.
	 * 
	 * @param state the tournament's state, cannot be null
	 * @param playerId
	 * @param toTableId
	 * @param toSeatId the seatId where the player should sit at the new table, or -1 for first available seat
	 * @param reason the {@link Reason} for this move
	 * @param playerData optional player data, can be null
	 */
    public void movePlayer(MTTStateSupport state, int playerId, int toTableId, int toSeatId, UnseatPlayersMttAction.Reason reason, Serializable playerData) {
        int fromTableId = state.getSeating().getTableByPlayer(playerId);
        log.debug("Move player["+playerId+"] From ["+fromTableId+"] To["+toTableId+"]");
        unseatPlayers(state, fromTableId, Collections.singleton(playerId), reason);
        seatPlayers(state, Collections.singleton(new SeatingContainer(playerId, toTableId, toSeatId, playerData)));
    }
    
    /**
     * Sends a {@link StartMttRoundAction} to the given tables.
     * 
     * @param state the tournament state, cannot be null
     * @param tables a {@link Collection} of tableIds of the tables to send the action to
     */
    public void sendRoundStartActionToTables(MTTStateSupport state, Collection<Integer> tables) {
        log.debug("sending mtt start action to tables: " + tables);
        for (Integer tableId : tables) {
            StartMttRoundAction action = new StartMttRoundAction(state.getId(), tableId);
            getMttNotifier().notifyTable(tableId, action);
        }
    }
    
    /**
     * Sends a {@link StartMttRoundAction} to the given table.
     *  
     * @param state the tournament's state, cannot be null
     * @param tableId the id of the table to send the start round action to
     */
    public void sendRoundStartActionToTable(MTTStateSupport state, Integer tableId) {
    	sendRoundStartActionToTables(state, Collections.singleton(tableId));
    }
    
    /**
     * Sends a {@link StopMttRoundAction} to the given tables.
     * 
     * @param state the tournament's state, cannot be null
     * @param tables a {@link Collection} of tableIds of the tables to send the action to
     */
    public void sendRoundStopActionToTables(MTTStateSupport state, Collection<Integer> tables) {
        log.debug("sending mtt stop action to tables: " + state.getTables());
        for (Integer tableId : tables) {
            StopMttRoundAction action = new StopMttRoundAction(state.getId(), tableId);
            getMttNotifier().notifyTable(tableId, action);
        }
    }

    /**
     * Gets the tableId of the table where the given player is sitting.
     * 
     * @param state the tournament's state, cannot be null
     * @param playerId the id of the player to find the tableId for
     * @return the tableId of the table where the given player is sitting, 
     * 		   or -1 if the player is not found at any table
     */
    public int getTableIdByPlayerId(MTTStateSupport state, int playerId) {
        Integer tableId = state.getSeating().getTableByPlayer(playerId);
        return tableId == null ? -1 : tableId;
    }

    /**
     * Gets a {@link Set} of playerIds for all remaining players in the tournament.
     * 
     * If no players have been seated, the returned {@link Set} will be empty.
     * 
     * The {@link Set} is defensively created (by {@link MTTStateSupport.getAllPlayers()}) so
     * modifying the {@link Set} has no effect on the seating.
     * 
     * @param state the tournament's state, cannot be null
     * @return a {@link Set} of playerIds for all remaining players in the tournament
     */
	public Set<Integer> getRemainingPlayers(MTTStateSupport state) {
		return state.getSeating().getAllPlayers();
	}    
    
	
	
	
	// --- PRIVATE METHODS --- //
	
    private Map<Integer, MttPlayer> createPlayerMap(MTTStateSupport state) {
        Map<Integer, MttPlayer> playerMap = new HashMap<Integer, MttPlayer>();
        for (MttPlayer player : state.getPlayerRegistry().getPlayers()) {
            playerMap.put(player.getPlayerId(), player);
        }
        return playerMap;
    }
    
	private void pre(MttInstance mttInstance) {
		notifierLocal.set(mttInstance.getMttNotifier());
		creator.set(mttInstance.getTableCreator());
	}

	private void post() {
		notifierLocal.set(null);
		creator.set(null);
	}
}