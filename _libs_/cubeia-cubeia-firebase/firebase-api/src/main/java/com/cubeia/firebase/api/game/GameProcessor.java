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
package com.cubeia.firebase.api.game;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.table.Table;

/**
 * The GameProcessor handles GameDataActions.
 * Implement this to provide game specific logic.
 * 
 * @author Fredrik
 *
 */
public interface GameProcessor {
	
	/**
	 * Handle a GameDataAction.
	 * 
	 * @param action
	 * @param table
	 */
	public void handle(GameDataAction action, Table table);

	/**
	 * Handle an internal GameObjectAction.
	 * 
	 * @param action
	 * @param table
	 */
	public void handle(GameObjectAction action, Table table);

}
