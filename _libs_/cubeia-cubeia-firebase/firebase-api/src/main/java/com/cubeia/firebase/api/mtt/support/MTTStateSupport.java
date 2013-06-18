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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.mtt.seating.TableSeating;
import com.cubeia.firebase.api.mtt.seating.TournamentSeating;
import com.cubeia.firebase.api.mtt.support.registry.PlayerRegistry;
import com.cubeia.firebase.api.mtt.support.registry.PlayerRegistryImpl;

/**
 * Convenience implementation of the {@link MTTState} interface.
 * 
 */
public class MTTStateSupport implements MTTState {

	/** Version id */
	private static final long serialVersionUID = 1L;

	private static transient Logger log = Logger.getLogger(MTTStateSupport.class);

	/** The identifier of the logic for this tournament. */
	private final int mttLogicId;
	
	/** The game that is played in this tournament. */
	private int gameId = -1;
	
	/** The minimum players need for this tournament to start. */
	private int minPlayers = Integer.MAX_VALUE;
	
	/** The id of this tournament. */
	private final int id;
	
	/** The name of this tournament. */
	private String name;
	
	/** The game specific state. */
	private Serializable state;
	
	/** The maximum number of players allowed in this tournament. */
	private int capacity = -1;

	/**
	 * Holds all tables used in this tournament. TODO: Use a table bean as value
	 * A table bean can hold which players are at which seat and so on, which will prove
	 * useful in table balancing situations.
	 */
	private Set<Integer> tables = new HashSet<Integer>();

	/** The seating of this tournament. */
	private TournamentSeating seating = new TournamentSeating();

	/**
	 * Number of seats to create at the table.
	 */
	private int seats = -1;

	/** Used to keep track of which players are registered for this tournament. */
	private PlayerRegistry registry;

	/** The {@link LobbyPath} of this tournament. */
	private LobbyPath path;

	/**
	 * Constructor.
	 * 
	 * @param mttLogicId
	 * @param id
	 */
	public MTTStateSupport(int mttLogicId, int id) {
		this(mttLogicId, id, "n/a");
	}

	/**
	 * Constructor.
	 * 
	 * @param mttLogicId
	 * @param id
	 * @param name
	 */
	public MTTStateSupport(int mttLogicId, int id, String name) {
		this.mttLogicId = mttLogicId;
		this.id = id;
		this.name = name;
		registry = new PlayerRegistryImpl(id);
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.common.Identifiable#getId()
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the {@link PlayerRegistry}.
	 * 
	 * @return the {@link PlayerRegistry}
	 */
	public PlayerRegistry getPlayerRegistry() {
		return registry;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "MttStateSupport - logicid[" + mttLogicId + "] gid[" + gameId + "] id[" + id + "] name[" + name + "] seats[" + seats + "] registered["+registry.size()+"]";
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#getMttLogicId()
	 */
	public int getMttLogicId() {
		return mttLogicId;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#getGameId()
	 */
	public int getGameId() {
		return gameId;
	}

	/**
	 * Gets the number of seats per table.
	 * 
	 * @return the number of seats per table
	 */
	public int getSeats() {
		return seats;
	}

	/**
	 * Sets the number of seats per table.
	 * 
	 * @param seats
	 */
	public void setSeats(int seats) {
		this.seats = seats;
	}

	/**
	 * Sets the gameId of the game to be played in this tournament.
	 * 
	 * @param gameId
	 */
	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	/**
	 * Gets the minimum players required for this tournament to start.
	 * 
	 * @return the minimum players required for this tournament to start
	 */
	public int getMinPlayers() {
		return minPlayers;
	}

	/**
	 * Sets the minimum players required for this tournament to start.
	 * 
	 * @param minPlayers the minimum players required for this tournament to start
	 */	
	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	/**
	 * Gets a Set of ids for all tables in the tournament.
	 * 
	 * Note 1: If tables are added or removed, the set will be updated asynchronously, so there
	 * is no guarantee that newly created or removed tables are present or non present in this Set.
	 * 
	 * Note 2: Game developers should NOT modify the returned Set. This will lead to unpredictable behavior. 
	 * Use {@link MTTSupport.#createTables(MTTStateSupport, int, String)} and {@link MTTSupport.#closeTable(MTTStateSupport, Integer)}.
	 * 
	 * @return a Set of ids for all tables in the tournament.
	 */
	public Set<Integer> getTables() {
		// TODO: Return an unmodifiable Set.
		return tables;
	}

	/**
	 * Called by Firebase when tables have been created.
	 */
	public void tablesCreated() {
		// TODO: Is this method needed?
		log.debug("Tables Created : " + tables);
	}

	/**
	 * Gets the seating of this tournament.
	 * 
	 * @return the {@link TournamentSeating}
	 */
	protected TournamentSeating getSeating() {
		return seating;
	}

	/**
	 * Sets the seating of this tournament.
	 * 
	 * @param seating
	 */
	protected void setSeating(TournamentSeating seating) {
		this.seating = seating;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#getState()
	 */
	public Object getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#setState(java.lang.Object)
	 */
	public void setState(Object state) {
		if (!(state instanceof Serializable)) {
			throw new IllegalStateException("state objects must implement Serializable");
		}
		this.state = (Serializable) state;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#getCapacity()
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Sets the capacity of this tournament. That is the maximum number of allowed players.
	 * 
	 * @param capacity
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Gets the number of remaining players in this tournament.
	 * 
	 */
	public int getRemainingPlayerCount() {
		return seating.getAllPlayers().size();
	}

	/**
	 * Creates a collection of playerIds sitting at the table requested.
	 * 
	 * @param tableId
	 * @return
	 */
	public Collection<Integer> getPlayersAtTable(int tableId) {
		TableSeating tableSeating = seating.getTableSeating(tableId);
		if (tableSeating == null) {
			log.warn("Tried to get players at tableId " + tableId
					+ " which does not exist in seating map. Available tables are " + seating.getAllTables());
			return new LinkedList<Integer>();
		}
		return new ArrayList<Integer>(tableSeating.getPlayers());
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#getRegisteredPlayersCount()
	 */
	public int getRegisteredPlayersCount() {
		// FIXME: Registered player Count should not return registry size(?)
		return getPlayerRegistry().size();
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#getLobbyPath()
	 */
	public LobbyPath getLobbyPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.api.mtt.MTTState#setLobbyPath(com.cubeia.firebase.api.lobby.LobbyPath)
	 */
	public void setLobbyPath(LobbyPath path) {
		this.path = path;
	}
}
