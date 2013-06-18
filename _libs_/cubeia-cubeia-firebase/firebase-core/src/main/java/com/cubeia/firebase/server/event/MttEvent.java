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

import com.cubeia.firebase.api.action.mtt.MttAction;

public class MttEvent extends Event<MttAction> {

	public MttEvent() { }
	
	public MttEvent(int playerid, int mttid) { 
		setPlayerId(playerid);
		setMttId(mttid);
	}
	
	public MttEvent(int playerid, int mttid, MttAction action) { 
		setPlayerId(playerid);
		setAction(action);
		setMttId(mttid);
	}

    public int getPlayerId() {
        return senderId;
    }
    
    @Override
    public String toString() {
        return "GameEvent: PID[" + getPlayerId() + "] MTTID[" + getMttId() +"]";
    }
    
    public void setPlayerId(int playerid) {
        super.setSenderId(playerid);
    }

    public int getMttId() {
    	return getFirstTargetId();
    }

    public void setMttId(int mttid) {
    	targetIds = new int[] { mttid };
    }
}
