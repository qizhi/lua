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

import java.util.Map;

import junit.framework.TestCase;

import com.cubeia.firebase.service.wlist.WaitingListGenerator;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;

public class NodeWaitingListTest extends TestCase {

	public void testAddRequest() throws Exception {
		FilteredRequest req1 = WaitingListGenerator.createRequest(11, 99, "", "apa", 5);
		Thread.sleep(32);
		FilteredRequest req2 = WaitingListGenerator.createRequest(22, 99, "", "apa", 4);
		Thread.sleep(32);
		FilteredRequest req3 = WaitingListGenerator.createRequest(33, 99, "", "apa", 3);
		Thread.sleep(32);
		FilteredRequest req4 = WaitingListGenerator.createRequest(44, 99, "", "apa", 2);
		FilteredRequest req5 = WaitingListGenerator.createRequest(55, 99, "", "apa", 1);
		
		
		
		// Create Node Waiting List
		NodeWaitingList list = new NodeWaitingList();
		list.addRequest(req1);
		list.addRequest(req2);
		list.addRequest(req3);
		list.addRequest(req4);
		list.addRequest(req5);
		
		
		assertEquals(5, list.getSize("p2"));
		
		//list.dumpToLog();
		
		// 1,2 and 3 should be ordered correctly like that. 4 and 5 can be in arbitrary order
		Map<String, Object> data = WaitingListGenerator.createData("apa", 4);
		FilteredRequest match = list.getMatch("p2", data);
		assertEquals(match.getPlayerId(), req3.getPlayerId());
		
		assertEquals(4, list.getSize("p2"));
		
		// Uh-oh.. we decided not to use the reported match so return it
		list.returnRequest(match);
		assertEquals(5, list.getSize("p2"));
		
		// We should get the same object
		FilteredRequest match2 = list.getMatch("p2", data);
		assertEquals(match, match2);
		
		assertEquals(4, list.getSize("p2"));
		
		
	}
	
	
}
