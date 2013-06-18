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
package com.cubeia.firebase.api.server.conf;

import java.io.Serializable;

import com.cubeia.firebase.api.util.Arguments;

/**
 * A config property is a combination of a property key and a string value.
 * It is a bean with constructor injection only.
 * 
 * @author Larsan
 */
public final class ConfigProperty implements Serializable {

	private static final long serialVersionUID = 5953288779662556293L;
	
	private final PropertyKey key;
	private final String value;
	
	/**
	 * @param key Property key, must not be null
	 * @param value Property value, must not be null
	 */
	public ConfigProperty(PropertyKey key, String value) {
		Arguments.notNull(key, "key");
		Arguments.notNull(value, "value");		
		this.value = value;
		this.key = key;
	}
	
	
	/**
	 * @return The property key, never null
	 */
	public PropertyKey getKey() {
		return key;
	}
	
	
	/**
	 * @return the property value, never null
	 */
	public String getValue() {
		return value;
	}
}
