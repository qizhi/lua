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

import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;


public class ActionRequest extends AbstractPlayerAction {

	/**	For serialization. */
	private static final long serialVersionUID = 1206971360907589599L;
	
	private int timedActionId;
	
	private int timeOutMillis;
	
	private List<PlayerAction> possibleActions = new LinkedList<PlayerAction>();
	
	/**
	 * @return Returns the possibleActions.
	 */
	public List<PlayerAction> getPossibleActions() {
		return possibleActions;
	}

	/**
	 * @param possibleActions The possibleActions to set.
	 */
	public void setPossibleActions(List<PlayerAction> possibleActions) {
		this.possibleActions = possibleActions;
	}

	/**
	 * @return Returns the timedActionId.
	 */
	public int getTimedActionId() {
		return timedActionId;
	}

	/**
	 * @param timedActionId The timedActionId to set.
	 */
	public void setTimedActionId(int timedActionId) {
		this.timedActionId = timedActionId;
	}

	/**
	 * @return Returns the timeOutMillis.
	 */
	public int getTimeOutMillis() {
		return timeOutMillis;
	}

	/**
	 * @param timeOutMillis The timeOutMillis to set.
	 */
	public void setTimeOutMillis(int timeOutMillis) {
		this.timeOutMillis = timeOutMillis;
	}

	public ActionRequest(int playerId, int tableId) {
		super(playerId, tableId);
	}

	public String toString() {
		String info = "Action Request - Possible: \n";
		for (PlayerAction action : possibleActions) {
			info += action+"\n";
		}
		return info;
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}

	public void addPossibleAction(PlayerAction action) {
		possibleActions.add(action);
	}
}
