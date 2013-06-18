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

import java.util.Set;

import com.cubeia.firebase.api.util.UnmodifiableSet;

/**
 * A set containing all players "watching" the current
 * table.
 *
 * @author lars.j.nilsson
 */
public interface TableWatcherSet {

    /**
     * Gets a set of watching players at this table.
     * 
     * @return a {@link Set} of playerIds for the watching players
     */
	public UnmodifiableSet<Integer> getWatchers();
	
	/**
	 * @return The number of watchers at the table
	 */
	public int getCountWatchers();
	
    /**
     * Adds a watcher to this table.
     * 
     * @param playerId
     */
	public void addWatcher(int playerId);
	
	
    /**
     * Removes a watcher from this table.
     * 
     * @param playerId
     */
	public void removeWatcher(int playerId);
	
	/**
	 * 
	 * @param playerId
	 * @return <code>true</code> if the player is in the set of watchers, <code>false</code> otherwise
	 */
	public boolean isWatching(int playerId);
	
}
