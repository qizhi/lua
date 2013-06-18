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
package com.cubeia.firebase.api.util;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * This simple utility executes a task one or more times. The
 * task returns a boolean for each execution, if that boolean
 * is "false" the repeat will continue, should the task return
 * "true" the repeater ends and returns. 
 * 
 * <p>This object is thread safe and can be used concurrently.
 * 
 * @author Lars J. Nilsson
 */
public class TaskRepeater {

	private final AtomicBoolean flag = new AtomicBoolean(true);
	private final Object lock = new Object();
	
	private final int repeats;
	private final long repeatDelay;

	private final String name;
	
	/**
	 * @param name Name of the task, used for logging
	 * @param repeats Max number of repeats to do
	 * @param repeatDelay Delay in milliseconds between repeats
	 */
	public TaskRepeater(String name, int repeats, long repeatDelay) {
		this.name = name;
		this.repeats = repeats;
		this.repeatDelay = repeatDelay;
	}
	
	/**
	 * Close this repeater, this will cause all invocations
	 * to "execute" to return immediately and subsequent calls
	 * to always return "false".
	 */
	public void close() {
		flag.set(false);
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
	/**
	 * This method does the same as {@link #execute(Callable)} with
	 * the exception that it catches and logs any errors. Should be used
	 * only when the task is known to be safe.
	 * 
	 * @param task Task to execute, must not be null
	 * @return True if the task is executed, false otherwise
	 */
	public boolean safeExecute(Callable<Boolean> task) {
		try {
			return execute(task);
		} catch (Exception e) {
			error("failed to execute task '" + name + "'", e);
			return false;
		}
	}
	
	/**
	 * Repeat the task until either the task returns "true",
	 * or until the configured timeout occurs. If the tasks
	 * has returned "true" this method also returns true, otherwise
	 * it returns false. 
	 * 
	 * @param task Task to execute, must not be null
	 * @return True if the task is executed, false otherwise
	 * @throws Exception If the task throws en exception
	 */
	public boolean execute(Callable<Boolean> task) throws Exception {
		boolean result = false;
		int count = 0;
		while(flag.get() && count < repeats) {
			result = task.call();
			if(result) {
				// DONE
				break;
			} else {
				// TRY AGAIN
				doRepeatDelay(count);
				count++;
			}
		}
		checkWarnFailure(result, count);
		return result;
	}
	
	
	// --- PROTECTED METHODS --- //
	
	/**
	 * Override to use something else than Log4J
	 */
	protected void debug(String msg) {
		Logger.getLogger(getClass()).debug(msg);
	}
	
	/**
	 * Override to use something else than Log4J
	 */
	protected void warn(String msg) {
		Logger.getLogger(getClass()).warn(msg);
	}
	
	/**
	 * Override to use something else than Log4J
	 */
	protected void error(String msg, Exception e) {
		Logger.getLogger(getClass()).error(msg, e);
	}

	
	// --- PRIVATE METHODS --- //
	
	private void checkWarnFailure(boolean result, int count) {
		if(count >= repeats && !result) {
			warn("Task '" + name + "' timed out after " + repeats + " attempts; Returning false");
		}
	}
	
	private void doRepeatDelay(int count) {
		debug("Task '" + name + "' failed, repeating in " + repeatDelay + " millis; Count: " + count);
		synchronized (lock) {
			try {
				lock.wait(repeatDelay);
			} catch (InterruptedException e) { }
		}
	}
}
