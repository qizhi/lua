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
package com.cubeia.firebase.service.wlist.queue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;
import com.cubeia.firebase.service.wlist.model.RequestQueue;


/**
 * This implementation uses a Priority Blocking Queue as the queue implementation
 * of choice.
 * 
 * The method returning values() might not be the optimum. Also, I am not sure there
 * are any guarantees that the values().iterator() will iterate in correct order.
 * 
 * All tests indicate that it will, but it is not specified in the javadocs... =/
 * 
 * @author Fredrik
 *
 */
public class SimpleRequestQueue implements RequestQueue {

	private Queue<FilteredRequest> queue = new PriorityBlockingQueue<FilteredRequest>();
	
	public void addRequest(FilteredRequest request) {
		Arguments.notNull(request, "request");
		queue.add(request);
	}

	public boolean remove(FilteredRequest request) {
		return queue.remove(request);
	}

	public boolean returnRequest(FilteredRequest request) {
		queue.add(request);
		if(queue.peek() == request) {
			return true;
		} else {
			return false;
		}
	}

	public int size() {
		return queue.size();
	}

	/**
	 * Return an iterable collection.
	 * 
	 * Since the Queue implementation cannot provide an ordered iterator
	 * we create an array, sort it and then create a list of it.
	 * 
	 * No, this is not performant. As a matter of fact, about 39% of all CPU
	 * when running load tests on the waiting list is spent in the 
	 * Arrays.sort(...).
	 * 
	 * However, using a queue implementation we are stuck with this.
	 * 
	 */
	public Collection<FilteredRequest> values() {
		FilteredRequest[] requests = queue.toArray(new FilteredRequest[]{});
		Arrays.sort(requests);
		return Arrays.asList(requests);
	}

}
