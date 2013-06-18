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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class LockControl implements LockControlMBean {

	private final Logger log = Logger.getLogger(getClass());
	
	private final AtomicBoolean recording = new AtomicBoolean(System.getProperty("com.cubeia.space.locks.enableRecording", "false").equals("true"));
	private final ConcurrentHashMap<Integer, Holder> locks = new ConcurrentHashMap<Integer, Holder>();

	public boolean getEventRecording() {
		return recording.get();
	}

	public String getRecordedEvent(int id) {
		Holder holder = locks.get(id);
		if(holder != null) {
			return holder.objectCapture;
		} else {
			return null;
		}
	}
	
	public String getRecordedAction(int id) {
		Holder holder = locks.get(id);
		if(holder != null) {
			return holder.actionCapture;
		} else {
			return null;
		}
	}

	public String getRecordedEventTrace(int id) {
		Holder holder = locks.get(id);
		if(holder != null) {
			return holder.stackCapture;
		} else {
			return null;
		}
	}

	public boolean isLocked(int id) {
		return getLockedTime(id) != -1;
	}
	
	public int getLockCount() {
		return locks.size();
	}
	
	public int[] getLocks() {
		int i = 0;
		List<Integer> l = new ArrayList<Integer>(locks.keySet());
		int[] arr = new int[l.size()];
		for (Integer id : l) {
			arr[i++] = id.intValue();
		}
		return arr;
	}
	
	public long getLockedTime(int id) {
		Holder holder = locks.get(id);
		if(holder != null) {
			return System.currentTimeMillis() - holder.startTime;
		} else {
			return -1;
		}
	}
	
	public boolean interruptLockHolderThread(int id) {
		Holder holder = locks.get(id);
		if(holder != null) {
			log.warn("Forcibly interrupting thread holding lock for id " + id);
			holder.thread.interrupt();
			return true;
		} else {
			return false;
		}
	}
 
	public void setEventRecording(boolean n) {
		recording.set(n);
	}
	
	
	// --- PACKAGE METHODS --- //
	
	void locked(Thread th, int id, Object event) {
		locks.put(id, new Holder(th, event));
	}
	
	void attach(int id, Object action) {
		Holder holder = locks.get(id);
		if(holder != null) {
			holder.attachAction(action);
		}
	}

	void unlocked(int id) {
		locks.remove(id);
	}
	
	
	// --- INTERNAL CLASSES --- //
	
	public class Holder {
		
		private final Thread thread;
		private final long startTime;
		private final String objectCapture;
		private final String stackCapture;
		private String actionCapture;
		
		private Holder(Thread thread, Object o) {
			this.startTime = System.currentTimeMillis();
			this.thread = thread;
			if(recording.get()) {
				this.objectCapture = getObject(o);
				this.stackCapture = getStack();
			} else {
				this.objectCapture = null;
				this.stackCapture = null;
			}
		}

		public void attachAction(Object action) {
			if(recording.get()) {
				this.actionCapture = (action == null ? null : action.toString());
			} 
		}

		private String getObject(Object o) {
			return (o == null ? null : o.toString());
		}

		private String getStack() {
			StringBuilder b = new StringBuilder();
			StackTraceElement[] trace = thread.getStackTrace();
			for (StackTraceElement e : trace) {
				b.append(e.toString()).append("\r\n");
			}
			return b.toString();
		}
	}
}
