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
package com.cubeia.firebase.api.lobby;

import junit.framework.TestCase;

public class LobbyPathUtilTest extends TestCase {

	public void testGetAncestor() {
		LobbyPath path = new LobbyPath(LobbyPathType.TABLES, 99, "a/b/", 123);
		LobbyPath ancestor = LobbyPathUtil.getAncestor(path);
		assertEquals(LobbyPathType.TABLES, ancestor.getType());
		assertEquals(99, ancestor.getArea());
		assertEquals(-1, ancestor.getObjectId());
		assertEquals("a/", ancestor.getDomain());
	}
	
	
	public void testGetAncestor2() {
		LobbyPath path = new LobbyPath(LobbyPathType.TABLES, 99, "a", 123);
		LobbyPath ancestor = LobbyPathUtil.getAncestor(path);
		assertEquals(LobbyPathType.TABLES, ancestor.getType());
		assertEquals(99, ancestor.getArea());
		assertEquals(-1, ancestor.getObjectId());
		assertEquals("", ancestor.getDomain());
	}
	
	public void testGetAncestor3() {
		LobbyPath path = new LobbyPath(LobbyPathType.TABLES, 99, "", 123);
		LobbyPath ancestor = LobbyPathUtil.getAncestor(path);
		assertEquals(LobbyPathType.TABLES, ancestor.getType());
		assertEquals(99, ancestor.getArea());
		assertEquals(-1, ancestor.getObjectId());
		assertEquals("", ancestor.getDomain());
	}


	public void testGetAncestor4() {
		LobbyPath path = new LobbyPath(LobbyPathType.TABLES, 99, "", 123);
		LobbyPath ancestor = LobbyPathUtil.getAncestor(path);
		assertEquals(LobbyPathType.TABLES, ancestor.getType());
		assertEquals(99, ancestor.getArea());
		assertEquals(-1, ancestor.getObjectId());
		assertEquals("", ancestor.getDomain());
		
	}



}
