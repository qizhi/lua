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
package com.cubeia.firebase.service.wlist.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.service.wlist.WaitingListGenerator;
import com.cubeia.firebase.service.wlist.linear.SimpleFilteredRequest;
import com.cubeia.firebase.service.wlist.queue.SortedRequestSet;

public class SimpleFilteredRequestTest extends TestCase {

	
	public void testComparable() throws Exception {
		// Create request1 first
		FilteredRequest request1 = WaitingListGenerator.createRequest(11, 99, "apa", 1);
		Thread.sleep(60);
		
		FilteredRequest request2 = WaitingListGenerator.createRequest(22, 99, "apa", 2);
		Thread.sleep(60);

		FilteredRequest request3 = WaitingListGenerator.createRequest(33, 99, "apa", 3);
		Thread.sleep(60);

		FilteredRequest request4 = WaitingListGenerator.createRequest(44, 99, "apa", 4);
		FilteredRequest request5 = WaitingListGenerator.createRequest(55, 99, "apa", 5);
		
		// Add them to a sorted set, but add them in wrong order
		// the request queue should sort them out
		RequestQueue queue = new SortedRequestSet();
		queue.addRequest(request5);
		queue.addRequest(request2);
		queue.addRequest(request3);
		queue.addRequest(request1);
		queue.addRequest(request4);
		
		
		assertEquals(queue.size(), 5);
		
		
		Iterator<FilteredRequest> iterator = queue.values().iterator();
		assertEquals(request1, iterator.next());
		assertEquals(request2, iterator.next());
		assertEquals(request3, iterator.next());
		
	}
	
	public void testPreMatch() {
		List<Parameter<?>> params = WaitingListGenerator.createParams("apa", 3);
		
		FilteredRequest request = new SimpleFilteredRequest(22, WaitingListGenerator.path, params);
		Map<String, Object> data = WaitingListGenerator.createData("apa", 1);
		
		// Test pre-matching of String attributes
		boolean pre = request.preMatch("p0", data.get("p0"));
		assertFalse(pre);
		pre = request.preMatch("p1", data.get("p1"));
		assertTrue(pre);
		

		// Test a pre matching of integer attribute
		pre = request.preMatch("p2", data.get("p2"));
		assertFalse(pre);
		
		data.put("p2", new Integer(5));
		pre = request.preMatch("p2", data.get("p2"));
		assertTrue(pre);
		
		
	}


	public void testMatch() {
		List<Parameter<?>> params = WaitingListGenerator.createParams("apa", 3);
		FilteredRequest request = new SimpleFilteredRequest(22, WaitingListGenerator.path, params);
		Map<String, Object> data = WaitingListGenerator.createData("apa", 1);
		
		boolean match = request.match(data);
		assertFalse(match);
		
		
		data.put("p2", new Integer(5));
		match = request.match(data);
		assertTrue(match);
		
		
		
	}
	
	
	
}
