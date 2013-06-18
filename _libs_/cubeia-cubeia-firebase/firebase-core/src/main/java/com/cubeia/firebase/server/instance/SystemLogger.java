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
package com.cubeia.firebase.server.instance;

import java.util.concurrent.atomic.AtomicReference;

import com.cubeia.firebase.api.syslog.SystemLog;

public class SystemLogger {

	private static AtomicReference<SystemLog> LOG = new AtomicReference<SystemLog>(null);
	
	public static SystemLog getInstance() {
		return LOG.get();
	}
	
	public static void error(String msg) {
		SystemLog log = getInstance();
		if(msg != null && log != null) {
			log.error(msg);
		}
	}
	
	public static void info(String msg) {
		SystemLog log = getInstance();
		if(msg != null && log != null) {
			log.info(msg);
		}
	}
	
	public static void warning(String msg) {
		SystemLog log = getInstance();
		if(msg != null && log != null) {
			log.warning(msg);
		}
	}
	
	
	
	// --- SETTER --- //
	
	static void setInstance(SystemLog log) {
		LOG.set(log);
	}
}
