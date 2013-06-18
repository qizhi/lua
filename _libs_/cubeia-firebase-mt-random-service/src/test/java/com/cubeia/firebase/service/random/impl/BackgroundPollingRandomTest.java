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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class BackgroundPollingRandomTest {

	@Test
	public void testSingleAccess() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		InternalRandom wrapped = new InternalRandom() {
			
			@Override
			public int next(int bits) {
				latch.countDown();
				return 0;
			}
		};
		BackgroundPollingRandom rand = new BackgroundPollingRandom(wrapped, 1, 10);
		rand.start();
		try {
			if(!latch.await(10000, MILLISECONDS)) {
				fail("no poll made within ten seconds (highly unlikely)");
			}
		} finally {
			rand.stop();
		}
	}
}
