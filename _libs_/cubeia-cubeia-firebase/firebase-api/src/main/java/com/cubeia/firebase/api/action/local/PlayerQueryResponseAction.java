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

import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.visitor.LocalActionVisitor;

/**
 * Server reponse for a Player Query request
 *
 * @author Fredrik
 */
public class PlayerQueryResponseAction implements LocalAction {
	
	/** Player id */
	private final int playerId;
	
	private String screenname;
	
	/** Arbitrary player data */
	private byte[] data = new byte[0];
	
	/** Status is defaulted to OK */
	private Status status = Status.OK; 
	
	/** Constructor. */
	public PlayerQueryResponseAction(int playerId) {
		this.playerId = playerId;
	}
	
	public String toString() {
		return "PlayerQueryResponse: id["+playerId+"] nick["+screenname+"]"; 
	}
	
	public String getScreenname() {
		return screenname;
	}

	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}

	public int getPlayerId() {
		return playerId;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void visit(LocalActionVisitor visitor) {
		visitor.handle(this);
	}

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
	
	
}
