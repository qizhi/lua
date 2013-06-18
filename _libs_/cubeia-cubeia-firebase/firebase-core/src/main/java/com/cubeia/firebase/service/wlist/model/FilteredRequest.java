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

import java.util.Map;

import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.defined.Parameter;
import com.cubeia.firebase.api.lobby.LobbyPath;

/**
 * Holds a filtered request in the waiting list.
 *
 * @author Fredrik
 */
public interface FilteredRequest extends Comparable<FilteredRequest> {

	public long getTimeStamp();
	
	public int getPlayerId();
	
	public LobbyPath getFqn();
	
	public Map<String, Parameter<?>> getParameters();
	
	/**
	 * Return true if the constrain for this attribute and this
	 * attribute only holds true.
	 * 
	 * If this method returns true, the match(Map) method will be 
	 * called to evaluate all attributes.
	 * 
	 * @param attribute
	 * @param value
	 * @return  
	 */
	public boolean preMatch(String attribute, Object value);
	
	/**
	 * Return true if the constraints for this requests
	 * matches the given set of attributes.
	 * 
	 * @param data
	 * @return
	 */
	public boolean match(Map<?, ?> data);
	
	public void setId(long id);
	public long getId();
	
	
	/**
	 * Direct loopback to the client.
	 * TODO: This is not the preferred way of communicating.
	 * using the loopback will result in a synchronous blocking call to send
	 * packet(s) to the client.
	 * 
	 * However, we are using the waitinglist locally, so we definately want 
	 * to avoid going to the router and broadcast the table available to 
	 * all gateway nodes, that would be a waste of internal bandwidth.
	 */
	public LocalActionHandler getLoopback();
	public void setLoopback(LocalActionHandler loopback);
	
}
