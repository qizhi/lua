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

import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class DiscardedDrawRandomTest {

	@Test
	public void testSingleDraw() {
		final AtomicInteger count = new AtomicInteger();
		InternalRandom wrapped = new InternalRandom() {
			
			@Override
			public int next(int bits) {
				count.incrementAndGet();
				return 0;
			}
		};
		DiscardedDrawRandom random = new DiscardedDrawRandom(wrapped, 100);
		for (int i = 0; i < 1000; i++) {
			random.nextBoolean();
			if(count.get() > (i + 1)) {
				/*
				 * this means where done, there has been more draws
				 * then accounted for by us...
				 */
				return;
			}
		}
		fail("no discard made within 1000 attempts (highly unlikely)");
	}
}
