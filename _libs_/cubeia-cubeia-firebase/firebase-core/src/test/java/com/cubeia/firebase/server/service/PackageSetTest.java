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
package com.cubeia.firebase.server.service;

import com.cubeia.firebase.server.service.PackageSet;
import com.cubeia.firebase.server.service.PackageSetImpl;

import junit.framework.TestCase;

public class PackageSetTest extends TestCase {

	private PackageSet set;
	
	@Override
	protected void setUp() throws Exception {
		set = new PackageSetImpl("test");
		set.addResource("com.game.one.-");
		set.addResource("com.game.two.*");
		set.addResource("com.game.three.Kalle");
	}
	
	public void testRecursive() {
		assertTrue(set.impliesResource("com.game.one.Kalle"));
		assertTrue(set.impliesResource("com.game.one.next.Kalle"));
		assertFalse(set.impliesResource("com.game.Kalle"));
	}
	
	public void testSamePack() {
		assertTrue(set.impliesResource("com.game.two.Kalle"));
		assertFalse(set.impliesResource("com.game.two.next.Kalle"));
		assertFalse(set.impliesResource("com.game.Kalle"));
	}
	
	public void testClass() {
		assertTrue(set.impliesResource("com.game.three.Kalle"));
		assertFalse(set.impliesResource("com.game.three.next.Kalle"));
		assertFalse(set.impliesResource("com.game.three.Olle"));
		assertFalse(set.impliesResource("com.game.Kalle"));
	}
}
