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
package com.cubeia.firebase.api.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

/**
 * <p>Action to unseat a set of players from a table.</p>
 * 
 * <p>All players share a reason to be unseated, i.e. you cannot unseat one player
 * for being out and one for a table merge in the same unseat request.</p>
 * 
 * @author Fredrik
 */
public class UnseatPlayersMttAction extends AbstractGameMttAction {
	
	/** Version id */
    private static final long serialVersionUID = 1L;

    /**
     * Reason for the pickup of a player.
     *
     * @author Fredrik
     */
    public static enum Reason {
    	
    	/** Not applicable */
    	NOT_SPECIFIED,
    	
    	/** Player is out from tournament */
    	OUT,
    	
    	/** Table is merging */
    	MERGE,
    	
    	/** Tables are being balanced */
    	BALANCING
    }
    
    private List<PlayerContainer> players;
    
    /**
     * The reason why we are picking up the contained players.
     */
	private final Reason reason;
    
    /**
     * <p>Create an action to unseat a set of players from a table.</p>
     * 
     * <p>All players share a reason to be unseated, i.e. you cannot unseat one player
     * for being out and one for a table merge in the same unseat request.</p>
     * 
     * 
     * @param mttId, id of the mtt instance
     * @param tableId, id of the target table
     * @param reason, reason for the pickup/unseat
     */
    public UnseatPlayersMttAction(int mttId, int tableId, Reason reason) {
        super(mttId, tableId);
		this.reason = reason;
        players = new ArrayList<PlayerContainer>();
    }
    
    public void addPlayer(int playerId) {
        players.add(new PlayerContainer(playerId));
    }
    
    public List<PlayerContainer> getPlayers() {
        return Collections.unmodifiableList(players);
    }
    
    public Reason getReason() {
    	return reason;
    }

    public void visit(GameActionVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String toString() {
    	return "UnseatPlayersMttAction mttid["+getMttId()+"] tid["+getTableId()+"] reason["+reason+"] players["+players+"]";
    }
    
    /**
     * <p>Inner class that holds information about the players contained in the
     * request.</p>
     *
     * @author Fredrik
     */
    public class PlayerContainer implements Serializable {
		
    	private static final long serialVersionUID = 1L;

		private int playerId;
        
		/**
		 * Default constructor, might be needed for JBoss deserialization.
		 * 
		 */
        @SuppressWarnings("unused")
		private PlayerContainer() {
        	
        }
        
        public PlayerContainer(int playerId) {
            super();
            this.playerId = playerId;
        }
        
        public int getPlayerId() {
            return playerId;
        }
        
        @Override
        public String toString() {
        	return "pid["+playerId+"]";
        }
    }
}
