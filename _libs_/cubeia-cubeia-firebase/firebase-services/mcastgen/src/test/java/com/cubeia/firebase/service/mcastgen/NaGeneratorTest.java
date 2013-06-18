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
package com.cubeia.firebase.service.mcastgen;

import com.cubeia.firebase.api.util.SocketAddress;

import junit.framework.TestCase;

public class NaGeneratorTest extends TestCase {

	public void test1() throws Exception {
		NaGenerator na = new NaGenerator(new SocketAddress("224.224.0.0:6900"));
		assertEquals(new SocketAddress("224.224.0.1:6901"), na.generate());
		assertEquals(new SocketAddress("224.224.0.2:6902"), na.generate());
	}
	
	public void test2() throws Exception {
		NaGenerator na = new NaGenerator(new SocketAddress("224.224.0.254:6909"));
		assertEquals(new SocketAddress("224.224.0.255:6910"), na.generate());
		assertEquals(new SocketAddress("224.224.0.0:6911"), na.generate());
		assertEquals(new SocketAddress("224.224.0.1:6912"), na.generate());
	}
}
