/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cubeia.firebase.test.common.rules;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.test.common.rules.Expect.Action;
import com.cubeia.firebase.test.common.rules.impl.ObjectExpect;
import com.cubeia.firebase.test.common.rules.impl.RandomGroupExpect;

public class RandomGroupExpectTest {

	@Test
	public void testRandomSparseGroup() throws Exception {
		ObjectExpect one = new ObjectExpect("1");
		ObjectExpect two = new ObjectExpect("2");
		ObjectExpect four = new ObjectExpect("4");
		
		RandomGroupExpect e = new RandomGroupExpect(one, two, four);
		
		Assert.assertEquals(e.accept("3"), Action.PASS_THROUGH);
		Assert.assertEquals(e.accept("2"), Action.PASS_THROUGH);
		Assert.assertEquals(e.accept("4"), Action.PASS_THROUGH);
		Assert.assertEquals(e.accept("1"), Action.DONE);
	}
	
	@Test
	public void testRandomGroup() throws Exception {
		ObjectExpect one = new ObjectExpect("1");
		ObjectExpect two = new ObjectExpect("2");
		ObjectExpect four = new ObjectExpect("4");
		
		RandomGroupExpect e = new RandomGroupExpect(one, two, four);
		
		Assert.assertEquals(e.accept("1"), Action.PASS_THROUGH);
		Assert.assertEquals(e.accept("4"), Action.PASS_THROUGH);
		Assert.assertEquals(e.accept("2"), Action.DONE);
	}
}
