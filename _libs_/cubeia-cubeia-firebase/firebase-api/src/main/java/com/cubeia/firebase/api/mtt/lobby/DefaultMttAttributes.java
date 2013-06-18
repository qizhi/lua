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
package com.cubeia.firebase.api.mtt.lobby;

/**
 * @author lars.j.nilsson
 */
public enum DefaultMttAttributes {
	
	/**
	 * Mtt instance id.
	 */
	_ID,
	
	/**
	 * Mtt tournament id.
	 */
	TOURNAMENT_ID,
	
	/**
	 * Mtt name.
	 */
	NAME,
	
	/**
	 * Mtt capacity, ie. maximum number of participating players.
	 */
	CAPACITY,
	
	/**
	 * Registered number of players
	 */
	REGISTERED,
	
	/**
	 * Remaining(active) number of players
	 */
	ACTIVE_PLAYERS,
	
	/*
	 * Current status of the tournament
	 */
	// STATUS,
	
	/**
	 * Last modification timestamp.
	 */
	_LAST_MODIFIED,
	
    /**
     * Flag for creation done. This is a required attribute
     */
    _READY
    
	
}
