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
package com.cubeia.firebase.api.syslog;

import java.util.Date;

import com.cubeia.firebase.api.util.Range;

/**
 * This interface is used to access the system log events. The system
 * may keep a rotating buffer of events in memory.
 * 
 * @author lars.j.nilsson
 */
public interface LogEventAccess {

	/**
	 * @return The internal buffer size, or -1 if the buffer is unbounded
	 */
	public int getBufferSize();

	
	/**
	 * @param types Types to return events, or null for all
	 * @param range Date range for returned events, mey be null
	 * @param limit Length range or returned events, may be null
	 * @return The system events found, never null
	 */
	public SystemEvent[] query(EventType[] types, Range<Date> range, Range<Integer> limit);
	
	
	/**
	 * @param limit Length range to return, null for all
	 * @return The system events found, never null
	 */
	public SystemEvent[] getEvents(Range<Integer> limit);
	
}
