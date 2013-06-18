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

import java.util.Date;
import java.util.Map;

import com.cubeia.firebase.api.common.AttributeValue;

/**
 * This interface is a simplified accessor for games to manipulate
 * the lobby data. Unlike the system state service contract this accessor
 * mandates a root path for all object data, for example: "/table/&lt;gameId&gt;/", 
 * which enforces separation between games. It also enforces data type control.
 * 
 * @author lars.j.nilsson
 */
public interface LobbyAccessor {
	
	/**
	 * @param path System state node path, must not be null
	 * @return True if a node exists for the path, false otherwise
	 */
	public boolean hasNode(LobbyPath path);
	
	/**
	 * This method removes all attributes from a given
	 * node but does ot remove the node itself.
	 * 
	 * @param path System state node path, must not be null
	 */
	public void clearNode(LobbyPath path);
	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 */
	public void removeNode(LobbyPath path);
	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 */
	public void removeAttribute(LobbyPath path, String attribute);

	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 * @param value Attribute value, must not be null
	 */
	public void setAttribute(LobbyPath path, String attribute, AttributeValue value);
	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 * @param value Attribute value
	 */
	public void setIntAttribute(LobbyPath path, String attribute, int value);
	
	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 * @param value Attribute value, may be null
	 */
	public void setStringAttribute(LobbyPath path, String attribute, String value);
	
	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 * @param value Attribute value, may be null
	 */
	public void setDateAttribute(LobbyPath path, String attribute, Date value);

	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or null if not found
	 */
	public AttributeValue getAttribute(LobbyPath path, String attribute);
	
	
	/**
	 * @param path System state node path, must not be null
	 * @return A defensive copy of the node attributes, never null
	 */
	public Map<String, AttributeValue> getAllAttributes(LobbyPath path);
	
	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or null if not found
	 * @throws ClassCastException If the attribute is not a string
	 */
	public String getStringAttribute(LobbyPath path, String attribute) throws ClassCastException;
	
	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or -1 if not found
	 * @throws ClassCastException If the attribute is not an int
	 */
	public int getIntAttribute(LobbyPath path, String attribute) throws ClassCastException;
	
	
	/**
	 * @param path System state node path, must not be null
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or -1 if not found
	 * @throws ClassCastException If the attribute is not a date
	 */
	public Date getDateAttribute(LobbyPath path, String attribute) throws ClassCastException;

}
