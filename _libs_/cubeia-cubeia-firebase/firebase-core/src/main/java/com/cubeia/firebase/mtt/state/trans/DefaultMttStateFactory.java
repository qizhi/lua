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
package com.cubeia.firebase.mtt.state.trans;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.mtt.state.MttStateData;
import com.cubeia.firebase.mtt.state.MttStateFactory;
import com.cubeia.firebase.transaction.CoreTransactionManager;

public class DefaultMttStateFactory implements MttStateFactory {
	
	private final static AtomicLong VERSION_COUNTER = new AtomicLong();
	
	
	// --- INSTANCE MEMBERS --- //
	
	private final Map<Integer, ClassLoader> mttLoaders;
	private final CoreTransactionManager manager;
	private final long mttWarnSize;
	
	public DefaultMttStateFactory(CoreTransactionManager manager, long mttWarnSize) { 
		this.mttWarnSize = mttWarnSize;
		mttLoaders = new ConcurrentHashMap<Integer, ClassLoader>();
		this.manager = manager;
	}
	
	public MttStateData createMttState(int mttLogicId, int mttId) {
		long version = VERSION_COUNTER.incrementAndGet();
		return new VersionedStateData(mttLogicId, mttId, version);
	}

	public TransactionalMttState createMttState(MttStateData orgState) {
		Arguments.notNull(orgState, "orgState");
		VersionedStateData versioned = (VersionedStateData)orgState;
		checkStateLock(versioned);
		return new TransactionalMttStateImpl(versioned, this, manager);
	}
	
	public MttStateData extractState(TransactionalMttState orgState) {
		Arguments.notNull(orgState, "orgState");
		TransactionalMttStateImpl impl = (TransactionalMttStateImpl)orgState;
		return impl.getReadData();
	}

	public void setMttClassLoader(int mttId, ClassLoader load) {
		if(load != null) this.mttLoaders.put(mttId, load);
		else this.mttLoaders.remove(mttId);
	}
	
	
	// --- TRANSACTION CALLBACKS --- //
	
	/*
	 * Verify version number.
	 */
	void verify(TransactionalMttStateImpl table) { }
	
	/*
	 * Verify version and clean up.
	 */
	void cancel(TransactionalMttStateImpl table) { }
	
	/*
	 * Clean up.
	 */
	void committed(TransactionalMttStateImpl table) { }
	
	/*
	 * Get a mtt class loader, or null if not found
	 */
	public ClassLoader getMttClassLoader(int mttId) {
		return mttLoaders.get(mttId);
	}
	
	long getObjectWarnSize() {
		return mttWarnSize;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	/*
	 * Check that this particular table has a read/write
	 * lock we can use.
	 */
	private void checkStateLock(VersionedStateData data) {
		synchronized(data) {
			Lock lock = data.getTransientLock();
			if(lock == null) {
				data.setTransientLock(new ReentrantLock());
			}
		}
	}
}
