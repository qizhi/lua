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
package com.cubeia.firebase.server.event;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cubeia.firebase.api.action.GameAction;

/**
 * A Game Event that has a list of player recipients. This object is not thread safe. If
 * the "globalEvent" flag is set to true, the player list will be ignored and the event 
 * broadcasted to all players.
 * 
 * @author fredrik.johansson
 */
public class MulticastClientEvent extends ClientEvent<GameAction> {

	private boolean globalEvent;
	
	public MulticastClientEvent() { }
	
    /**
     * @param globalEvent True if this event should be sent to all players, regardless of list
     */
	public MulticastClientEvent(boolean globalEvent) {
		setIsGlobalEvent(globalEvent);
	}
    
    @Override
    public String toString() {
    	return "MultiPlayerClientEvent - Players: "+ getPlayers().toString() + ": " +  super.toString();
    }

	public void setTableId(int tableId) {
		super.setSenderId(tableId);
	}
	
    /**
     * @param globalEvent True if this event should be sent to all players, regardless of list
     */
    public void setIsGlobalEvent(boolean globalEvent) {
		this.globalEvent = globalEvent;
	}
    
    /**
     * @return True if this is a global event, in which case the player list will be ignored
     */
    public boolean isGlobalEvent() {
		return globalEvent;
	}

    /**
     * @return Returns a copy of the player list, never null.
     */
    @SuppressWarnings("unchecked")
	public List<Integer> getPlayers() {
    	if(targetIds == null) {
    		return Collections.EMPTY_LIST;
    	} else {
    		List<Integer> list = new ArrayList<Integer>(targetIds.length);
    		for (int id : targetIds) {
    			list.add(id);
    		}
    		return list;
    	}
    }
    
    /**
     * This method sets the given player ids as targets if they are
     * specified. If however, the id array is null or has length zero,
     * the event will be marked as {@link #setIsGlobalEvent(boolean) global} 
     * automatically.
     * 
     * @param playerIds Player ids, zero length or null for global
     */
    public void checkSetPlayersGlobal(int[] playerIds) {
		if(playerIds != null && playerIds.length > 0) {
			setIsGlobalEvent(false);
			for (int id : playerIds) {
				addPlayer(id);
			}
		} else {
			setIsGlobalEvent(true);
		}
	}
    
    public void addPlayer(int id) {
        super.addTarget(id);
    }
	
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
    	super.writeExternal(out);
    	out.writeBoolean(globalEvent);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	globalEvent = in.readBoolean();
    }
}
