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
package com.cubeia.firebase.api.game.rule;

import com.cubeia.firebase.api.action.JoinRequestAction;
import com.cubeia.firebase.api.action.LeaveAction;
import com.cubeia.firebase.api.action.RemovePlayerAction;
import com.cubeia.firebase.api.action.ReserveSeatRequestAction;
import com.cubeia.firebase.api.action.WatchAction;
import com.cubeia.firebase.api.game.table.Table;

/**
 * Interface for defining seating rules.
 *
 */
public interface SeatingRules {

    /**
     * Called to check if a player is allowed to join (sit down at) a table.
     * 
     * @param action
     * @param table
     * @return <code>true</code> if the action is allowed, <code>false</code> otherwise.
     */
	public boolean actionAllowed(JoinRequestAction action, Table table);
	
    /**
     * Called to check if a player is allowed to leave (his seat a) a table.
     * 
     * @param action
     * @param table
     * @return <code>true</code> if the action is allowed, <code>false</code> otherwise.
     */
	public boolean actionAllowed(LeaveAction action, Table table);
    
    /**
     * Called to check if a player is allowed to watch a table.
     * 
     * @param action
     * @param table
     * @return <code>true</code> if the action is allowed, <code>false</code> otherwise.
     */
    public boolean actionAllowed(WatchAction action, Table table);

    /**
     * Called to check if a player is allowed to reserve a seat (sit down at) a table.
     * 
     * @param action
     * @param table
     * @return <code>true</code> if the action is allowed, <code>false</code> otherwise.
     */
	public boolean actionAllowed(ReserveSeatRequestAction action, Table table);

	public boolean actionAllowed(RemovePlayerAction action, Table table);
}
