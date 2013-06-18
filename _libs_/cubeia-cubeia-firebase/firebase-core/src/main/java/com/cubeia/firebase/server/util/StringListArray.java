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
package com.cubeia.firebase.server.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.StringList;

/**
 * A simple string list based on an array.
 * 
 * @author Larsan
 */
public final class StringListArray implements StringList {

	private final String[] arr;

	/**
	 * @param arr String list, must not be null
	 */
	public StringListArray(String[] arr) {
		Arguments.notNull(arr, "array");
		this.arr = arr;
	}
	
	public Iterator<String> iterator() {
		return Arrays.asList(arr).iterator();
	}

	public int size() {
		return arr.length;
	}

	public String get(int index) {
		if(index < 0 || index >= arr.length) throw new NoSuchElementException();
		return arr[index];
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			b.append(arr[i]);
			if(i + 1 < arr.length) {
				b.append(",");
			}
		}
		return b.toString();
	}
}
