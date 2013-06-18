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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cubeia.firebase.util.BalanceCalculator;

import junit.framework.TestCase;

public class TestBalanceCalculator extends TestCase {

	public void testDistributeOne() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		List<Set<String>> list = new ArrayList<Set<String>>();
		list.add(getStringSet("kalle", "olle"));
		c.distribute(list, getStringSet("adam", "håkan"));
		assertEquals(4, list.get(0).size());
	}
	
	public void testDistributeTwo() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		List<Set<String>> list = new ArrayList<Set<String>>();
		list.add(getStringSet("kalle", "olle", "bertil"));
		list.add(getStringSet("adam", "håkan"));
		c.distribute(list, getStringSet("laban"));
		assertEquals(3, list.get(0).size());
		assertEquals(3, list.get(1).size());
	}
	
	public void testDistributeThree() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		List<Set<String>> list = new ArrayList<Set<String>>();
		list.add(getStringSet("kalle", "olle", "bertil", "agnes", "eva"));
		list.add(getStringSet("adam", "håkan"));
		c.distribute(list, getStringSet("laban"));
		assertEquals(5, list.get(0).size());
		assertEquals(3, list.get(1).size());
	}
	
	public void testDistributeFour() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		List<Set<String>> list = new ArrayList<Set<String>>();
		list.add(getStringSet("kalle", "olle", "bertil"));
		list.add(getStringSet("adam", "håkan"));
		c.distribute(list, getStringSet("laban", "dilbert", "karin"));
		assertEquals(4, list.get(0).size());
		assertEquals(4, list.get(1).size());
	}
	
	public void testDistributeFive() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		List<Set<String>> list = new ArrayList<Set<String>>();
		list.add(getStringSet("kalle", "olle", "bertil"));
		list.add(getStringSet("adam", "håkan", "ulla"));
		c.distribute(list, getStringSet("laban", "dilbert", "karin"));
		assertEquals(5, list.get(0).size());
		assertEquals(4, list.get(1).size());
	}
	
	public void testDistributeSix() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		List<Set<String>> list = new ArrayList<Set<String>>();
		list.add(getStringSet("kalle"));
		list.add(getStringSet("adam", "håkan", "ulla"));
		c.distribute(list, getStringSet("laban", "dilbert", "karin"));
		assertEquals(4, list.get(0).size());
		assertEquals(3, list.get(1).size());
	}
	
	public void testBalanceOne() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		Collection<String> list = getStringList(c, 10);
		Collection<Set<String>> parts = c.balance(list, 4);
		assertParts(parts, 4, 3, 2);
	}
	
	public void testBalanceTwo() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		Collection<String> list = getStringList(c, 48);
		Collection<Set<String>> parts = c.balance(list, 4);
		assertParts(parts, 4, 12, 12);
	}
	
	public void testBalanceThree() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		Collection<String> list = getStringList(c, 5);
		Collection<Set<String>> parts = c.balance(list, 6);
		assertParts(parts, 6, 1, 0);
	}
	
	public void testBalanceFour() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		Collection<String> list = getStringList(c, 10);
		Collection<Set<String>> parts = c.balance(list, 5);
		assertParts(parts, 5, 2, 2);
	}
	
	public void testBalanceFive() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		Collection<String> list = getStringList(c, 0);
		Collection<Set<String>> parts = c.balance(list, 4);
		assertParts(parts, 4, 0, 0);
	}
	
	public void testBalanceSize() {
		BalanceCalculator<String> c = new BalanceCalculator<String>();
		Collection<String> list = getStringList(c, 10);
		Collection<Set<String>> parts = c.balance(list, 1);
		assertParts(parts, 1, 10, 10);
	}

	
	/// --- PRIVATE METHODS --- ///
	
	private Set<String> getStringSet(String...vals) {
		Set<String> set = new HashSet<String>();
		for (String s : vals) {
			set.add(s);
		}
		return set;
	}

	private void assertParts(Collection<Set<String>> parts, int size, int high, int low) {
		assertEquals(size, parts.size());
		for (Set<String> set : parts) {
			int test = set.size();
			if(test > high || test < low) {
				fail("partition size " + test + " outside bounds " + low + ":" + high);
			}
		}
	}

	private Collection<String> getStringList(BalanceCalculator<String> c, int len) {
		Collection<String> answer = new ArrayList<String>(len);
		for(int i = 0; i < len; i++) {
			answer.add("string-" + i);
		}
		return answer;
	}
}
