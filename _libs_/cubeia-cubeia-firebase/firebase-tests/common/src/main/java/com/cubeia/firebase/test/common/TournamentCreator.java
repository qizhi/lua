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
package com.cubeia.firebase.test.common;

/**
 * This simple interface is used to create tournaments outside
 * the control of the common packages. Most likely this will
 * wrap a JMX-connection to a remote tournament activator.
 * 
 * @author Lars J. Nilsson
 */
public interface TournamentCreator {

	/**
	 * Create a new tournament. This will probably require a remote
	 * call to the tournament activator. 
	 * 
	 * @return The Id of the new tournament
	 */
	public int create();
	
}
