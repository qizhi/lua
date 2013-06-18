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
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.cubeia.firebase.service.wlist.model.FilteredRequest;
import com.cubeia.firebase.service.wlist.model.RequestQueue;


/**
 * SortedSet implementation of a RequestQueue
 * 
 * 
 * @author Fredrik
 *
 */
public class SortedRequestSet implements RequestQueue {

	private SortedSet<FilteredRequest> requests = Collections.synchronizedSortedSet(new TreeSet<FilteredRequest>());
	
	public void addRequest(FilteredRequest request) {
		requests.add(request);
	}

	/**
	 * Remove the request from this queue
	 */
	public boolean remove(FilteredRequest request) {
		return requests.remove(request);
	}
	
	
	public boolean returnRequest(FilteredRequest request) {
		requests.add(request);
		if(requests.first() == request) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * So.. like, whats your size then?
	 */
	public int size() {
		return requests.size();
	}

	/**
	 * Creating a new immutable list here is not very performant
	 * or memory-effective. 
	 * 
	 * I have tried returning newly created lists and
	 * Collections.unmodifiableSortedSet(...) but nothing  
	 * as worked for me other then the array-thingy below... 
	 * 
	 */
	public Collection<FilteredRequest> values() {
		FilteredRequest[] array = requests.toArray(new FilteredRequest[]{});
		return Arrays.asList(array);
	}

}
