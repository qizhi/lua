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
package com.cubeia.firebase.api.mtt;

import java.io.Serializable;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.mtt.support.MTTStateSupport;

/**
 * <p>Models an MTT (Multi Table Tournament).</p> 
 * 
 * <p>This interface is similar to Table in the way that it provides a higher
 * level abstraction than actually implemented by 3rd parties.</p> 
 * 
 * <p>The implementation of MTT should contain a single, unique and identifiable
 * tournament. The implementation may (must) be stateful and will be replicated
 * in a dedicated space for failover purposes. Firebase provides a default state
 * implementation that provides some common functionality. 
 * See {@link MTTStateSupport}
 * </p> 
 * @author Fredrik
 */
public interface MTTState extends Identifiable, Serializable {
	
	/**
	 * This ID should be the id of the implementing
	 * logic. That is, the id in the tournament.xml deployment descriptor.
	 *
	 */
	public int getMttLogicId();
	
	/**
	 * Gets the id of the game that will be played on 
	 * the table. Used when creating tables.
	 * 
	 * @return the id of the game that will be played on the table.
	 */
	public int getGameId();
	
	/**
	 * Sets the name of this mtt instance state.
	 * 
	 * @see MTTState#getName()
	 * @param name must not be null
	 */
	public void setName(String name);
	
	/**
	 * Returns the name of this mtt instance state. The name represents an instance of the tournament. 
	 * @return the name of the tournament instance state
	 */
	public String getName();
	
	/**
	 * Gets the maximum number of players allowed in the tournament.
	 *  
	 * @return capacity
	 */
	public int getCapacity();

	/**
	 * Gets number of players currently active in the tournament.
	 *  
	 * @return player count
	 */
	public int getRemainingPlayerCount();
	
	/**
	 * <p>Gets number of players that is/was registered. This will be
	 * used as a registered counter when in register state and as a
	 * comparison in the lobby when in running state.</p>
	 * 
	 * <p><b>Example:</b></br>
	 * <p>REGISTERING: Tourn-1   27/100   Open</br>
	 * <em>Tournament is open and 27 players have registered.
	 * There is still room for 73 more.</em></p>
	 * 
	 * <p>REGISTERING: Tourn-1   45/85   Running</br>
	 * <em>Tournament got 85 participants and is now running.
	 * There are currently 45 players left (40 players are out).</em></p>
	 * 
	 * 
	 * @return player count
	 */
	public int getRegisteredPlayersCount();
	
	/**
	 * Returns the optional user provided state holder. 
	 * @return the state holder, will be null if no state has been set
	 */
	public Object getState();
	
	/**
	 * Sets an optional state object. This object must be serializable.
	 * 
	 * @param state the state to set
	 */
	public void setState(Object state);
	
	/**
	 * Sets the {@link LobbyPath} for this tournament.
	 * 
	 * @param path the path to set, must not be null
	 */
	public void setLobbyPath(LobbyPath path);
	
	/**
	 * Gets the {@link LobbyPath} for this tournament.
	 * 
	 * @return the {@link LobbyPath} for this tournament.
	 */
	public LobbyPath getLobbyPath();
	
}
