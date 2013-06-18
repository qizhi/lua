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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.jgroups.ChannelException;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.space.ExtendedSpace;
import com.cubeia.space.SpaceConfig;

public class AbstractLockSpace<E extends Identifiable> implements ExtendedSpace<E>, CommitTimeRecorder {

	private static final String JMX_NAME = "com.cubeia.firebase.service:type=tspace,name=LockControl";
	
	protected final LockMap locks;
	protected RedistributionMap<E> space;
	protected final MBusContract mbus;
	protected final SpaceConfig conf;
	
	protected final long lockTimout;
	
	protected final Logger log = Logger.getLogger(getClass());
	protected final CoreTransactionManager manager;
	protected final MBeanServer serv;
	protected final String name;
	
	private final AtomicReference<CommitTimeRecorder> timeRecorder = new AtomicReference<CommitTimeRecorder>();

	private final boolean isJtaEnabled;

	/**
	 * Subclasses must create the space themselves after calling this
	 * super-constructor, this classes will *not* help them do it.
	 */
	protected AbstractLockSpace(String name, SpaceConfig conf, EventType type, MBusContract mbus, MBeanServer serv, CoreTransactionManager manager) {
		this.name = name;
		this.conf = conf;
		this.serv = serv;
		this.isJtaEnabled = conf.isJtaEnabled();
		this.lockTimout = conf.getLockAquisitionTimeout();
		locks = new LockMap(type, manager);
		this.manager = manager;
		this.mbus = mbus;
	}
	
	@Override
	public void recordCommitTime(long millis) {
		CommitTimeRecorder next = timeRecorder.get();
		if(next != null) {
			next.recordCommitTime(millis);
		}
	}
	
	public void setCommitTimeRecorder(CommitTimeRecorder rec) {
		timeRecorder.set(rec);
	}

	public void add(E[] objects) {
		try {
            space.add(objects);
        } catch (Exception e) {
            String msg = "Error adding objects to cache";
            log.error(msg, e);
        } 
	};
	
	public void remove(int[] objects) {
		try {
            space.remove(objects);
        } catch (Exception e) {
            String msg = "Error removing objects to cache";
            log.error(msg, e);
        }  
	}
	
	public void add(E object) {
        try {
            space.set(object);
        } catch (Exception e) {
            String msg = "Error putting object to cache";
            log.error(msg, e);
        }    
	}

	public boolean exists(int objectid) {
		throw new UnsupportedOperationException();
	}

	public E peek(int id) {
		return space.get(id);
	}
	
	/*
	 * Ticket #558
	 */
	public boolean isLocked(E object) {
		return locks.isHeldByCurrentThread(object.getId());
	}

	public void put(E object) throws LockException {
		checkIsLocked(object.getId());
		try {
			space.set(object);
		} catch (Exception e) {
			String msg = "Error putting object to cache";
			log.error(msg, e);
		} finally {
			locks.unlock(object.getId());
		}
	}

	public void release(int id) {
		if(isLocked(id)) {
			locks.unlock(id);
		}
	}

	public boolean remove(int id) {
		try {
			space.remove(id);
		} catch (Exception e) {
			String msg = "Error removing from cache";
			log.error(msg, e);
		}
		return true;
	}

	public Collection<E> snapshot() {
		throw new UnsupportedOperationException();
	}
	
	public E take(int id) {
		return take(id, null);
	}

	public E take(int id, Object action) {
		try {
			if (locks.lock(id, lockTimout, action)) {
				E obj = space.get(id);
				if(obj != null) {
					checkTransaction(obj, id);
				}
				return obj;
			} else {
				log.error("Timeout or failure when waiting for write lock; Space name: " + space.getName() + "; Object: " + id);
				return null;
			}
		} catch (InterruptedException e) {
			log.warn("Thread interrupted unexpectedly", e);
			return null;
		}
	}
	
	public void attachInfo(int tableId, Object action) {
		locks.getLockControl().attach(tableId, action); 
	}
	
	public void reset() {
		space.reset();
	}
	
	public boolean existsLocal(int i) {
		return space.existsLocal(i);
	}
	
	public E peekLocal(int i) {
		return space.peekLocal(i);
	}

	public E buddyPeekLocal(int i) {
		return space.peekBuddy(i);
	}
	
	
	// --- LIFETIME METHODS --- //

	public void start() { 
		initJmx();
		locks.attach(mbus);
		try {
			space.connect();
		} catch (ChannelException e) {
			throw new IllegalStateException("Failed to connect space", e);
		}
	}

	public void stop() { 
		locks.dettach(mbus);
		space.disconnect();
		destroyJmx();
	}
	
	
	// --- PROTECTED METHODS --- //
	
	protected void destroyJmx() {
		try {
			String name = JMX_NAME + ",space=" + this.name;
			if(serv.isRegistered(new ObjectName(name))) {
				serv.unregisterMBean(new ObjectName(name));
			}
		} catch(Exception e) {
			log.error("Failed to bind to JMX", e);
		}	
	}
	
	protected void initJmx() {
		try {
			String name = JMX_NAME + ",space=" + this.name;
			if(!serv.isRegistered(new ObjectName(name))) {
				serv.registerMBean(locks.getLockControl(), new ObjectName(name));
			}
		} catch(Exception e) {
			log.error("Failed to bind to JMX", e);
		}	
	}
	
	/*protected List<Address> getMembers() {
		return space.getMembers();
	}*/
	
	/*protected Address getLocalAddress() {
		return space.getLocalAddress();
	}*/

	/*protected Address getBuddyAddress() {
		return space.getBuddyAddress();
	}*/
	
	/*protected String infoString() {
		return space.infoString();
	}*/
	
	
	
	// --- UNUSED METHODS --- //

	public void halt() { }

	public boolean isHalted() {
		return false;
	}

	public void resume() { }
	
	public boolean isFailOverEnabled() {
		return true;
	}

	public boolean isJtaEnabled() {
		return isJtaEnabled;
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	private void checkTransaction(E obj, int id) {
		CoreTransaction trans = manager.currentTransaction();
		if(trans != null) {
			trans.attach(new SpaceResource<E>(obj, id, this));
		}
	}
	
	private boolean isLocked(int id) {
		return locks.isHeldByCurrentThread(id);
	}
	
	private void checkIsLocked(int id) {
		if (!locks.isHeldByCurrentThread(id)) {
		    throw new LockException(id, "The current thread must hold a write lock (by calling take) when calling this method. Id = " + id);
		}
	}
	
	/**
	 * Getter for the internal lock map. This method is for test purposes only.
	 * @return the lock map
	 */
	protected LockMap getLocks() {
		return locks;
	}
}
