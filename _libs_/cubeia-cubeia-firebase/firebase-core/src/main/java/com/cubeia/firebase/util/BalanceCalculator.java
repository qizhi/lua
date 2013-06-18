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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.cubeia.firebase.api.util.Arguments;

public class BalanceCalculator<T> {
	
	/**
	 * This method takes an existing set of partitions, and distributes the 
	 * remainder collection across the existing partitions. The algorithm
	 * simply takes from the remainder and adds one of the sets, while keeping a
	 * sorted order.
	 * 
	 * @param existing Existing partition list to distribute to, must not be null
	 * @param remainder Remainder, the collection to distribute over existing partitions
	 */
	public void distribute(List<Set<T>> existing, Collection<T> remainder) {
		Arguments.notNull(existing, "existing");
		if(remainder != null && remainder.size() > 0) {
			Iterator<T> it = remainder.iterator();
			List<Entry> sizes = toSizeList(existing);
			List<Entry> newSizes = calculateNewSizes(sizes, remainder.size());
			for (Entry e : newSizes) {
				Set<T> set = existing.get(e.id);
				int add = e.size - sizes.get(e.id).size;
				for (int i = 0; i < add; i++) {
					set.add(it.next());
				}
			}
		}
	}



	/**
	 * This method takes a collection of objects and splits them
	 * into a number of partitions. This method splits the objects 
	 * in linear order.
	 * 
	 * @param col Objects to balance, must not be null
	 * @param partitions Number of partitions
	 * @return An collection of partitions
	 */
	public List<Set<T>> balance(Collection<T> col, int partitions) {
		Arguments.notNull(col, "collection");
		if(partitions <= 0) return new LinkedList<Set<T>>();
		if(col.size() == 0) {
			return emptyPartitions(partitions);
		} else {
			List<Set<T>> answer = emptyPartitions(partitions);
			for(Iterator<T> it = col.iterator(); it.hasNext(); ) {
				Iterator<Set<T>> jt = answer.iterator();
				while(it.hasNext() && jt.hasNext()) {
					jt.next().add(it.next());
				}
			}
			return answer;
		}
	}
	
	
	/// --- PRIVATE METHODS --- ///
	
	/*
	 * Return map of new sizes
	 */
	private List<Entry> calculateNewSizes(List<Entry> sizes, int addSize) {
		List<Entry> next = cloneList(sizes);
		Collections.sort(next, new EComp());
		int index = 0;
		Entry e = (next.size() > 0 ? next.get(index) : null);
		while(e != null && addSize > 0) {
			e.size++;
			addSize--;
			index = (index + 1 < next.size() ? index + 1 : 0);
			Entry tmp = next.get(index);
			if(e.size >= tmp.size) {
				e = tmp;
			}
		}
		return next;
	}

	private List<Entry> cloneList(List<Entry> sizes) {
		List<Entry> next = new ArrayList<Entry>(sizes.size());
		for (Entry e : sizes) {
			next.add(new Entry(e));
		}
		return next;
	}

	/*
	 * Return list of entries (list index + size)
	 */
	private List<Entry> toSizeList(List<Set<T>> existing) {
		int i = 0;
		List<Entry> list = new ArrayList<Entry>(existing.size());
		for (Set<T> s : existing) {
			list.add(new Entry(i++, s.size()));
		}
		return list;
	}
	
	private List<Set<T>> emptyPartitions(int partitions) {
		List<Set<T>> name = new LinkedList<Set<T>>();
		for(int i = 0; i< partitions; i++) {
			name.add(new HashSet<T>());
		}
		return name;
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private static class EComp implements Comparator<Entry> {

		public int compare(Entry o1, Entry o2) {
			if(o1.size == o2.size) {
				return (o1.id > o2.id ? 1 : -1);
			} else if(o1.size > o2.size) {
				return 1;
			} else {
				return -1;
			}
		}
	}
	
	private static class Entry {
		
		private int id, size;
		
		private Entry(int id, int size) {
			this.size = size;
			this.id = id;
		}
		
		public Entry(Entry e) {
			this.size = e.size;
			this.id = e.id;
		}

		@Override
		public String toString() {
			return id + ":" + size;
		}
	}
}
