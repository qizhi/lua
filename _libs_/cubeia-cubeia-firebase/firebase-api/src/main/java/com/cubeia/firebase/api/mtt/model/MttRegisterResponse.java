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
package com.cubeia.firebase.api.mtt.model;


/**
 * <p>Enumeration of firebase contained responses for joining or leaving a tournament.</p>
 * 
 * <p>The enumeration contains generic allow and denied, but also some more commonly
 * used fine-grained responses such as low on funds.</p>
 *
 * @author Fredrik
 */
public enum MttRegisterResponse {
	
	/** Player accepted */
	ALLOWED,
	
	/**
	 * The registration failed.
	 */
	FAILED,
	
	/** Generic denied by server */
	DENIED,
	
	/** 
	 * Request denied. 
	 * Player has no funds for registering.
	 */
	DENIED_LOW_FUNDS,
	
	/**
	 *  Request denied. 
	 *  The tournament is full.
	 */
	DENIED_MTT_FULL,
	
	/** 
	 *  Request Denied. 
	 *  No privileges to register for the tournament.
	 */
	DENIED_NO_ACCESS,
	
	/**
	 * Request denied. 
	 * The player is already registered.
	 */
	DENIED_ALREADY_REGISTERED,
	
	/**
	 * Request denied.
	 * The tournament is running and cannot accept (un)registrations.
	 */
	DENIED_TOURNAMENT_RUNNING
}
