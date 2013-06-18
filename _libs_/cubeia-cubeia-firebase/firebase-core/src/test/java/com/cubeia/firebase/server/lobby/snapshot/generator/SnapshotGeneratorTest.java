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
package com.cubeia.firebase.server.lobby.snapshot.generator;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.server.lobby.snapshot.FullSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;
import com.cubeia.firebase.server.lobby.systemstate.StateLobby;
import com.cubeia.firebase.server.lobby.systemstate.StateLobbyAlternative;
import com.cubeia.firebase.server.lobby.systemstate.SystemStateTestGenerator;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;


public class SnapshotGeneratorTest {
	
	private SystemStateCacheHandler cache = new SystemStateCacheHandler("com/cubeia/firebase/systemstate/systemstate-local-test-service.xml");
	private StateLobby lobby;
	private SnapshotGenerator generator;
	
	@Before
	public void setUp() throws Exception {
		cache.start();
		
		lobby = new StateLobbyAlternative(cache);
		lobby.setBroadcastPeriod(10);
		lobby.start();
		// cache.dumpInfo();
		
		Thread.sleep(20);
		
		generator = new TableSnapshotGenerator(lobby, LobbyPathType.TABLES);
	}
	
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testCreate() {
		LobbyPath path = new LobbyPath(99, "xyz");
		Map attributes = SystemStateTestGenerator.createTableAttributes(3, "Table_3");
		NodeChangeDTO created = new NodeChangeDTO(path, attributes, false, attributes);
		generator.nodeAttributeChanged(created);
		
		ConcurrentMap<LobbyPath, FullSnapshot> fullSnapshots = generator.getFullSnapshots();
		Assert.assertNotNull(fullSnapshots.get(path));
		Assert.assertEquals(1, fullSnapshots.get(path).getLobbyData().size());
		
		
		
	}
	
}
