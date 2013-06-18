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
package com.cubeia.firebase.api.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Attribute implements Serializable {
	
	public static Attribute fromProtocolAttribute(com.cubeia.firebase.io.protocol.Attribute att) {
		return (att == null ? null : new Attribute(att.name, att.value));
	}
	
	public static List<Attribute> fromProtocolAttributes(List<com.cubeia.firebase.io.protocol.Attribute> atts) {
		if(atts == null) return Collections.emptyList();
		List<Attribute> list = new ArrayList<Attribute>(atts.size());
		for (com.cubeia.firebase.io.protocol.Attribute a : atts) {
			list.add(fromProtocolAttribute(a));
		}
		return list;
	}
	
	public static com.cubeia.firebase.io.protocol.Attribute fromAttributeToProtocol(Attribute att) {
		return (att == null ? null : new com.cubeia.firebase.io.protocol.Attribute(att.name, att.value));
	}
	
	public static List<com.cubeia.firebase.io.protocol.Attribute> fromAttributesToProtocol(List<Attribute> atts) {
		if(atts == null) return Collections.emptyList();
		List<com.cubeia.firebase.io.protocol.Attribute> list = new ArrayList<com.cubeia.firebase.io.protocol.Attribute>(atts.size());
		for (Attribute a : atts) {
			list.add(fromAttributeToProtocol(a));
		}
		return list;
	}

	private static final long serialVersionUID = 664144121997510877L;
	
	private final String name;
	private final String value;
	
	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attribute other = (Attribute) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Attribute [name=" + name + ", value=" + value + "]";
	}
}
