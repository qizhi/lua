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

import com.cubeia.firebase.api.action.local.FilteredJoinAction;
import com.cubeia.firebase.api.action.local.FilteredJoinCancelAction;
import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.wlist.model.FilteredRequest;

/**
 * Receives requests for filtered joins and dispatches them accordingly.
 * The actual logic can vary with the implementation but the basic flow should follow:
 * 
 * 1. Check for free tables directly if so, then send join request
 * 
 * 2. If no direct join possible, then add to appropiate waiting list
 * 
 * The FilteredJoinService is also responsible for dispatching changes in the lobby
 * to the appropiate waiting list.
 *
 * @author Fredrik
 */
public interface FilteredJoinService {
	
	public void setGameRouter(Sender<GameEvent> gameRouter);
	
	/**
	 * Humbly request a filtered join.
	 * 
	 * @param action
	 */
	public void addFilteredJoinAction(FilteredJoinAction action, LocalActionHandler loopback);
	
	/**
	 * Cancels a request. Sequence number is used as identifier.
	 * 
	 * @param action not null
	 * @param loopback not null
	 */
	public void cancelFilteredJoinAction(FilteredJoinCancelAction action, LocalActionHandler loopback);
    
    /**
     * Cancels a request.
     * 
     * @param requestId
     */
    public boolean cancelFilteredJoinAction(Long requestId);
	
	/**
	 * Send out a join event for this request.
	 * 
	 * @param request
	 */
	public void sendJoin(FilteredRequest request, int tableId, int seq);
	
	/**
	 * Report a failed seating.
	 * The method signature is TBD
	 * @param request
	 */
	public void returnFilteredJoinRequest(long requestId);
	
	/**
	 * Consumes a request. 
	 * The request will be removed from the waiting list cache.
	 * 
	 * It is important that this method is called for every successful
	 * request so that the request match does not grow infinitely.
	 * 
	 * @param requestId
	 */
	public void consumeFilteredJoinRequest(long requestId);
	
}
