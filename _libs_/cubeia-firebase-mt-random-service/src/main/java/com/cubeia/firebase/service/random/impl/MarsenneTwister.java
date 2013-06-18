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

import com.cubeia.firebase.service.random.api.RandomSeedService;

/**
 * This is an unsynchronized implementation of a Marsenne Twister
 * sequence. Seeding should ultimately be done with a {@link
 * RandomSeedService seed service} implementation.
 * 
 * @author Lars J. Nilsson
 */
public class MarsenneTwister {
	
	private final static int UPPER_MASK = 0x80000000;
	private final static int LOWER_MASK = 0x7fffffff;

	private final static int N = 624;
	private final static int M = 397;
	
	private final static int MAGIC[] = { 0x0, 0x9908b0df };
	
	private final static int FACTOR1 = 1812433253;
	private final static int FACTOR2 = 1664525;
	private final static int FACTOR3 = 1566083941;
	
	private final static int MASK1 = 0x9d2c5680;
	private final static int MASK2 = 0xefc60000;

	private final static int INIT_SEED = 19650218;

	private int[] buffer;
	private int index;

	/*public MarsenneTwister() {
		this(new int[] { 0, 0 });
	}*/

	/**
	 * @param seeder Seeder to use, must not be null
	 */
	public MarsenneTwister(RandomSeedService seeder) {
		notNull(seeder, "seeder");
		buffer = new int[N];
		int[] seed = new int[N];
		seeder.seed(seed);
		seed(INIT_SEED);
		seed(seed);
	}
	
	/**
	 * @param seed Seed to use, must not be null and not empty
	 */
	public MarsenneTwister(int[] seed) {
		notNull(seed, "seed");
		if(seed.length == 0) {
			throw new IllegalArgumentException("Zero length seed");
		}
		buffer = new int[N];
		seed(INIT_SEED);
		seed(seed);
	}
	
	/**
	 * This method mimics the "next" method required by the Java Random
	 * implementations. It will return an N bit integer from the sequence. 
	 * 
	 * @param bits Bits to return, should not be negative
	 * @return An integer of N bits from the sequence
	 */
	public int next(int bits) {
		if (index >= N) {
			int y = 0;
			int z = 0;
			for (; z < N-M; z++) {
				y = (buffer[z] & UPPER_MASK) | (buffer[z+1] & LOWER_MASK);
				buffer[z] = buffer[z+M] ^ (y >>> 1) ^ MAGIC[y & 0x1];
			}
			for (;z < N-1; z++) {
				y = (buffer[z] & UPPER_MASK) | (buffer[z+1] & LOWER_MASK);
				buffer[z] = buffer[z+(M-N)] ^ (y >>> 1) ^ MAGIC[y & 0x1];
			}
			y = (buffer[N-1] & UPPER_MASK) | (buffer[0] & LOWER_MASK);
			buffer[N-1] = buffer[M-1] ^ (y >>> 1) ^ MAGIC[y & 0x1];
			index = 0;
		}
		int next = buffer[index++];
		next ^= (next >>> 11);
		next ^= (next << 7) & MASK1;
		next ^= (next << 15) & MASK2;
		next ^= (next >>> 18);
		return (next >>> (32 - bits));
	}

	
	// --- PRIVATE METHODS --- //
	
	/*
	 * Seed the twister, the seed(int) method should be called with INIT_SEED
	 * first for the Twister to be configured correctly
	 */
	private void seed(int[] seed) {
		int i = 1;
		int j = 0;
		int k = (N > seed.length ? N : seed.length);
		while (k > 0) {
			buffer[i] = (buffer[i] ^ ((buffer[i-1] ^ (buffer[i-1] >>> 30)) * FACTOR2)) + seed[j] + j;
			i++; 
			j++;
			if (i >= N) { 
				buffer[0] = buffer[N-1]; 
				i = 1; 
			}
			if (j >= seed.length) {
				j = 0;
			}
			k--;
		}
		for (k = N-1; k > 0; k--) {
			buffer[i] = (buffer[i] ^ ((buffer[i-1] ^ (buffer[i-1] >>> 30)) * FACTOR3)) - i;
			i++;
			if (i >= N) { 
				buffer[0] = buffer[N-1]; 
				i = 1; 
			}
		}
		buffer[0] = UPPER_MASK; 
	}

	/*
	 * This is only used for the INIT_SEED required by the Marsenne Twister. Should
	 * be called before the real (int array) seed method 
	 */
	private void seed(int seed) {
		buffer[0] = seed;
		for (index = 1; index < N; index++) {
			buffer[index] = (FACTOR1 * (buffer[index-1] ^ (buffer[index-1] >>> 30)) + index);
		}
	}
}
