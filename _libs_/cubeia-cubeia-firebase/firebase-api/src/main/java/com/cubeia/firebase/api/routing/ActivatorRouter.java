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
package com.cubeia.firebase.api.routing;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.mtt.MttAction;

/**
 * This is the contract for an object which knows how to dispatch to the rest of
 * the system, or connected clients. In other words, it is the connection with the 
 * message bus in the Firebase cluster. 
 * 
 * @author Larsan
 */
public interface ActivatorRouter {

	/**
	 * Sends a game data action to a particular table. The
	 * data will be wrapped and dispatched across the message bus
	 * asynchronously. The action must have a valid table id set for
	 * the event to be routed correctly.
	 * 
	 * @param tableId Id of the table to send action to
	 * @param action Action to send, must not be null
	 */
	public void dispatchToGame(int tableId, GameAction action);
	
	
	/**
	 * Sends a game data action to a particular tournament. The
	 * data will be wrapped and dispatched across the message bus
	 * asynchronously. The action must have a valid MTT id set for
	 * the event to be routed correctly.
	 * 
	 * @param mttInstanceId Id of the tournament to send action to
	 * @param action Action to send, must not be null
	 */
	public void dispatchToTournament(int mttInstanceId, MttAction action);
	
}
