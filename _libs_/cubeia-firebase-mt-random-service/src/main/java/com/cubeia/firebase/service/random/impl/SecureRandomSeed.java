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

import static com.cubeia.firebase.api.util.Arguments.notNull;

import java.security.SecureRandom;

import com.cubeia.firebase.service.random.api.RandomSeedService;

/**
 * This is a trivial random seed service on top of 
 * a Java secure random.
 * 
 * @author Lars J. Nilsson
 */
public class SecureRandomSeed implements RandomSeedService {

	private final SecureRandom secureRandom;

	/**
	 * This method created the object with a new Java secure 
	 * random a seed source.
	 */
	public SecureRandomSeed() {
		this(new SecureRandom());
	}
	
	/**
	 * @param secureRandom Secure random to use, must not be null
	 */
	public SecureRandomSeed(SecureRandom secureRandom) {
		notNull(secureRandom, "secure random");
		this.secureRandom = secureRandom;
	}

	@Override
	public void seed(int[] seeds) {
		for (int i = 0; i < seeds.length; i++) {
			seeds[i] = secureRandom.nextInt();
		}
	}
}
