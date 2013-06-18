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
package com.cubeia.firebase.server.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import com.cubeia.firebase.util.FirebaseLockFactory;

/**
 * This is a wrapper lock which enforces lock order over two locks. Given a 
 * "master" lock and a local read/write lock, it makes sure to always lock the master 
 * before its local lock. The master lock will usually be shared between several 
 * second level locks.
 * 
 * @author Larsan
 * @date 2007 maj 28
 */
public final class SecondLevelLock implements ReadWriteLock {

	private final Lock master;
	private final ReadWriteLock lock;
	
	private final Lock readLock;
	private final Lock writeLock;
	
	/**
	 * @param master Master lock, must not be null
	 */
	public SecondLevelLock(Lock master) {
		lock = FirebaseLockFactory.createLock();
		this.master = master;
		readLock = new RLock();
		writeLock = new WLock();
	}
	
	
	// --- READ / WRITE LOCK --- //
	
	public Lock readLock() {
		return readLock;
	}

	public Lock writeLock() {
		return writeLock;
	}

	
	// --- INNER CLASSES --- //
	
	/**
	 * This class enforces order given two locks. The master
	 * lock will always be locked before the internal lock.
	 */
	private static class WrapLock implements Lock {
		
		private final Lock internal;
		private final Lock master;
		
		protected WrapLock(Lock master, Lock internal) {
			this.master = master;
			this.internal = internal;
		}

		public void lock() {
			master.lock();
			internal.lock();
		}

		public void lockInterruptibly() throws InterruptedException {
			master.lockInterruptibly();
			boolean b = false;
			try {
				internal.lockInterruptibly();
				b = true;
			} finally {
				if(!b) {
					master.unlock();
				}
			}
		}

		public Condition newCondition() {
			throw new UnsupportedOperationException();
		}

		public boolean tryLock() {
			if(master.tryLock()) {
				boolean b = false;
				try {
					b = internal.tryLock();
				} finally {
					if(!b) {
						master.unlock();
					}
				}
				return b;
			} else return false;
		}

		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			if(master.tryLock(time, unit)) {
				boolean b = false;
				try {
					b = internal.tryLock(time, unit);
				} finally {
					if(!b) {
						master.unlock();
					}
				}
				return b;
			} else return false;
		}

		public void unlock() {
			internal.unlock();
			master.unlock();
		}
		
	}

	
	/**
	 * Order-enforcing write lock.
	 */
	private final class WLock extends WrapLock {

		private WLock() {
			super(master, lock.writeLock());
		}
	}
	
	
	/**
	 * Order-enforcing read lock.
	 */
	private final class RLock extends WrapLock {

		private RLock() {
			super(master, lock.readLock());
		}
	}
}