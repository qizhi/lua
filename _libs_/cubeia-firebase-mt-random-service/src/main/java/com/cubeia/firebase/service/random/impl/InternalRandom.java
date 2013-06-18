/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
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

package com.cubeia.firebase.service.random.impl;

/**
 * Interface for internal random implementation that publishes
 * the {@link #next(int)} method in order to call it from separate
 * threads. An internal random should not be exposed outside the
 * service.
 * 
 * <p>All implementations must be thread-safe.
 * 
 * @author Lars J. Nilsson
 */
public interface InternalRandom {

	/**
	 * @param bits Bits of the next int to use, should not be negative
	 * @return A new integer from the marsenne sequence
	 */
	public int next(int bits);
	
}
