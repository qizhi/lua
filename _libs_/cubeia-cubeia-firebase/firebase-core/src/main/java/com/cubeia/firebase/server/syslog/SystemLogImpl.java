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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.syslog.EventType;
import com.cubeia.firebase.api.syslog.LogEventAccess;
import com.cubeia.firebase.api.syslog.SystemEvent;
import com.cubeia.firebase.api.syslog.SystemLog;

/**
 * Simple bounded buffer implementation of a system log. Writes to
 * the log4j Logger "SYSLOG". Internal buffer size: 10000;
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 20
 */
public class SystemLogImpl implements SystemLog {

	private static final String SYSLOG_LOGGER = "SYSLOG";
	private static final int DEF_LEN = 10000;

	
	// --- INSTANCE MEMBERS --- //
	
	private final Logger log = Logger.getLogger(SYSLOG_LOGGER);
	private final BoundedEventBuffer buffer;
	
	public SystemLogImpl() {
		buffer = new BoundedEventBuffer(DEF_LEN);
	}
	
	public LogEventAccess access() {
		return buffer;
	}

	public void error(String msg) {
		if(msg != null) event(EventType.ERROR, msg);
	}

	public void info(String msg) {
		if(msg != null) event(EventType.INFO, msg);
	}

	public void warning(String msg) {
		if(msg != null) event(EventType.WARNING, msg);
	}
	
	
	// --- PRIVATE METHODS --- //

	private void event(EventType t, String msg) {
		SystemEvent event = new SystemEvent(msg, null, t);
		buffer.event(event);
		write(t, msg);
	}

	private void write(EventType t, String msg) {
		if(t == EventType.ERROR) log.error(msg);
		else if(t == EventType.WARNING) log.warn(msg);
		else log.info(msg);
	}
}
