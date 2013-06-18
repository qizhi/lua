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
package com.cubeia.firebase.server.gateway.client;

import java.io.Serializable;

import com.cubeia.firebase.server.gateway.event.ClientGameActionHandler;

public class AbstractPlayerData implements IPlayerData, Serializable {

	private static final long serialVersionUID = 1L;

	/** Unique player id */
    private int id = -1;
    
    /** Screenname or nick */
    private String screenname = "n/a";

    private int operatorid = -1;
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getScreenname() {
		return screenname;
	}

	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}

	public ClientGameActionHandler getActionHandler() {
		return null;
	}

    public String toString() {
    	return screenname+"["+id+"]";
    }

	@Override
	public int getOperatorId() {
		return operatorid;
	}

	@Override
	public void setOperatorId(int operatorId) {
		this.operatorid = operatorId;
	}
}
