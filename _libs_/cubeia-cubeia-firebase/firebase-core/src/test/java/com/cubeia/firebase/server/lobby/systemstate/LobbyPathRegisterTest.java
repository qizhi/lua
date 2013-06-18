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
package com.cubeia.firebase.server.lobby.systemstate;

import java.util.Set;

import com.cubeia.firebase.server.lobby.systemstate.LobbyPathRegister;

import junit.framework.TestCase;

public class LobbyPathRegisterTest extends TestCase {

	private LobbyPathRegister reg;
	
	@Override
	protected void setUp() throws Exception {
		reg = new LobbyPathRegister();
	}
	
	public void testNodeAdd() throws Exception {
		addTestNodes();
		
		/*
		 * Answer:
		 * 
		 *  /a
		 *  /a/b
		 *  /a/b/c
		 *  /f
		 */
		
		Set<String> nodes = reg.getNodes("/");
		
		assertEquals(4, nodes.size());
		
		assertTrue(nodes.contains("/a"));
		assertTrue(nodes.contains("/a/b"));
		assertTrue(nodes.contains("/a/b/c"));
		assertTrue(nodes.contains("/f"));
		
	}
	
	public void testEndNodeAdd() throws Exception {
		addTestNodes();
		
		/*
		 * Answer:
		 * 
		 *  /a/b/c
		 *  /f
		 */
		
		Set<String> nodes = reg.getEndNodes("/");
		
		assertEquals(2, nodes.size());

		assertTrue(nodes.contains("/a/b/c"));
		assertTrue(nodes.contains("/f"));
		
	}
	
	public void testNodeGet() throws Exception {
		addTestNodes();
		
		/*
		 * Answer:
		 * 
		 *  /a
		 *  /a/b
		 *  /a/b/c
		 */
		
		Set<String> nodes = reg.getNodes("/a");
		
		assertEquals(3, nodes.size());
		
		assertTrue(nodes.contains("/a"));
		assertTrue(nodes.contains("/a/b"));
		assertTrue(nodes.contains("/a/b/c"));
		
	}
	
	public void testEndNodeGet() throws Exception {
		addTestNodes();
		
		/*
		 * Answer:
		 * 
		 *  /a/b/c
		 */
		
		Set<String> nodes = reg.getEndNodes("/a");
		
		assertEquals(1, nodes.size());

		assertTrue(nodes.contains("/a/b/c"));
		
	}
	
	public void testNodeRem() throws Exception {
		addTestNodes();
		
		reg.unregisterNode("/a/b/c");
		
		Set<String> nodes = reg.getNodes("/");
		
		assertEquals(3, nodes.size());
		
		assertTrue(nodes.contains("/a"));
		assertTrue(nodes.contains("/a/b"));
		assertTrue(nodes.contains("/f"));
		
		nodes = reg.getEndNodes("/a");
		assertEquals(1, nodes.size());
		assertTrue(nodes.contains("/a/b"));
		
		reg.unregisterNode("/");
		
		nodes = reg.getNodes("/");
		
		assertEquals(0, nodes.size());
		
	}
	
	private void addTestNodes() {
		reg.registerLeaf("/a/b/c/d");
		reg.registerPath("/a/b/");
		reg.registerLeaf("/a/e");
		reg.registerPath("/f");
	}
}
