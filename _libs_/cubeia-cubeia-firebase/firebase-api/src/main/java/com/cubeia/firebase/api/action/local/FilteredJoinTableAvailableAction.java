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

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.visitor.LocalActionVisitor;

/**
 * Action for waiting lists and join by filters.
 * 
 * @author Fredrik
 *
 */
public class FilteredJoinTableAvailableAction implements LocalAction {

	/** Player */
	private int playerId = -1;
	
	private int tableId = -1;
	
	private int seat = -1;
	
	/** Lobby tree address, e.g. a/b */
	private String address = "";

	/** ID of the game */
	private int gameId;
	
	/** Server side id. */
	private long requestId = -1;
	private int sequenceId = -1;
	
	public FilteredJoinTableAvailableAction(int playerId, int tableId) {
		this.playerId = playerId;
		this.tableId = tableId;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPlayerId() {
		return playerId;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getGameId() {
		return gameId;
	}

	public void visit(LocalActionVisitor visitor) {
		visitor.handle(this);
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public int getSequenceId() {
		return sequenceId;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public boolean matches(GameAction action) {
		return false;
	}

	
	public int getTableId() {
		return tableId;
	}

	public void setTableId(int tableId) {
		this.tableId = tableId;
	}

	
}
