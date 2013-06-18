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
package com.cubeia.firebase.api.util;


/**
 * Simple utility class for argument bounds checking.
 * 
 * @author lars.j.nilsson
 */
public final class Arguments {

	private Arguments() { }
	
	
	/**
	 * This method check if the supplied argument is null. If it is, an
	 * illegal argument exception is thrown.
	 * 
	 * @param arg Object to check for null, may be null
	 * @param name Name of the argument which is being checked, may be null
	 * @throws IllegalArgumentException If the argument is null
	 */
	public static void notNull(Object arg, String name) throws IllegalArgumentException {
		if(arg == null) throw new IllegalArgumentException("argument '" + name + "' must not be null");
	}

	
	/**
	 * This method check if the supplied argument is less than zero. If it is, an
	 * illegal argument exception is thrown.
	 * 
	 * @param arg Number to check
	 * @param name Name of the argument which is being checked, may be null
	 * @throws IllegalArgumentException If the argument is less than zero
	 */
	public static void positive(int arg, String name) throws IllegalArgumentException {
		if(arg <= 0) throw new IllegalArgumentException("argument '" + name + "' must be positive; found: " + arg);
	}
}
