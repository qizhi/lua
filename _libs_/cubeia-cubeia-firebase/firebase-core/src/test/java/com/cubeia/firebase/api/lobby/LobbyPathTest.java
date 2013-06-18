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

import static com.cubeia.firebase.api.lobby.LobbyPathType.MTT;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.lobby.LobbyPathUtil;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;

public class LobbyPathTest extends TestCase {

	
	public void testEquality() {
		LobbyPath path1 = new LobbyPath(99, "a/b/");
		LobbyPath path2 = new LobbyPath(99, "a/b/");
		
		assertTrue(path1.equals(path2));
		assertEquals(path1.hashCode(), path2.hashCode());
		
	}
	
	public void testEqualityType() {
		LobbyPath path1 = new LobbyPath(LobbyPathType.TABLES, 99, "a/b/", 1);
		LobbyPath path2 = new LobbyPath(LobbyPathType.TABLES, 99, "a/b/", 2);
		
		assertTrue(path1.equals(path2));
		assertEquals(path1.hashCode(), path2.hashCode());
		
	}
	
	public void testUnequalityType() {
		LobbyPath path1 = new LobbyPath(LobbyPathType.TABLES, 99, "a/b/", 1);
		LobbyPath path2 = new LobbyPath(LobbyPathType.MTT, 99, "a/b/", 2);
		
		assertFalse(path1.equals(path2));
		assertNotSame(path1.hashCode(), path2.hashCode());
		
	}
	
	public void testAncestorEquality() {
		LobbyPath path1 = new LobbyPath(99, "a/b/");
		LobbyPath path2 = new LobbyPath(99, "a/b/");
		LobbyPath path3 = new LobbyPath(99);
		LobbyPath path4 = new LobbyPath(99);
		LobbyPath path5 = new LobbyPath(99, "jmx/", 123);
		
		
		Map<LobbyPath, String> map = new HashMap<LobbyPath, String>();
		map.put(path1, "1");
		map.put(path3, "3");
		
		assertEquals("1", map.get(path2));
		assertEquals("3", map.get(path4));
		assertEquals("3", map.get(LobbyPathUtil.getAncestor(path5)));
		
	}
	
	public void testLobbyPathIntStringString() {
		LobbyPath path = new LobbyPath(112, "a/b/", 9);
		assertEquals(path.getRoot(), SystemStateConstants.TABLE_ROOT_FQN+112);
		assertEquals(path.getDomain(), "a/b/");
		assertEquals(path.getSystemPath(), SystemStateConstants.TABLE_ROOT_FQN+"112/a/b/9");
		assertEquals(path.getNameSpace(), SystemStateConstants.TABLE_ROOT_FQN+"112/a/b/");
		assertEquals(path.getLobbyPath(), "a/b/9");
		assertEquals(path.getRootLobbyPath(), "112/a/b/");
		
		LobbyPath path2 = new LobbyPath(112, "/a/b", 9);
		assertEquals(path2.getRoot(), SystemStateConstants.TABLE_ROOT_FQN+112);
		assertEquals(path2.getDomain(), "a/b/");
		assertEquals(path2.getSystemPath(), SystemStateConstants.TABLE_ROOT_FQN+"112/a/b/9");
		
		LobbyPath path3 = new LobbyPath(112, "/", 9);
		assertEquals(path3.getRoot(), SystemStateConstants.TABLE_ROOT_FQN+112);
		assertEquals(path3.getDomain(), "");
		assertEquals(path3.getSystemPath(), SystemStateConstants.TABLE_ROOT_FQN+"112/9");
		
		
	}
	
	
	public void testRemoveTableId() {
		LobbyPath path = new LobbyPath(112, "a/b/", 9);
		
		String rem = LobbyPath.removeTableId(path.getLobbyPath());
		
		assertEquals(rem, "a/b/");
		assertEquals(rem, path.getDomain());
		
	}

	
	public void testParse() {
		String fqn = "/table/112/a/b/9";
		LobbyPath path = new LobbyPath().parseFqn(fqn);
		assertEquals(path.getDomain(), "a/b/");
		assertEquals(path.getObjectId(), 9);
		assertEquals(path.getNameSpace(), SystemStateConstants.TABLE_ROOT_FQN+"112/a/b/");
		
		String fqn2 = "/table/112/9";
		LobbyPath path2 = new LobbyPath().parseFqn(fqn2);
		assertEquals(path2.getObjectId(), 9);
		
		String fqn3 = "/table/6/large/5";
		LobbyPath path3 = new LobbyPath().parseFqn(fqn3);
		assertEquals("large/", path3.getDomain());
		assertEquals(5, path3.getObjectId());
		assertEquals(path3.getNameSpace(), SystemStateConstants.TABLE_ROOT_FQN+"6/large/");
        assertSame(LobbyPathType.TABLES, path3.getType());
	}
	
	public void testParseTournamentPaths() {
	    String path = "/tournament/1/dicetournament/2";
	    LobbyPath lobbyPath = new LobbyPath(MTT);
	    lobbyPath = lobbyPath.parseFqn(path);
	    
	    assertNotNull(lobbyPath);
	    assertEquals(1, lobbyPath.getArea());
	    assertEquals("dicetournament/", lobbyPath.getDomain());
        assertEquals(2, lobbyPath.getObjectId());
        assertSame(LobbyPathType.MTT, lobbyPath.getType());
	}
	
	public void testParseInvalid() {
		String path = "/table/4714";
		LobbyPath lobbyPath = new LobbyPath();
		assertNull(lobbyPath.parseFqn(path));
	}
	
	
	
	/*public void testParseFullFqn() {
		Fqn fqn1 = Fqn.fromString("/table/99/a/b/3");
		LobbyPath path1 = FqnUtil.parseFullFqn(fqn1);
		assertEquals(99, path1.getGameId());
		assertEquals("a/b/", path1.getDomain());
		assertEquals(3, path1.getTableId());
		
		Fqn fqn2 = Fqn.fromString("/table/99/1");
		LobbyPath path2 = FqnUtil.parseFullFqn(fqn2);
		assertEquals(99, path2.getGameId());
		assertEquals("", path2.getDomain());
		assertEquals(1, path2.getTableId());
	}*/
}
