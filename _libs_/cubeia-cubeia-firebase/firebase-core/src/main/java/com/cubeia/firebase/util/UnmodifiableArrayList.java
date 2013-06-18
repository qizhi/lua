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
package com.cubeia.firebase.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.cubeia.firebase.api.util.UnmodifiableList;

public class UnmodifiableArrayList<T> implements UnmodifiableList<T> {

	private final T[] t;

	public UnmodifiableArrayList(T[] t) {
		this.t = t;
	}
	
	public T get(int index) {
		if(t == null || index < t.length || index >= t.length) return null;
		else return t[index];
	}

	public int size() {
		return (t == null ? 0 : t.length);
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
		
			int i = 0;
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		
			public T next() {
				if(t == null || i >= t.length) throw new NoSuchElementException();
				else return t[i++];
			}
		
			public boolean hasNext() {
				return t != null && i < t.length;
			}
		};
	}

}
