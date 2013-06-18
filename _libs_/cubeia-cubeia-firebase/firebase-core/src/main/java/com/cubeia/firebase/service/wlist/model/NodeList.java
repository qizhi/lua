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

/**
 * Holds all requests for a node in the lobby tree.
 *
 * @author Fredrik
 */
public interface NodeList {
	
	/**
	 * Add a request to the end of line.
	 * 
	 * @param action
	 */
	public void addRequest(FilteredRequest request);
	
	/**
	 * Return a request to its proper position.
	 * 
	 * @param request
	 * @return True if the request is at the head of any part of the list
	 */
	public boolean returnRequest(FilteredRequest request);
	
	public void removeRequest(FilteredRequest request);
	
	/**
	 * Get the final match for the given data.
	 * 
	 * Key is the changed attribute. Send in an empty string, "", if we should
	 * check all attribute (or default queue depending on implementation). 
	 * 
	 * Returns null if no match was found.
	 * 
	 * @param key, the changed attribute.
	 * @param attribute
	 * @param value
	 * @return
	 */
	public FilteredRequest getMatch(String attribute, Map<?, ?> data);
	
	
	public void dumpToLog();
	
	public long getSize();
}
