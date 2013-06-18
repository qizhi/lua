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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;
import com.cubeia.firebase.io.protocol.Param;


public class PlayerInfoAction extends AbstractPlayerAction {

	private static final long serialVersionUID = -2596167981468173111L;
	
    /** Screenname */
    String nick;

	private List<Param> details;
    
	public PlayerInfoAction(int playerId, int tableId) {
		super(playerId, tableId);
		details = new ArrayList<Param>(5);
	}

	public String toString() {
		return "PlayerInfoAction - pid["+getPlayerId()+"] nick["+nick+"]";
	}
	
	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}

	public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    /**
     * Gets the details for this player.
     * @return a {@link Map} of details, never null.
     */
	public List<Param> getDetails() {
		return details;
	}

	public void setDetails(List<Param> details) {
		this.details = details; 
	}

}
