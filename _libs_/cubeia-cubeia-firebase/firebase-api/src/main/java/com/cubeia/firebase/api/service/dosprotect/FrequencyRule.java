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
package com.cubeia.firebase.api.service.dosprotect;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This rule checks for "x accesses during y timespan". 
 * 
 * @author Larsan
 */
public class FrequencyRule implements Rule {

	private final int accesses;
	private final long millis;

	private final Map<Object, Caller> map = new ConcurrentHashMap<Object, Caller>();
	
	/**
	 * @param accesses Number of accesses within the timespan
	 * @param millis Timespan in millis
	 */
	public FrequencyRule(int accesses, long millis) {
		this.accesses = accesses;
		this.millis = millis;
	}
	
	
	// --- RULE --- //
	
	public boolean allow(Object callerId, RuleChain chain) {
		Caller c = map.get(callerId);
		if (c == null) {
			c = createCaller(callerId);
		}

		if (c.allow()) {
			return chain.next(callerId);
		} else {
			return false;
		}
	}

	public void cleanup() {
		for (Iterator<Caller> it = map.values().iterator(); it.hasNext();) {
			Caller c = it.next();
			if(c.cleanup() == 0) {
				it.remove();
			}
		}
	}
	
	
	// ---- TEST METHODS --- //
	
	int size() {
		return map.size();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private synchronized Caller createCaller(Object callerId) {
		if (map.containsKey(callerId)) {
			return map.get(callerId);
		} else {
			Caller c = new Caller();
			map.put(callerId, c);
			return c;
		}
	}
	
	
	// --- INNER CLASSES --- //
	
	private class Caller {
		
		private final LinkedList<Long> entries = new LinkedList<Long>();
		
		public synchronized boolean allow() {
			Long now = Long.valueOf(System.currentTimeMillis());
			purgeStaleEntries(now);
			return check(now);
		}
		
		public synchronized int cleanup() {
			Long now = Long.valueOf(System.currentTimeMillis());
			purgeStaleEntries(now);
			return entries.size();
		}

		private boolean check(Long now) {
			if (entries.size() < accesses) {
				entries.add(now);
				return true;
			} else {
				return false;
			}
		}

		private void purgeStaleEntries(Long now) {
			for (Iterator<Long> it = entries.iterator(); it.hasNext();) {
				Long l = it.next();
				if (l.longValue() + millis <= now) {
					it.remove();
				}
			}
		}
	}
}
