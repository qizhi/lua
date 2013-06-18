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
package com.cubeia.firebase.api.action.local;

import com.cubeia.firebase.api.action.visitor.LocalActionVisitor;

/**
 * Wraps a player query request from a client.
 * 
 * @author Fredrik
 *
 */
public class PlayerQueryRequestAction implements LocalAction {
	
	/** Requested player id to lookup */
	private int playerid = -1;
	/**
	 * Create a Login Request.
	 * 
	 * @param user
	 * @param password
	 */
	public PlayerQueryRequestAction(int playerid) {
		this.playerid = playerid;
		
	}
	
	public int getPlayerid() {
		return playerid;
	}

	public String toString() {
		return "PlayerQueryRequest: pid["+playerid+"]";
	}

	public void visit(LocalActionVisitor visitor) {
		visitor.handle(this);
	}
	
}
