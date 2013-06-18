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

import java.io.Serializable;
import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;

/**
 * This interface acts as an accessor for an objects information in 
 * the lobby. It has the object id, and its properties available.
 * 
 * @author lars.j.nilsson
 */
public interface LobbyObject extends Serializable {

	/**
	 * @return The object id
	 */
	public int getObjectId();
	
	/**
	 * @return The object attributes, never null
	 */
	public Map<String, AttributeValue> getAttributes();
	
}
