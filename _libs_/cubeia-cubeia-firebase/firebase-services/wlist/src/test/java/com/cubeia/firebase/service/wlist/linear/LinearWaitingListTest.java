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
package com.cubeia.firebase.service.wlist.linear;

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.cubeia.firebase.api.action.local.FilteredJoinAction;
import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.service.wlist.WaitingListGenerator;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;

public class LinearWaitingListTest extends TestCase {

	public void testAddJoinWithDateRequest() throws Exception {
		List<Parameter<?>> params11 = WaitingListGenerator.createParams("apa", 5, new Date(System.currentTimeMillis()));
		FilteredJoinAction action11 = WaitingListGenerator.createAction(11, 99, "", params11);
		FilteredRequest req1 = WaitingListGenerator.createRequest(action11);
		LinearWaitingList list = new LinearWaitingList();
		list.addJoinRequest(req1);
		// list.dumpToLog();
		// Lobby paths
		LobbyPath pathA = new LobbyPath(99, "a/", 1);
		// LobbyPath pathB = new LobbyPath(99, "b/", 1);
		
		// Create a data that shouldn't match 99/a and p2 > 5 and date
		Map<String, Object> data1 = WaitingListGenerator.createData("apa", 6, new Date(System.currentTimeMillis() - 10000));
		FilteredRequest match1 = list.getMatch(pathA, "p3", data1);
		assertNull(match1);
		
		// Create a data that should match 99/a and p2 > 5 and date
		data1 = WaitingListGenerator.createData("apa", 6, new Date(System.currentTimeMillis() + 10000));
		match1 = list.getMatch(pathA, "p3", data1);
		assertNotNull(match1);
		assertEquals(11, match1.getPlayerId());
	}
	
	public void testReturnsAtHead() throws Exception {
		List<Parameter<?>> params11 = WaitingListGenerator.createParams("apa", 5);
		List<Parameter<?>> params22 = WaitingListGenerator.createParams("apa", 4);
		
		FilteredJoinAction action11 = WaitingListGenerator.createAction(11, 99, "", params11);
		FilteredJoinAction action22 = WaitingListGenerator.createAction(22, 99, "a", params22);
		
		FilteredRequest req1 = WaitingListGenerator.createRequest(action11);
		Thread.sleep(32);
		FilteredRequest req2 = WaitingListGenerator.createRequest(action22);
		Thread.sleep(32);
		
		LinearWaitingList list = new LinearWaitingList();
		list.addJoinRequest(req1);
		list.addJoinRequest(req2);
		
		//list.dumpToLog();
		
		// Lobby paths
		LobbyPath pathA = new LobbyPath(99, "a/", 1);
		// LobbyPath pathB = new LobbyPath(99, "b/", 1);
		
		// Create a data that should match 99/a and p2 > 5
		Map<String, Object> data1 = WaitingListGenerator.createData("apa", 6);
		FilteredRequest match1 = list.getMatch(pathA, "p2", data1);
		assertEquals(11, match1.getPlayerId());
		
		// If we return this, it should be placed at head
		assertTrue(list.returnRequest(match1));
	}
	
	public void testAddJoinRequest() throws Exception {
		List<Parameter<?>> params11 = WaitingListGenerator.createParams("apa", 5);
		List<Parameter<?>> params22 = WaitingListGenerator.createParams("apa", 4);
		List<Parameter<?>> params33 = WaitingListGenerator.createParams("apa", 3);
		List<Parameter<?>> params44 = WaitingListGenerator.createParams("apa", 2);
		List<Parameter<?>> params55 = WaitingListGenerator.createParams("apa", 1);
		
		FilteredJoinAction action11 = WaitingListGenerator.createAction(11, 99, "", params11);
		FilteredJoinAction action22 = WaitingListGenerator.createAction(22, 99, "a", params22);
		FilteredJoinAction action33 = WaitingListGenerator.createAction(33, 99, "b", params33);
		FilteredJoinAction action44 = WaitingListGenerator.createAction(44, 99, "a", params44);
		FilteredJoinAction action55 = WaitingListGenerator.createAction(55, 99, "", params55);
		
		FilteredRequest req1 = WaitingListGenerator.createRequest(action11);
		Thread.sleep(32);
		FilteredRequest req2 = WaitingListGenerator.createRequest(action22);
		Thread.sleep(32);
		FilteredRequest req3 = WaitingListGenerator.createRequest(action33);
		Thread.sleep(32);
		FilteredRequest req4 = WaitingListGenerator.createRequest(action44);
		FilteredRequest req5 = WaitingListGenerator.createRequest(action55);
		
		LinearWaitingList list = new LinearWaitingList();
		list.addJoinRequest(req1);
		list.addJoinRequest(req2);
		list.addJoinRequest(req3);
		list.addJoinRequest(req4);
		list.addJoinRequest(req5);
		
		//list.dumpToLog();
		
		// Lobby paths
		LobbyPath pathA = new LobbyPath(99, "a/", 1);
		LobbyPath pathB = new LobbyPath(99, "b/", 1);
		
		// Create a data that should match 99/a and p2 > 5
		Map<String, Object> data1 = WaitingListGenerator.createData("apa", 6);
		FilteredRequest match1 = list.getMatch(pathA, "p2", data1);
		assertEquals(11, match1.getPlayerId());
		
		// Next match should get player 22 since 11 is taken
		FilteredRequest match2 = list.getMatch(pathA, "p2", data1);
		assertEquals(22, match2.getPlayerId());
		
		// Return match1 and find best match which should return match1
		list.returnRequest(match1);
		FilteredRequest match3 = list.getMatch(pathA, "p2", data1);
		assertEquals(match1, match3);
		
		// Get a match in a different branch
		Map<String, Object> data2 = WaitingListGenerator.createData("apa", 6);
		FilteredRequest match4 = list.getMatch(pathB, "p2", data2);
		assertEquals(33, match4.getPlayerId());
		
		// Now we should get the root request
		FilteredRequest match5 = list.getMatch(pathB, "p2", data2);
		assertEquals(55, match5.getPlayerId());

		// This branch should be empty now
		FilteredRequest match6 = list.getMatch(pathB, "p2", data2);
		assertNull(match6);
	}
}
