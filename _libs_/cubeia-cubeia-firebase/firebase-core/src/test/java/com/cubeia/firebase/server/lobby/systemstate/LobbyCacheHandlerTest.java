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

import java.util.Collection;

import junit.framework.TestCase;

import com.cubeia.firebase.api.game.lobby.TableAttributeMapper;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;
import com.cubeia.firebase.server.lobby.systemstate.LobbyCacheHandler;
import com.cubeia.firebase.server.lobby.systemstate.LobbyListener;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;

public class LobbyCacheHandlerTest extends TestCase implements LobbyListener {

	private SystemStateCacheHandler cache = new SystemStateCacheHandler("com/cubeia/firebase/systemstate/systemstate-local-test-service.xml");
	
	private LobbyCacheHandler handler;
	
	protected void setUp() throws Exception {
		cache.start();
		handler = new LobbyCacheHandler(cache, this);
	}

	protected void tearDown() throws Exception {
		cache.stop();
		handler.destroy();
	}

	public void testGetSubNodes() {
		initSomeData();
		Collection<LobbyPath> subNodes = handler.getSubNodes("/table/99/a");
		assertEquals(3, subNodes.size());
		assertTrue(subNodes.contains(new LobbyPath(99, "a/")));
		assertTrue(subNodes.contains(new LobbyPath(99, "a/b/")));
		assertTrue(subNodes.contains(new LobbyPath(99, "a/c")));
	}
	
	public void testGetEndNodes() {
		initSomeData();
		Collection<LobbyPath> subNodes = handler.getEndNodes("/table/99/a");
		assertEquals(2, subNodes.size());
		assertTrue(subNodes.contains(new LobbyPath(99, "a/b")));
		assertTrue(subNodes.contains(new LobbyPath(99, "a/c")));
	}
	
	public void testGetSubNodesFromRoot() {
		initSomeData();
		Collection<LobbyPath> subNodes = handler.getSubNodes("/table/99/");
		assertEquals(4, subNodes.size());
		assertTrue(subNodes.contains(new LobbyPath(99, "")));
		assertTrue(subNodes.contains(new LobbyPath(99, "a/")));
		assertTrue(subNodes.contains(new LobbyPath(99, "a/b/")));
		assertTrue(subNodes.contains(new LobbyPath(99, "a/c")));
	}
	
	public void testGetEndNodesFromRoot() {
		initSomeData();
		Collection<LobbyPath> subNodes = handler.getEndNodes("/table/99/");
		assertEquals(2, subNodes.size());
		assertTrue(subNodes.contains(new LobbyPath(99, "a/b/")));
		assertTrue(subNodes.contains(new LobbyPath(99, "a/c")));
	}
	
	public void testGetSubNodesFromEmpty() {
		cache.updateAttribute("/table/99/1", TableAttributeMapper.NODE_TYPE_ATTRIBUTE_NAME, TableAttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE);
		Collection<LobbyPath> subNodes = handler.getSubNodes("/table/99/");
		System.out.println("SubNodes: "+subNodes);
		assertEquals(1, subNodes.size());
		assertTrue(subNodes.contains(new LobbyPath(99, "")));
	}

	
	
	/**
	 * Populate the cache with some simple data
	 */
	private void initSomeData() {
		cache.updateAttribute("/table/99/a/b/1", TableAttributeMapper.NODE_TYPE_ATTRIBUTE_NAME, TableAttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE);
		cache.updateAttribute("/table/99/a/c/1", TableAttributeMapper.NODE_TYPE_ATTRIBUTE_NAME, TableAttributeMapper.TABLE_NODE_TYPE_ATTRIBUTE_VALUE);
	}
	
	public void nodeAttributeChanged(NodeChangeDTO change) {}
	public void nodeCreated(LobbyPath path) {}
	public void tableRemoved(LobbyPath path) {}
	public void nodeRemoved(String path) { }

}
