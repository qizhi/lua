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
package com.cubeia.firebase.api.game;

import java.util.Collection;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.SystemMessageAction;


/**
 * 
 * Created on 2006-sep-08
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public interface GameNotifier {
    	
	/**
     * <p>Send the supplied action to the given player.
     * This sending method ignores current status of player at the table.</p>
     * 
     * <p>DO NOT use this method on player with status = TABLE_LOCAL.</p> 
     * 
     * @param playerid
     * @param action
     */
	public void sendToClient(int playerid, GameAction action);
	
	/**
     * <p>Send multiple actions to the given player.
     * This sending method ignores current status of player at the table.</p>
     * 
     * <p>DO NOT use this method on player with status = TABLE_LOCAL.</p> 
     * 
     * @param playerid
     * @param action
     */
	public void sendToClient(int playerId, Collection<? extends GameAction> actions);
	
	/**
	 * Broadcast a message to the system. This will effectively send the message
	 * to all currently logged in players. Please use this method with extreme care.
	 * 
	 * @param msg Message to send, must not be null
	 */
	public void broadcast(SystemMessageAction msg);
	
    /**
     * Notify the given player with the supplied action
     * 
     * @param playerid
     * @param action
     */
    public void notifyPlayer(int playerid, GameAction action);
    
    /**
     * <p>Notify the given player with the supplied actions</p>
     * 
     * @param playerid
     * @param action
     */
    public void notifyPlayer(int playerid, Collection<? extends GameAction> action);
    
    
    /**
     * Notify all players and watchers with the supplied action.
     * All players in this case will be all players
     * that will need the update. 
     * 
     * Eg. at a poker table all players are all clients
     * at the table as well as all observers.
     * 
     * @param action
     */
    public void notifyAllPlayers(GameAction action);
    
    /**
     * Notify all players  with the supplied action.
     * All players in this case will be all players
     * that will need the update. 
     * 
     * Eg. at a poker table all players are all clients
     * at the table as well as all observers.
     * 
     * Watchers will be included if the watchers flag
     * is set to true.
     * 
     * @param action
     * @param watchers, true is all watchers should also
     * receive the action.
     */
    public void notifyAllPlayers(GameAction action, boolean watchers);
    
    
    /**
     * <p>Notify all players with the supplied actions.
     * All players in this case will be all players
     * that will need the update.</p> 
     * 
     * <p>Eg. at a poker table all players are all clients
     * at the table as well as all observers.</p>
     * 
     * @param action
     */
    public void notifyAllPlayers(Collection<? extends GameAction> action);
    
    /**
     * <p>Notify all players with the supplied actions.
     * All players in this case will be all players
     * that will need the update.</p> 
     * 
     * <p>Eg. at a poker table all players are all clients
     * at the table as well as all observers.</p>
     * 
     * @param action
     * @param watchers, true is all watchers should also
     * receive the action.
     */
    public void notifyAllPlayers(Collection<? extends GameAction> action, boolean watchers);
    
    
    
    /**
     * <p>Notify all players and watchers except the given one
     * with the supplied action.
     * All players in this case will be all players
     * that will need the update.</p> 
     * 
     * <p>Eg. at a poker table all players are all clients
     * at the table as well as all observers.</p>
     * 
     * @param action
     */
    public void notifyAllPlayersExceptOne(GameAction action, int playerid);
    
    
    /**
     * <p>Notify all players except the given one
     * with the supplied action.
     * All players in this case will be all players
     * that will need the update.</p> 
     * 
     * <p>Eg. at a poker table all players are all clients
     * at the table as well as all observers.</p>
     * 
     * <p>Watchers will be included if the watchers flag
     * is set to true.</p>
     * 
     * @param action
     * @param watchers, true is all watchers should also
     * receive the action.
     */
    public void notifyAllPlayersExceptOne(GameAction action, int playerid, boolean watchers);
    
    /**
     * <p>Notify all players with the supplied actions.
     * All players in this case will be all players
     * that will need the update.</p> 
     * 
     * <p>Eg. at a poker table all players are all clients
     * at the table as well as all observers.</p>
     * 
     * @param action
     */
    public void notifyAllPlayersExceptOne(Collection<? extends GameAction> action, int playerid);
    
    /**
     * <p>Notify all players with the supplied actions.
     * All players in this case will be all players
     * that will need the update.</p> 
     * 
     * <p>Eg. at a poker table all players are all clients
     * at the table as well as all observers.</p>
     * 
     * @param action
     * @param watchers, true is all watchers should also
     * receive the action.
     */
    public void notifyAllPlayersExceptOne(Collection<? extends GameAction> action, int playerid, boolean watchers);
	
}

