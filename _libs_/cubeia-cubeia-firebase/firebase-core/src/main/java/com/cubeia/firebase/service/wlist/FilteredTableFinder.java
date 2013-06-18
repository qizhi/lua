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

import com.cubeia.firebase.service.wlist.model.FilteredRequest;

/**
 * Searches the lobby for the supplied filtered request.
 * If a match is found then we should return it.
 * If no match is found we return null.
 * 
 *
 * @author Fredrik
 */
public interface FilteredTableFinder {
	
	/**
	 * Return true if a table was found and the request was dispatched. 
	 * 
	 * @param request
	 * @return
	 */
	public boolean matchRequest(FilteredRequest request);
	
}
