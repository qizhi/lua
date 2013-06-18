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

import java.util.Random;

/**
 * A random implementation that takes it's randomness from 
 * a marsenne twister sequence. This random is not possible
 * to {@link #setSeed(long) seed} manually. As all Random objects
 * if is thread safe for concurrent use.
 * 
 * @author Lars J. Nilsson
 */
public class RandomImpl extends Random implements InternalRandom {

	private static final long serialVersionUID = 1L;
	
	private final MarsenneTwister mt;

	RandomImpl(MarsenneTwister mt) {
		notNull(mt, "marsenne twister");
		this.mt = mt;	
	}
	
	@Override
	public synchronized int next(int bits) {
		return mt.next(bits);
	}
}
