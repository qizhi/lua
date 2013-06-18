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
import com.cubeia.firebase.api.util.UnmodifiableSet;

/**
 * This is the interface to the collection of players currently
 * associated with the table (seated). It contains references to
 * player objects and a map over the current seating.
 * 
 * <p>A player should only ever exist once per set.
 *
 * @author lars.j.nilsson
 */
public interface TablePlayerSet {

    /**
     * THis method is equivalent to the collections
     * "size" method, and returns the number of player at
     * the table.
     * 
     * @return The number of players at the table
     */
    public int getPlayerCount();


    /**
     * This method removes a player at the table. It fails
     * silently if the player is not found in the set.
     * 
     * @param playerId PLayer to remove
     */
    public void removePlayer(int playerId);	


    /**
     * Add a player at a specific seat. If another player is 
     * referenced at the seat it will be removed, but remain in
     * the set, which may not be intended. 
     * 
     * @param player Player to add, must not be null
     * @param seat Seat id, indexed from zero
     */
    public void addPlayer(GenericPlayer player, int seat);


    /**
     * Get all players in an unmodifiable set. 
     * 
     * @return A set of all players, never null
     */
    public UnmodifiableSet<GenericPlayer> getPlayers();


    /**
     * Get a player object reference by id.
     * 
     * @param playerId Player id
     * @return A player object, or null if not found
     */
    public GenericPlayer getPlayer(int playerId);


    /**
     * Get a map over the table seating. 
     * 
     * @return The table seating map, never null
     */
    public TableSeatingMap getSeatingMap();

    /*
     * <p>This is a utility method for manually seating a player at 
     * a table</p>
     * 
     * This method will:
     * <ul>
     *  <li>Notify all players at the table of the seating (NotifyJoinPacket)</li>
     *  <li>Add a player-table association to the client registry</li>
     *  <li>Add the player to the playerset</li>
     * </ul>
     * 
     * Note the following:
     * <ul>
     *  <li>The table interceptor will not be called</li>
     *  <li>The table listener will not be called</li>
     *  <li>There will not be a join response packet sent to the seated player/client</li>
     * </ul>
     * 
     * <p>The same seating rules as for a join request will be applied. I.e. we will check if the
     * player is seated and if the seat is available. See DefaultSeatingRules implementation for 
     * more details.</p>
     * 
     * <p>No notification is sent out to the player that is seated. If you wish to notify the seated
     * player then you can either create a JoinResponseAction or a custom action and send to the client.</p> 
     * 
     * <p>Note that you should set the Status of the supplied GenericPlayer to whatever correlates best
     * with your requirements. If you set the status to anything but CON
     * 
     * @param player, not null
     * @param seat
     */
    // public void seatPlayer(GenericPlayer player, int seat) ;

    /**
     * <p>This is a utility method for manually unseating a player at 
     * a table</p>
     * 
     * This method will:
     * <ul>
     *  <li>Notify all players at the table of the removal (NotifyLeavePacket)</li>
     *  <li>Remove the player-table association from the client registry</li>
     *  <li>Remove the player from the playerset</li>
     * </ul>
     * 
     * Note the following:
     * <ul>
     *  <li>The table interceptor will not be called</li>
     *  <li>The table listener will not be called</li>
     *  <li>There will not be a leave response packet sent to the removed player/client</li>
     * </ul>
     * 
     * <p>No notification is sent out to the player that is seated. If you wish to notify the seated
     * player then you must send a custom message using send to client in the table notifier.</p> 
     * 
     * @param playerId
     */
    public void unseatPlayer(int playerId);

}
