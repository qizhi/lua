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

import java.security.SecureRandom;

/**
 * This is a wrapped random that adds discarded draws. For every integer 
 * drawn from the underlying random, this random will draw 0-N other integers
 * and discard them.
 * 
 * @author Lars J. Nilsson
 */
public class DiscardedDrawRandom extends RandomWrapper implements InternalRandom {

	private static final long serialVersionUID = 1L;
	
	private final int DISCARDED_BITS = 1; // draws an INT from the sequence anyway
	
	private final int maxDiscard;
	private final SecureRandom random = new SecureRandom();
	
	/**
	 * @param wrapped Underlying random to use, must not be null
	 * @param maxDiscard Max (N) discarded integers per draw, must be positive
	 */
	DiscardedDrawRandom(InternalRandom wrapped, int maxDiscard) {
		super(wrapped);
		this.maxDiscard = maxDiscard;
	}
	
	@Override
	public int next(int bits) {
		doDiscard();
		return super.next(bits);
	}
	
	
	// --- PRIVATE METHODS --- //

	private void doDiscard() {
		int len = random.nextInt(maxDiscard);
		for (int i = 0; i < len; i++) {
			super.next(DISCARDED_BITS);
		}
	}
}
