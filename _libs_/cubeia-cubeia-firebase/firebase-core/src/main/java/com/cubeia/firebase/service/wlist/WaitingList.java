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
package com.cubeia.firebase.service.wlist;

import java.util.Map;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;

public interface WaitingList {
	
	/**
	 * <p>Add a filtered join request to the waiting list.</p>
	 * 
	 * <p>This request will always be placed in the end of the line.</p>
	 * 
	 * @param action
	 */
	public void addJoinRequest(FilteredRequest request);
	
	/**
	 * <p>Return a request to its proper place in the waiting list.</p>
	 * 
	 * <p>This request will be placed in the line according to the 
	 * timestamp attribute. Use this method to replace the request if the
	 * seating should fail.</p>
	 * 
	 * @param action
	 * @return True if the request is at the head of the list
	 */
	public boolean returnRequest(FilteredRequest request);
	
	/**
	 * Remove this request.
	 * 
	 * @param request
	 */
	public void removeRequest(FilteredRequest request);
	
	/**
	 * <p>Get all NodeLists for the given FQN and all FQN's branching
	 * downwards and find the longest waiting match.
	 * </p>
	 * 
	 * <p>If the table is newly created or attributes were bulk added,
	 * you can send in either an empty string ("") or null as attribute.</p>
	 * 
	 * @param fqn LobbyPath of the changed data
	 * @param attribute, can be "" or null as well.
	 * @param data, never null
	 * @return The match or null if not found
	 */
	public FilteredRequest getMatch(LobbyPath path, String attribute, Map<?, ?> data);
	
	/**
	 * <p>Get all NodeLists for the given FQN and all FQN's branching
	 * downwards and find the longest waiting match.
	 * </p>
	 * <p>If there is only a specific attribute that has
	 * changed, then you should use the reportChange method that accepts the more fine-grained
	 * approach of checking requests that maps against the attribute.
	 * </p>
	 * 
	 * @return The match or null if not found 
	 * @param fqn LobbyPath of the changed data
	 * @param attribute, can be "" or null as well.
	 * @param data, never null
	 */
	public FilteredRequest getMatch(LobbyPath path, Map<?, ?> data);
	
	/**
	 * Great debugging opportunities here!
	 *
	 */
	public void dumpToLog();
	
}
