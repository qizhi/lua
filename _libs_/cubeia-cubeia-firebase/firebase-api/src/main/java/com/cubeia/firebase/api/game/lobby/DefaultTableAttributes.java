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
package com.cubeia.firebase.api.game.lobby;

/**
 * A simple enumeration whose values are used as attribute names by 
 * the default table mapper. When mapping the enum toString method will be
 * used to generate the attribute names.
 * 
 * <p>These preset attributes are all considered internal and should be prefixed with _
 * 
 * <p>The {@link #_MTT_ID} attribute will only be set if the table in question
 * belongs to a tournament, in which case it will be set to the instance id of the
 * tournament.
 * 
 * @author lars.j.nilsson
 */
public enum DefaultTableAttributes {
	
	/**
	 * Table id.
	 */
	_ID,
	
	/**
	 * Table name.
	 */
	_NAME,
	
	/**
	 * Table capacity, ie. maximum number of seated players.
	 */
	_CAPACITY,
	
	/**
	 * Current number of seated players.
	 */
	_SEATED,
	
	
	/**
	 * Game id of the table.
	 */
	_GAMEID,
	
	
	/**
	 * Last modification timestamp.
	 */
	_LAST_MODIFIED,
	
	
	/**
	 * Number of clients watching the table.
	 */
	_WATCHERS,

	
	/**
	 * Optional MTT id to which the table belongs.
	 */
	_MTT_ID
}
