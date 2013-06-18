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

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

public class CreateTableResponseAction extends AbstractPlayerAction {

	private static final long serialVersionUID = 1L;

	private int seq = -1;
	
	private Status status = Status.OK;
	
	/** Game specific response code */
	private int responseCode = -1;

	private int seat;
	
	public CreateTableResponseAction(int playerId, int tableId, int seat, Status status, int responseCode) {
		super(playerId, tableId);
		this.status = status;
		this.responseCode = responseCode;
		this.seat = seat;
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}
	
	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public String toString() {
		return "Create Table Response Action pid["+getPlayerId()+"] tableid["+getTableId()+"] status["+status+"] code["+responseCode+"]";
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	
}
