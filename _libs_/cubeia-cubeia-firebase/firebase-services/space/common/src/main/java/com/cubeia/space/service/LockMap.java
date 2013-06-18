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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;
import com.cubeia.firebase.service.messagebus.util.MBusListenerAdapter;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;

/**
 * This is a map of locks for known objects which uses the mbus 
 * to create / destroy locks. Ie. it listens to the mbus, when new objects
 * emerges a lock is automatically created and when the mbus removes the 
 * object, the lock will be GC'ed as well.
 * 
 * @author Larsan
 */
public class LockMap extends MBusListenerAdapter {
	
	private static final boolean USE_FAIR_SYNC_LOCK = true;
	private static final long REMOVE_LOCK_TIMEOUT = 10000;
	
	private final Logger log = Logger.getLogger(getClass());
	private final Map<Integer, ReentrantLock> locks = new ConcurrentHashMap<Integer, ReentrantLock>();
	
	private final EventType type;
	private final CoreTransactionManager manager;
	private final LockControl control;

	/**
	 * @param t Map event type, ie. MTT or GAME, must not be null
	 */
	LockMap(EventType t, CoreTransactionManager manager) {
		this.control = new LockControl();
		this.type = t;
		this.manager = manager;
	}
	
	/**
	 * Attach this map to the mbus. This will add listeners and 
	 * setup locks for all relevant channels.
	 * 
	 * @param mbus MBus to attach to, must not be null
	 */
	public void attach(MBusContract mbus) {
		mbus.addMBusListener(this);
		PartitionMap map = mbus.getCurrentPartitionMap();
		for (Partition p : map.getAllPartitions(type)) {
			Channel[] chans = map.getChannelsForPartition(p);
			channelAdded(p, chans);
		}
	}
	
	/**
	 * This method removes all listeners from the mbus.
	 * 
	 * @param mbus MBus to detach from, must not be null
	 */
	public void dettach(MBusContract mbus) {
		mbus.removeMBusListener(this);
	}
	
	
	// --- MBUS LISTENER --- //
	
	@Override
	public void channelAdded(Partition part, Channel[] channels) {
		if(part.getType().equals(type)) {
			for (Channel ch : channels) {
				add(ch.getId());
			}
		}
	}
	
	@Override
	public void channelRemoved(Partition part, Channel[] channels) {
		if(part.getType().equals(type)) {
			for (Channel ch : channels) {
				remove(ch.getId());
			}
		}
	}
	
	
	// --- LOCKING METHODS --- //
	
	public boolean isHeldByCurrentThread(int id) {
		ReentrantLock lock = locks.get(id);
		return (lock == null ? false : lock.isHeldByCurrentThread());
	}
	
	public boolean lock(int id, long timeout, Object o) throws InterruptedException {
		ReentrantLock lock = locks.get(id);
		if(lock == null) {
			log.error("Attempt to lock non-existing object " + id);
			return false;
		} else {
			boolean ok = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
			if(ok) {
				control.locked(Thread.currentThread(), id, o);
				checkTransaction(lock, id);
			}
			return ok;
		}
	}

	public void unlock(int id) {
		ReentrantLock lock = locks.get(id);
		if(lock == null) {
			log.error("Attempt to unlock non-existing object " + id);
		} else {
			if(lock.isHeldByCurrentThread()) {
				control.unlocked(id);
				lock.unlock();
			} 
		}
	}
	
	
	// --- PACKAGE METHODS --- //
	
	LockControl getLockControl() {
		return control;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void checkTransaction(ReentrantLock lock, int id) {
		CoreTransaction trans = manager.currentTransaction();
		if(trans != null) {
			trans.attach(new LockResource(lock, id));
		}
	}
	
	private void add(int id) {
		if(!locks.containsKey(id)) {
			locks.put(id, new ReentrantLock(USE_FAIR_SYNC_LOCK));
		} else {
			log.warn("Attempt to recreate lock " + id);
		}
	}
	
	private void remove(int id) {
		ReentrantLock lock = locks.get(id);
		if(lock == null) {
			log.warn("Attempt to remove non-existing lock " + id);
		} else {
			boolean locked = false;
			try {
				locked = lock.tryLock(REMOVE_LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
				if(!locked) {
					log.warn("Failed to acquire lock " + id + " for removal");
				}
			} catch(InterruptedException e) { 
				// Do nothing
			} finally {
				control.unlocked(id);
				locks.remove(id);
				if(locked) {
					lock.unlock();
				}
			}	
		}
	}
}
