/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.tournament;

/**
 * Configuration for the tournament support in Firebase. For the
 * moment it only details the tournament handler implementation.
 * 
 * @author larsan
 */
public interface Configuration {

	/**
	 * @return the tournament handler implementation, must not return null
	 */
	public Class<? extends TournamentHandler> getTournamentHandlerClass();
	
}
