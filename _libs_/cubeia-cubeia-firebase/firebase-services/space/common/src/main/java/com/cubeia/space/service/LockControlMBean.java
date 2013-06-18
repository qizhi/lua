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
package com.cubeia.space.service;

/**
 * MBean for lock control and diagnostics. The "event recording" captures
 * stacks of calling threads and string versions of triggering events and actions.
 * It is turned of by default, but can be turned on with the "even-recording" property.
 * 
 * <p>You can enable recording from start by setting the following system property to
 * "true": 
 * 
 * <pre>
 * 		com.cubeia.space.locks.enableRecording
 * </pre>
 * 
 * @author larsan
 */
public interface LockControlMBean {

	/**
	 * @return True if the event recording is on, false other wise
	 */
	public boolean getEventRecording();
	
	/**
	 * @param on True to turn on event recording, false to turn it off
	 */
	public void setEventRecording(boolean on);
	
	/**
	 * @param id Object (lock) id
	 * @return The string version of triggering event, or null
	 */
	public String getRecordedEvent(int id);
	
	/**
	 * @param id Object (lock) id
	 * @return The string version of triggering action, or null
	 */
	public String getRecordedAction(int id);
	
	/**
	 * @param id Object (lock) id
	 * @return Thread stack trace of calling thread for lock, or null
	 */
	public String getRecordedEventTrace(int id);
	
	/**
	 * This method interrupts the thread which has the lock
	 * for a given objects. NB: Use with care!
	 * 
	 * @param id Object (lock) id
	 * @return True if the thread was interrupted
	 */
	public boolean interruptLockHolderThread(int id);
	
	/**
	 * @param id Object (lock) id
	 * @return True if the lock is held, false otherwise
	 */
	public boolean isLocked(int id);
	
	/**
	 * @param id Object (lock) id
	 * @return The time in millis the lock has been held, or -1
	 */
	public long getLockedTime(int id);
	
	/**
	 * @return The number of currently held locks
	 */
	public int getLockCount();
	
	/**
	 * @return All currently held locks, never null
	 */
	public int[] getLocks();
	
}
