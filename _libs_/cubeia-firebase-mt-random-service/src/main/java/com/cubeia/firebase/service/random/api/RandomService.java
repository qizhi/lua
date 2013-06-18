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

package com.cubeia.firebase.service.random.api;

import java.util.Random;

import com.cubeia.firebase.api.service.Contract;

/**
 * This is a random service which create Java Random implementations
 * of a Marsenne Twister algorithm. It contains a "system default" Random
 * which will be shared among all callers. It can also create new, completely
 * separated and separately seeded Random objects on demand. 
 * 
 * <p>The service supports two strengthening layers, "discarded draw", and "background
 * polling". Discarded draw will for every random number generated, discard 0-N numbers 
 * in the background. Background polling will use a separate thread to discard 0-N numbers
 * every Y milliseconds. Both these can be used to make it harder to attack the marsenne
 * sequence. The are enabled and configured in the {@link RandomServiceConfig cluster config}.
 * 
 * @author Lars J. Nilsson
 */
public interface RandomService extends Contract { 
	
	/**
	 * @return The shared system random, never null
	 */
	public Random getSystemDefaultRandom();
	
	/**
	 * @return A new, separately seeded, random, never null
	 */
	public Random createNewRandom();
	
}