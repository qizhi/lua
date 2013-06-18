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
import com.cubeia.firebase.api.game.player.PlayerStatus;

/**
 * Cleanup (i.e. remove) this player from the table if still not in 
 * CONNECTED mode.
 * 
 * <p>The action will mainly serve as a trigger, the actual logic will take place
 * in the action handler.
 * 
 * <p>This is a transient action. If the table the action is targeting does not
 * exist when the action runs (ie. it has been removed in the interval between
 * scheduling and running), the action will be silently dropped.
 */
public class CleanupPlayerAction extends AbstractPlayerAction implements TransientGameAction {

    private static final long serialVersionUID = 1L;
    
    private PlayerStatus checkStatus;

    public CleanupPlayerAction(int playerId, int tableId, PlayerStatus status) {
        super(playerId, tableId);
		checkStatus = status;
    }

    
    public String toString() {
    	return "CleanupPlayer pid: "+getPlayerId()+" table: "+getTableId()+" status: "+checkStatus;
    }
    
    public void visit(GameActionVisitor visitor) {
        visitor.visit(this);
    }

	public PlayerStatus getCheckStatus() {
		return checkStatus;
	}
    
}
