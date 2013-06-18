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
package com.cubeia.firebase.server.syslog;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import com.cubeia.firebase.api.syslog.EventType;
import com.cubeia.firebase.api.syslog.SystemEvent;
import com.cubeia.firebase.api.util.Range;
import com.cubeia.firebase.util.FirebaseLockFactory;

/*
 * Thread safe list implementation. Query methods are slow
 * using this class. /LJN
 */
public class BoundedEventBuffer implements InternalEventBuffer {

	private final int len;
	private final LinkedList<SystemEvent> events;
	private final ReadWriteLock lock;

	BoundedEventBuffer(int len) {
		lock = FirebaseLockFactory.createLock();
		events = new LinkedList<SystemEvent>();
		this.len = len;
	}
	
	public void event(SystemEvent event) {
		if(event == null) return; // SANITY CHECK
		lock.writeLock().lock();
		try {
			if(events.size() >= len) events.removeLast();
			events.addFirst(event);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public int getBufferSize() {
		return len;
	}

	public SystemEvent[] getEvents(Range<Integer> limit) {
		return query(null, null, limit);
	}

	public SystemEvent[] query(EventType[] types, Range<Date> range, Range<Integer> limit) {
		Set<EventType> set = getTypeSet(types);
		LinkedList<SystemEvent> list = new LinkedList<SystemEvent>();
		lock.readLock().lock();
		try {
			doSearchInto(set, range, limit, list);
		} finally {
			lock.readLock().unlock();
		}
		return list.toArray(new SystemEvent[list.size()]);
	}
	
	
	// --- PRIVATE METHODS --- //

	// lock before method
	private void doSearchInto(Set<EventType> set, Range<Date> range, Range<Integer> limit, LinkedList<SystemEvent> list) {
		for (int i = 0; i < events.size(); i++) {
			SystemEvent event = events.get(i);
			if(set.contains(event.getType())) {
				if(inDate(event.getDate(), range) && inLimit(i, limit)) {
					list.addFirst(event); // REVERSE ORDER
				} else if(killRange(event.getDate(), range) || killLimit(i, limit)) {
					break; // OUSIDE SEARCH SCOPE
				}
			}
		}
	}

	private boolean killLimit(int index, Range<Integer> limit) {
		if(limit == null) return false;
		Integer to = limit.getTo();
		return (to != null && index > to);
	}

	private boolean killRange(Date date, Range<Date> range) {
		if(range == null) return false;
		Date from = range.getFrom(); // use from as we're iterating "reversed"
		return (from != null && date.before(from));
	}

	private boolean inLimit(int index, Range<Integer> limit) {
		if(limit == null) return true;
		Integer from = limit.getFrom();
		if(from != null && index < from.intValue()) return false;
		Integer to = limit.getTo();
		if(to != null && index > to.intValue()) return false;
		return true;
	}

	private boolean inDate(Date date, Range<Date> range) {
		if(range == null) return true;
		Date from = range.getFrom();
		if(from != null && date.before(from)) return false;
		Date to = range.getTo();
		if(to != null && date.after(to)) return false;
		return true;
	}

	private Set<EventType> getTypeSet(EventType[] types) {
		Set<EventType> set = new HashSet<EventType>(3);
		if(types == null) types = getAllTypes();
		for (EventType type : types) {
			set.add(type);
		}
		return set;
	}

	private EventType[] getAllTypes() {
		return EventType.values();
	}
}
