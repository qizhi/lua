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
package com.cubeia.firebase.api.util;

/**
 * A very simple range class.
 * 
 * @author lars.j.nilsson
 */
public final class Range<T> implements Cloneable {

	private final T from, to;

	
	/**
	 * @param from From object, may be null
	 * @param to To object, may be null
	 */
	public Range(final T from, final T to) {
		this.from = from;
		this.to = to;
	}
	
	/**
	 * @return The from object, may be null
	 */
	public T getFrom() {
		return from;
	}
	
	
	/**
	 * @return the to object, may be null
	 */
	public T getTo() {
		return to;
	}
	
	
	// --- OBJECT METHODS --- //

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object clone() {
		return new Range(from, to);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Range<?>)) return false;
		Range<?> r = (Range<?>)obj;
		return equals(r.from, from) && equals(r.to, to);
	}

	@Override
	public int hashCode() {
		return hash(to) ^ hash(from);
	}

	@Override
	public String toString() {
		return "date range; from=" + from + "; to=" + to;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private boolean equals(Object d, Object e) {
		if(d == null && e == null) return true;
		else if(d == null || e == null) return false;
		else return d.equals(e);
	}
	
	private int hash(T d) {
		return (d == null ? 7 : d.hashCode());
	}
}
