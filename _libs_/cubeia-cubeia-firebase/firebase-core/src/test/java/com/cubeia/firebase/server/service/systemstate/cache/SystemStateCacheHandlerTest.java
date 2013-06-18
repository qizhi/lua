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
package com.cubeia.firebase.server.service.systemstate.cache;

import java.util.Map;

import org.jboss.cache.Fqn;
import org.jboss.cache.Node;

import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;

import junit.framework.TestCase;

@SuppressWarnings({ "rawtypes" })
public class SystemStateCacheHandlerTest extends TestCase {

	private SystemStateCacheHandler handler = new SystemStateCacheHandler("com/cubeia/firebase/systemstate/systemstate-local-test-service.xml");
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		handler.start();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler.stop();
	}
	
	public void testGetSubNodes() {
		initSomeData();
		
		Map<Fqn<?>, Node> subNodes = handler.getSubNodes("/a", true);
		System.out.println("SubNode: "+subNodes.keySet());
		assertEquals(4, subNodes.size());
		
		assertTrue(subNodes.containsKey(Fqn.fromString("/a")));
		assertTrue(subNodes.containsKey(Fqn.fromString("/a/b")));
		assertTrue(subNodes.containsKey(Fqn.fromString("/a/c")));
		assertTrue(subNodes.containsKey(Fqn.fromString("/a/c/d")));
	}

	
	public void testGetSubNodesRoot() {
		initSomeData();
		Map<Fqn<?>, Node> subNodes = handler.getSubNodes("/", true);
		System.out.println("SubNode: "+subNodes.keySet());
		assertEquals(5, subNodes.size());
		
		assertTrue(subNodes.containsKey(Fqn.fromString("/")));
		assertTrue(subNodes.containsKey(Fqn.fromString("/a")));
		assertTrue(subNodes.containsKey(Fqn.fromString("/a/b")));
		assertTrue(subNodes.containsKey(Fqn.fromString("/a/c")));
		assertTrue(subNodes.containsKey(Fqn.fromString("/a/c/d")));
	}
	
	public void testGetSubNodesEmpty() {
		Map<Fqn<?>, Node> subNodes = handler.getSubNodes("/", true);
		System.out.println("SubNode: "+subNodes.keySet());
		assertEquals(1, subNodes.size());
		
		assertTrue(subNodes.containsKey(Fqn.fromString("/")));
	}
	
	
	/**
	 * Populate the cache with some simple data
	 */
	private void initSomeData() {
		handler.updateAttribute("/a/b", "1", "one");
		handler.updateAttribute("/a/c", "2", "two");
		handler.updateAttribute("/a/c/d", "3", "three");
	}

}
