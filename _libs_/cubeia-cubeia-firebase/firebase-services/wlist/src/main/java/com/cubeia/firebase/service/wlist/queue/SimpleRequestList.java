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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.service.wlist.model.FilteredRequest;
import com.cubeia.firebase.service.wlist.model.RequestQueue;


/**
 * Simple implementation of a RequestList
 * 
 * This implementation uses LinkedList to keep the requests orderd.
 * For every added request we will re-sort the list. This is not very performant.
 * 
 * The SimpleLinkedQueue does not need to resort on add, but on the other hand the
 * SimpleLinkedQueue iterator is arbitrary. =/
 * 
 * @author Fredrik
 *
 */
@Deprecated
public class SimpleRequestList implements RequestQueue {

	private List<FilteredRequest> requests = Collections.synchronizedList(new LinkedList<FilteredRequest>());
	
	/**
	 * Only place last in queue.
	 * Nevermind resorting.
	 */
	public void addRequest(FilteredRequest request) {
		requests.add(request);
	}

	/**
	 * This request is returned, so we need to
	 * resort the list to ensure fariness.
	 */
	public boolean returnRequest(FilteredRequest request) {
		requests.add(request);
		return false;
//		synchronized (requests) {
//			Collections.sort(requests);
//		}
	}
	
	/**
	 * Remove the request from this queue
	 */
	public boolean remove(FilteredRequest request) {
		return requests.remove(request);
	}
	
	
	

	/**
	 * So.. like, whats your size then?
	 */
	public int size() {
		return requests.size();
	}

	/**
	 * Creating a new immutable list here is not very performant
	 * or memory-effective. However, since we using a LinkedList 
	 * implementation which is fail-fast we need to return an 
	 * immutable copy.
	 * 
	 */
	public List<FilteredRequest> values() {
		return new LinkedList<FilteredRequest>(requests);
	}

}
