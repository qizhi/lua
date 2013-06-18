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

public class AddressPatternTest extends TestCase {

	public void test1() throws Exception {
		AddressPattern a = new AddressPattern("*.*.*.1", 1);
		SocketAddress test = a.generate(new SocketAddress("224.224.0.0:2000"));
		assertEquals(new SocketAddress("224.224.0.1:2001"), test);
	}
	
	public void test2() throws Exception {
		AddressPattern a = new AddressPattern("*.-1.*.-1", -1);
		SocketAddress test = a.generate(new SocketAddress("224.224.0.0:2000"));
		assertEquals(new SocketAddress("224.223.0.255:1999"), test);
	}
	
	public void test3() throws Exception {
		AddressPattern a = new AddressPattern("*.50.*.1", 100);
		SocketAddress test = a.generate(new SocketAddress("224.224.0.0:2000"));
		assertEquals(new SocketAddress("224.18.0.1:2100"), test);
	}
}
