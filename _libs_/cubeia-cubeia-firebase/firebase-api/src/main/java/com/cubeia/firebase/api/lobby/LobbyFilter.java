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
package com.cubeia.firebase.api.lobby;

import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;

/**
 * A simple filter interface used to determine access to 
 * objects in the lobby.
 * 
 * @author lars.j.nilsson
 */
public interface LobbyFilter {

	/**
	 * @param object Attributes for table to check, never null
	 * @return True if the object should be included, false otherwise
	 */
	public boolean accept(Map<String, AttributeValue> object);

}
