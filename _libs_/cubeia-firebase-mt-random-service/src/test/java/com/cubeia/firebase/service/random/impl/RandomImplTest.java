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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class RandomImplTest {

	@Test
	public void testCreate() throws Exception {
		MarsenneTwister mt = mock(MarsenneTwister.class);
		new RandomImpl(mt).nextBoolean();
		verify(mt).next(anyInt());
	}
	
	/*@Test(expected=UnsupportedOperationException.class)
	public void testFailReseed() throws Exception {
		MarsenneTwister mt = mock(MarsenneTwister.class);
		new RandomImpl(mt).setSeed(0);
	}*/
}
