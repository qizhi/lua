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
 * This class is a immutable final class for property keys. It consists
 * of a namespace and a property name.
 * 
 * @author Larsan
 */
public final class PropertyKey implements Serializable {

	private static final long serialVersionUID = 8327162969552153191L;

	private final Namespace ns;
	private final String property;
	
	/**
	 * @param ns Namespace, if null replaced with {@link Namespace#NULL}
	 * @param prop Property string name, must not be null
	 */
	public PropertyKey(Namespace ns, String prop) {
		Arguments.notNull(prop, "property");
		if(ns == null) this.ns = Namespace.NULL;
		else this.ns = ns;
		this.property = prop;
	}
	
	/**
	 * @return The property namespace, never null
	 */
	public Namespace getNamespace() {
		return ns;
	}
	
	/**
	 * @return The property string name, never null
	 */
	public String getProperty() {
		return property;
	}
	
	
	// --- OBJECT METHODS --- //
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PropertyKey)) return false;
		return hashCode() == obj.hashCode();
	}
	
	@Override
	public int hashCode() {
		int pre = ns.hashCode();
		if(pre == 0) return property.hashCode();
		else return pre ^ property.hashCode();
	}
	
	@Override
	public String toString() {
		return ns.toString() + ":" + property;
	}
}
