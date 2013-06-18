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

/**
 * Send out an accept answer to the client.
 * If the requested was not accepted (accepted = false)
 * then it is explicitly denied by the server.
 * 
 * @author Fredrik
 *
 */
public class RequestStatusAction extends AbstractPlayerAction {

	private static final long serialVersionUID = 9192561303309808705L;
	
	public enum ActionType {
		JOIN,
		LEAVE,
		GAME_SPECIFIC,
		WATCH,
		UNWATCH
	}
	
	private ActionType request;
	
	/** If not accepted, denied */
	private boolean accepted;
	
	/** Response code can be used by client to generate a message to the client */
	private int responseCode = -1;
	
	/** Extra identifier */
	private int extra = -1;
	
	public RequestStatusAction(int playerId, int tableId, ActionType request, boolean accepted) {
		super(playerId, tableId);
		this.request = request;
		this.accepted = accepted;
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}
	
	
	public boolean isAccepted() {
		return accepted;
	}

	public ActionType getRequest() {
		return request;
	}
	
	
	
	public int getExtra() {
		return extra;
	}

	public void setExtra(int extra) {
		this.extra = extra;
	}

	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode the responseCode to set
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String toString() {
		return "Req Status Action pid["+getPlayerId()+"] tableid["+getTableId()+"] request["+request+"] accepted["+accepted+"] extra["+extra+"]";
	}
}
