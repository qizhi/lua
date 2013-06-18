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

import com.cubeia.firebase.api.game.player.Player;

/**
 * <p>Model of a seat at a Table.</p>
 * 
 * <p>The seat holds a reference to the player seated
 * if applicable.</p>
 *
 * @author Fredrik
 * @param <T>
 */
public interface Seat<T extends Player> {
	
	public boolean isVacant();
	
	public boolean isOccupied();
	
	public void seat(T player);

	public int getPlayerId();
	
	public T getPlayer();

	public int getId();

	public void clear();
}
