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
package com.cubeia.firebase.server.util;

import junit.framework.TestCase;


public class ThreadPoolPropertiesTest extends TestCase {

	public void testParse() throws Exception {
		ThreadPoolProperties p = new ThreadPoolProperties("2,3,40,true,10");
		assertEquals(2, p.getCoreSize());
		assertEquals(3, p.getMaxSize());
		assertEquals(40, p.getTimeout());
		assertEquals(true, p.isQueueingEnable());
		assertEquals(10, p.getQueueSize());
	}
	
	public void testParse2() throws Exception {
		ThreadPoolProperties p = new ThreadPoolProperties("5,10,4000");
		assertEquals(5, p.getCoreSize());
		assertEquals(10, p.getMaxSize());
		assertEquals(4000, p.getTimeout());
	}
	
	public void testParse3() {
		try {
			new ThreadPoolProperties("5,10,4000,false,10,3");
			fail("Expected illegal argument exception.");
		} catch(Exception e) { }
	}
}
