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
 * Configuration interfaces can be mapped to namespaces. Namespaces are string
 * tokens separated by dots. They are resolved from left to right. They exhibit 
 * relations, ie. namespace  "com.company" is a <em>child</em> namespace to "com", 
 * "com.busted" is a <em>parent</em> namespace to "com.busted.hard" etc. 
 * 
 * <p>The {@link #NULL NULL} namespace is set as a static member on this class. Its
 * is represented in string form as an empty string.
 * 
 * @author lars.j.nilsson
 */
public final class Namespace implements Serializable {

	private static final long serialVersionUID = -7581259418842754126L;

	/**
	 * Null namespace, represented as an empty string.
	 */
	public static final Namespace NULL = new Namespace("");
	
	
	/// --- INSTANCE MEMBERS --- ///
	
	private final String ns;
	
	
	/**
	 * @param ns Namspace in string form, must not be null
	 */
	public Namespace(String ns) {
		Arguments.notNull(ns, "ns");
		this.ns = ns;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Namespace)) return false;
		return ((Namespace)o).ns.equals(ns);
	}
	
	@Override
	public int hashCode() {
		return (ns.length() == 0 ? 5 : ns.hashCode());
	}
	
	@Override
	public String toString() {
		return ns;
	}

	/**
	 * This method attempts to resolve the parent namespace of this
	 * namespace. It will return {@link #NULL NULL} if no parent can 
	 * be found, and &lt;null&gt; if this is @link #NULL NULL}.
	 * 
	 * @return The parent namespace, {@link #NULL null} for the root namespace, and null if no parent exist
	 */
	public Namespace getParent() {
		if(ns.length() == 0) return null;
		int i = ns.lastIndexOf('.');
		if(i == -1) return Namespace.NULL;
		else {
			String next = ns.substring(0, i);
			return new Namespace(next);
		}
	}
}
