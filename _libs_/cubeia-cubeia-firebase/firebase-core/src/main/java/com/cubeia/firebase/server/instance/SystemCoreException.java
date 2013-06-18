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

import com.cubeia.firebase.api.server.SystemException;

/**
 * This exception pipes explicit exception messages straight to the 
 * system log. 
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 20
 */
public class SystemCoreException extends SystemException {

	private static final long serialVersionUID = -1975673490513734062L;

	public SystemCoreException() {
		super();
	}

	public SystemCoreException(String msg, Throwable cause) {
		super(msg, cause);
		syslog(msg);
	}

	public SystemCoreException(String msg) {
		super(msg);
		syslog(msg);
	}

	public SystemCoreException(Throwable cause) {
		super(cause);
	}

	
	// --- PRIVATE METHODS --- ///
	
	private void syslog(String msg) {
		SystemLogger.error(msg);
	}
}