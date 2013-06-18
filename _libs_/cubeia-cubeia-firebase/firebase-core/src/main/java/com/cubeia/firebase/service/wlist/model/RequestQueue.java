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

import java.util.Collection;



public interface RequestQueue {
	
	/**
	 * Inserts a request in the end of the queue.
	 * Timestamp sorting can be ignored.
	 * 
	 * @param request
	 */
	public void addRequest(FilteredRequest request);
	
	/**
	 * Insert a request in its proper sorted position.
	 * 
	 * @param request
	 * @return True it the request is at the head of the queue
	 */
	public boolean returnRequest(FilteredRequest request);
	
	public int size();
	
	public boolean remove(FilteredRequest request);
	
	/**
	 * Return an immutable, thread safe (non-fail-fast)iterable list of requests.
	 * 
	 * @return
	 */
	public Collection<FilteredRequest> values();
}
