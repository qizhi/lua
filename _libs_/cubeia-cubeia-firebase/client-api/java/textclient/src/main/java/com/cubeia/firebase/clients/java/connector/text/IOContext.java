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
package com.cubeia.firebase.clients.java.connector.text;



/**
 * Connection / IO Context for client applications
 * that want to communicate with the server.
 * 
 * 
 */
public class IOContext {
    
    /** The current user */
    private Player player;
    
    /** The communication layer towards the server */
    private Connection connector;

    /**
     * @return Returns the connector.
     */
    public Connection getConnector() {
        return connector;
    }

    /**
     * @param connector The connector to set.
     */
    public void setConnector(Connection connector) {
        this.connector = connector;
    }

    /**
     * @return Returns the player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @param player The player to set.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }
 
    public int getPlayerId() {
        return player.getId();
    }
    
    public void setPlayerId(int id) {
        if (player != null) {
            player.setId(id);
        }
    }
}
