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
package com.cubeia.firebase.api.service.sysstate;

import java.util.Map;
import java.util.Set;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.service.Contract;

/**
 * This is the system state service which can be used by games to
 * manipulate the cluster wide shared memory.
 * 
 * @author lars.j.nilsson
 */
public interface PublicSystemStateService extends Contract {
	
	/**
	 * @param nodePath Path to check, must not be null
	 * @return True if the node exists, false otherwise
	 */
	public boolean exists(String nodePath);
	
	
	/**
	 * @param nodePath Path of node to remove, must not be null
	 * @return True if the node existed and was removed, false otherwise
	 */
	public boolean remove(String nodePath);

	
	/**
	 * @param nodePath Path of node to set attribute on, must not be null
	 * @param attribute Attribute name, must not be null
	 * @param value The value to set, must not be null
	 */
	public void setAttribute(String nodePath, String attribute, AttributeValue value);
	
	
	/**
	 * @param nodePath Path of node to get attribute from, must not be null
	 * @param attribute Attribute name, must not be null
	 * @return The attribute value, or null if not found
	 */
	public AttributeValue getAttribute(String nodePath, String attribute);
	
	
	/**
	 * @param nodePath Path of node to remove attribute from, must not be null
	 * @param attribute Attribute name, must not be null
	 * @return The old attribute value, or null if not found
	 */
	public AttributeValue removeAttribute(String nodePath, String attribute);
	
	
	/**
	 * @param nodePath Path of node to get all attributes from, must not be null
	 * @return A defensive copy of all node attributes, never null
	 */
	public Map<String, AttributeValue> getAttributes(String nodePath);
	
    /**
     * Gets the children of a node.
     * 
     * Example:
     * Cache:
     *   /a/b/c
     *   /a/b/d
     *   
     * Input address:
     *   /a
     *   
     * Result:
     *   b
     * @param fqn
     * @return
     */
    public Set<String> getChildren(String fqn);	

}
