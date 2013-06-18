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

import com.cubeia.firebase.api.util.Arguments;

/**
 * A single system event as stored by the system log. The
 * event has a type, a date and a messages.
 * 
 * @author lars.j.nilsson
 */
public final class SystemEvent {

	private final String msg;
	private final Date date;
	private final EventType type;
	
	/**
	 * @param msg Event message, must not be null
	 * @param date Event timestamp, if null will be set to the current system time
	 * @param type Event type, must not be null
	 */
	public SystemEvent(final String msg, final Date date, final EventType type) {
		Arguments.notNull(msg, "message");
		Arguments.notNull(type, "event type");
		this.date = (date == null ? new Date() : date);
		this.msg = msg;
		this.type = type;
	}

	
	/**
	 * @return The event timestamp, never null
	 */
	public Date getDate() {
		return date;
	}

	
	/**
	 * @return The event message, never null
	 */
	public String getMessage() {
		return msg;
	}

	
	/**
	 * @return The event type, never null
	 */
	public EventType getType() {
		return type;
	}
	
	
	
	// --- OBJECT METHODS --- //
	
	@Override
	public String toString() {
		return "syslog event: date=" + date + "; type=" + type + "; msg=" + msg;
	}
}
