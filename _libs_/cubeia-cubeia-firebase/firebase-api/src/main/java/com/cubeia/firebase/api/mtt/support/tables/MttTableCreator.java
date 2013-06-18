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
package com.cubeia.firebase.api.mtt.support.tables;

import java.util.Collection;

/**
 * <p>Interface for creating tournament tables.</p>
 * 
 * <p>The request for creating tables will be propagated to the
 * actor responsible for construction and removal of tables (normally the game coordinator).
 * </p>
 *
 * @author Fredrik
 */
public interface MttTableCreator {

	/**
	 * Request creation of tables.
	 * 
	 * <p>The attachment is optional, but if provided must be serializable.
	 * 
	 * @param tableCount
	 * @param seats
	 * @param baseName, will be used to construct table names
	 * @param attachment Game/MTT specific serializable attachment, may be null
	 */
	public void createTables(int gameId, int mttId, int tableCount, int seats, String baseName, Object attachment);
	
	/**
	 * Requests removal of the tables with the given id's.
	 * 
	 * @param gameId the game id
	 * @param mttId the tournament id
	 * @param tableIds the tables to remove
	 */
    public void removeTables(int gameId, int mttId, Collection<Integer> tableIds);

    /**
     * Requests removal of the tables with the given id's after the given delay in milliseconds.
     * 
     * @param gameId the game id
     * @param mttId the tournament id
     * @param tableIds the tables to remove
     * @param delay the delay in milliseconds
     */
    public void removeTables(int gameId, int mttId, Collection<Integer> unusedTables, long delayMs);
	
	
}
