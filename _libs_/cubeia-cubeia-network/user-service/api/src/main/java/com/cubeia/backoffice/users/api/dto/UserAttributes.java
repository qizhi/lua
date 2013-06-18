/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.backoffice.users.api.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Key/value user attributes.
 * @author w
 */
@XmlRootElement(name="UserAttributes")
public class UserAttributes implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<String, String> attributes = new HashMap<String, String>();
	
	public UserAttributes() {}
	
	public UserAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Map of user attributes. String -> String.
	 * @return attribute map.
	 */
	@XmlElementWrapper(name = "attributes")
	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

}
