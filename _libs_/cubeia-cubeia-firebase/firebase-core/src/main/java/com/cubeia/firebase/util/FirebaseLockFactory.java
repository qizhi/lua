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

import java.util.concurrent.locks.ReadWriteLock;

import com.cubeia.util.lock.ReadWriteLockFactory;

/**
 * This is the master read/write lock factory used by Firebase. It
 * uses fair locks by default (as per Trac issue 562), and may also bypass 
 * the issue described in Trac issue 581. To change these setting use, 
 * the boolean system properties:
 * 
 * <pre>
 * 		com.cubeia.firebase.lock.useFair
 * 		com.cubeia.firebase.lock.bypassTicket581
 * </pre>
 *
 * For example:
 * 
 * <pre>
 * 		-Dcom.cubeia.firebase.lock.useFair=false -Dcom.cubeia.firebase.lock.bypassTicket581=false
 * </pre>
 *
 * @author Lars J. Nilsson
 */
public class FirebaseLockFactory extends ReadWriteLockFactory {
	
	public static final boolean USE_FAIR = System.getProperty("com.cubeia.firebase.lock.useFair", "true").equals("true");
	public static final boolean BYPASS_581 = System.getProperty("com.cubeia.firebase.lock.bypassTicket581", "false").equals("true");
	
	private FirebaseLockFactory() {
		super(USE_FAIR, BYPASS_581);
	}

	public static ReadWriteLock createLock() {
		return new FirebaseLockFactory().newLock();
	}
	
	public static ReadWriteLock createLock(String jmxId) {
		return new FirebaseLockFactory().newLock(jmxId);
	}
}
