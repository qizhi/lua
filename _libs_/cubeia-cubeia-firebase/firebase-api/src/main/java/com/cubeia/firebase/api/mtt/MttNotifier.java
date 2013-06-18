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
package com.cubeia.firebase.api.mtt;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.mtt.MttAction;

/**
 * Notifier used to send actions to tables and players.
 *
 * @author Fredrik
 */
public interface MttNotifier {

    /**
     * Notifies the table with given action.
     * 
     * @param tableId the table to notify
     * @param action the action to send
     */
    public void notifyTable(int tableId, GameAction action);

    /**
     * Notifies the player with the given action.
     * 
     * @param playerId the player id
     * @param action the action to send
     */
    public void notifyPlayer(int playerId, MttAction action);
    
}
