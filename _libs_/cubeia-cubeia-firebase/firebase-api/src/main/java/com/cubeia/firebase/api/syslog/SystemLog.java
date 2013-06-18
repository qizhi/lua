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

/**
 * Firebase system log access interface. The system log is responsible for 
 * high level messages only and should not be used for ordinary logging.
 * 
 * <p>The log contains a buffer of messages kept in memory, when this buffer
 * is exceeded old events will be dropped. Normally the system log is also written 
 * to file and old dropped events can be retrieved from the file system.
 * 
 * @author lars.j.nilsson
 */
public interface SystemLog {

	/**
	 * @return The interface used for accessing the log, never null
	 */
	public LogEventAccess access();
	
	/**
	 * @param msg Error message to log, must not be null
	 */
	public void error(String msg);
	
	/**
	 * @param msg Info message to log, must not be null
	 */
	public void info(String msg);
	
	/**
	 * @param msg Warning message to log, must not be null
	 */
	public void warning(String msg);
	
}
