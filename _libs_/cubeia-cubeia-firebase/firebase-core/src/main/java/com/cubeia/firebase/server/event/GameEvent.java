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

import com.cubeia.firebase.api.action.GameAction;

public class GameEvent extends StandardEvent<GameAction> {
	
	public GameEvent() { }
	
	public GameEvent(int playerid, int tableid) { 
		setPlayerId(playerid);
		setTableId(tableid);
	}
	
	public GameEvent(GameAction action) { 
		setTableId(action.getTableId());
	}

    public int getPlayerId() {
        return senderId;
    }
    
    @Override
    public String toString() {
        return "GameEvent: PID[" + getPlayerId() + "] TID[" + getTableId() +"]";
    }
    
    public void setPlayerId(int playerid) {
        super.setSenderId(playerid);
    }

    public int getTableId() {
    	return getFirstTargetId();
    }

    public void setTableId(int tableid) {
    	targetIds = new int[] { tableid };
    }
}

