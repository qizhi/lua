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


/**
 * Deny action for a game specific action.
 * 
 * Subclasses a RequestStatusAction where the type is set to 
 * GAME_SPECIFIC.
 * 
 * @author Fredrik
 *
 */
public class CommandResponseAction extends RequestStatusAction {

	/** */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor 
	 * 
	 * @param playerId
	 * @param tableId
	 * @param accepted
	 */
	public CommandResponseAction(int playerId, int tableId, boolean accepted) {
		super(playerId, tableId, RequestStatusAction.ActionType.GAME_SPECIFIC, accepted);
	}
	
	/**
	 * 
	 * 
	 * @param playerId
	 * @param tableId
	 * @param request, will be ignored. GAME_SPECIFIC will be used regardless.
	 * @param accepted
	 */
	public CommandResponseAction(int playerId, int tableId, ActionType request, boolean accepted) {
		super(playerId, tableId, RequestStatusAction.ActionType.GAME_SPECIFIC, accepted);
	}
	
	
	
}
