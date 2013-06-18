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
package com.cubeia.firebase.api.game.table;

/**
 * This object manages the game state for a single table. You get a 
 * reference to this object via the table interface passed to the 
 * game on action processing.
 * 
 * @author Lars J. Nilsson
 */
public interface TableGameState {

    /**
     * Gets the game state held by this table. This may be null if
     * the table has not set any state and no state was set when the 
     * table was created.
     * 
     * @return The game state object, or null
     */
	public Object getState();

	
    /**
     * Sets the game state for this table. The state object should
     * be serializable in order to support server clustering. 
     * 
     * @param gameState New game state, should be serializable, may be null
     */
	public void setState(Object gameState);
	
}
