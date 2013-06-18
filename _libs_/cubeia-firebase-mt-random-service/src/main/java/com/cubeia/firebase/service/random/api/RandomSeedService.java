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

import com.cubeia.firebase.api.service.Contract;

/**
 * This is a plugin service interface for seeding the marsenne
 * twister implementation. This can be implemented to seed from a
 * hardware random number generated or other sources of entropy. 
 * 
 * <p>In the absence of this service the random service will use
 * a standard Java secure random for the seeds.
 * 
 * @author Lars J. Nilsson
 */
public interface RandomSeedService extends Contract {

	/**
	 * Populate the given array with random noise. All integers
	 * in the array will be used. 
	 * 
	 * @param seeds Seed array to populate, must not be null
	 */
	public void seed(int[] seeds);
	
}
