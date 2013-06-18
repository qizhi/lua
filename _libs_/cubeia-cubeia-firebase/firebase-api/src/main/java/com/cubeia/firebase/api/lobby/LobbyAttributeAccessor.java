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
 * <p>This interface exposes the attributes of a table whose Lobby data 
 * is handled by Firebase to the Games.</p>
 * 
 * <p>It is recommended that you use the batch method if you are updating
 * more then one attribute.</p>
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 28
 */
public interface LobbyAttributeAccessor {

	/**
	 * @param attribute Attribute name, must not be null
	 */
	public void removeAttribute(String attribute);

	/**
	 * @param attribute Attribute name, must not be null
	 * @param value Attribute value, must not be null
	 */
	public void setAttribute(String attribute, AttributeValue value);
	
	/**
	 * @param attribute Attribute name, must not be null
	 * @param value Attribute value
	 */
	public void setIntAttribute(String attribute, int value);
	
	
	/**
	 * @param attribute Attribute name, must not be null
	 * @param value Attribute value, may be null
	 */
	public void setStringAttribute(String attribute, String value);
	
	
	/**
	 * @param attribute Attribute name, must not be null
	 * @param value Attribute value, may be null
	 */
	public void setDateAttribute(String attribute, Date value);

	
	/**
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or null if not found
	 */
	public AttributeValue getAttribute(String attribute);
	
	
	/**
	 * @return A defensive copy of the node attributes, never null
	 */
	public Map<String, AttributeValue> getAllAttributes();
	
	
	/**
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or null if not found
	 * @throws ClassCastException If the attribute is not a string
	 */
	public String getStringAttribute(String attribute) throws ClassCastException;
	
	
	/**
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or -1 if not found
	 * @throws ClassCastException If the attribute is not an int
	 */
	public int getIntAttribute(String attribute) throws ClassCastException;
	
	
	/**
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or null if not found
	 * @throws ClassCastException If the attribute is not a date
	 */
	public Date getDateAttribute(String attribute) throws ClassCastException;
	
	/**
	 * Batch update for the supplied attributes. This method of updating the
	 * lobby is recommended if you are updating more then one attribute
	 * for performance reasons.
	 * 
	 * @param attributes Attributes to set, must not be null
	 */
	public void setAttributes(Map<String, AttributeValue> attributes);
}
