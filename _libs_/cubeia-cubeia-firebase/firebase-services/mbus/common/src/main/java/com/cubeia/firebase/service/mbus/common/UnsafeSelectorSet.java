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
package com.cubeia.firebase.service.mbus.common;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class UnsafeSelectorSet implements SelectorSet {

	private final Set<Integer> set = new TreeSet<Integer>();
	
	@Override
	public String toString() {
		return "IntSet [size=" + set.size() + "; toString=" + set.toString() + "]";
	}
	
	public void add(int i) {
		this.set.add(i);
	}
	
	public void add(Set<Integer> set) {
		this.set.addAll(set);
	}
	
	public void remove(int i) {
		set.remove(i);
	}
	
	public boolean contains(int i) {
		return set.contains(i);
	}
	
	public int size() {
		return set.size();
	}
	
	public Set<Integer> get(boolean clear) {
		Set<Integer> tmp = new HashSet<Integer>(set);
		if(clear) set.clear();
		return tmp;
	}
	
	/**
	 * <b>Not Supported</b>
	 */
	public Set<Integer> getWait(boolean clear) throws InterruptedException {
		throw new UnsupportedOperationException();
	}
}
