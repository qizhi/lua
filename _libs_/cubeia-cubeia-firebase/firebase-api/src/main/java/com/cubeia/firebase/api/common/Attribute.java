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
package com.cubeia.firebase.api.common;

import java.io.Serializable;

import com.cubeia.firebase.api.util.Arguments;

/**
 * Small class that encapsulates an attribute name and 
 * value. The name is a string, and both members can be accessed
 * as public variables of this class.
 * 
 * @author Larsan
 */
public final class Attribute implements Serializable {

	private static final long serialVersionUID = -1588300659740966613L;
	
	public final String name;
	public final AttributeValue value;

	
	/**
	 * @param name Attribute name, must not be null
	 * @param value Attribute value, must not be null
	 */
	public Attribute(String name, AttributeValue value) {
		Arguments.notNull(name, "name");
		Arguments.notNull(value, "value");
		this.value = value;
		this.name = name;
	}
}
