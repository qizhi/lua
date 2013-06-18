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
 * Action sent to players that are invited to a game.
 * 
 * @author Fredrik
 *
 */
public class NotifyInvitedAction extends AbstractPlayerAction {

	private static final long serialVersionUID = 1L;
	
	/** Seat at table. -1 = not specified (and no reservation) */
	private int seat = -1;
	private int inviter = -1;
	
	public NotifyInvitedAction(int playerId, int inviterId, int tableId, int seat) {
		super(playerId, tableId);
		this.inviter = inviterId;
		this.seat = seat;
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}
	
	public String toString() {
		return "Notify Invited Action pid["+getPlayerId()+"] tid["+getTableId()+"] seat["+seat+"]";
	}

	public int getSeat() {
		return seat;
	}

	public int getInviterId() {
		return inviter;
	}
}
