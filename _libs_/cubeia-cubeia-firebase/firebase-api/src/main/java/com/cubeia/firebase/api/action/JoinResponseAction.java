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


public class JoinResponseAction extends AbstractPlayerAction {

	private static final long serialVersionUID = 4267305416199490968L;
	
	private int seatId; 
	
	private int status = 0;
	
	private int responseCode = -1;
	
	
	public JoinResponseAction(int playerId, int tableId, int seatId, int status) {
		super(playerId, tableId);
		this.seatId = seatId;
		this.status = status;
	}
	
	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}	

	public int getSeatId() {
		return seatId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    public String toString() {
		return "JoinResponse: playerId: " + getPlayerId() + " table: "+getTableId()+" seatId: " + seatId+" status: "+status+ " code: "+responseCode;
	}


	public void setSeatId(int seatId) {
		this.seatId = seatId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	
}
