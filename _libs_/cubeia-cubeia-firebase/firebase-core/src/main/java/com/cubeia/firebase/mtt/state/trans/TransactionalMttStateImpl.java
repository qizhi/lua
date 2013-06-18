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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.mtt.MTTState;
import com.cubeia.firebase.api.scheduler.Scheduler;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.firebase.mtt.scheduler.MttActionScheduler;
import com.cubeia.firebase.transaction.CoreResource;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.firebase.transaction.ResourceException;


public class TransactionalMttStateImpl implements TransactionalMttState {

	
	// --- INSTANCE MEMBERS --- //

	private final DefaultMttStateFactory factory;	
	private final VersionedStateData data;
	
	private MttSchedulerMapDeltaChange currentSchedule;
	
	private MttStateDeltaChange delta;
	private MttSchedulerImpl scheduler;
	
	private final List<DeltaChange> changes = new LinkedList<DeltaChange>();
	
	protected MttActionScheduler actionScheduler;
	private final CoreTransactionManager manager;

	TransactionalMttStateImpl(VersionedStateData data, DefaultMttStateFactory factory, CoreTransactionManager manager) {
		this.data = data;
		this.factory = factory;
		this.manager = manager;
		checkTransaction();
	}
	
	public int getId() {
		return data.getId();
	}

	public boolean begin(long timeout) throws InterruptedException {
		return tryAcquireLock(timeout);
	}
	
	public void setActionScheduler(MttActionScheduler actionScheduler) {
		this.actionScheduler = actionScheduler;
	}

	@Override
	public void commit() {
		commitData();
		commitSchedule();
		commitDelta();
		//commitTables();
	}

	@Override
	public void release() {
		tryReleaseLock();
	}

	@Override
	public void rollback() {
		rollbackData();
		rollbackSchedule();
		rollbackDelta();
		//rollbackTables();
	}

	public MTTState getMttState() {
		if(delta == null) {
			delta = new MttStateDeltaChange(data, getClassLoader(), factory.getObjectWarnSize());
		}
		return (MTTState)delta.getNext();
	}
	
	public void setMttState(MTTState state) {
		delta = new MttStateDeltaChange(data, state, getClassLoader(), factory.getObjectWarnSize());
	}

	public MttStateDeltaChange getDelta() {
		return delta;
	}
	
	public Scheduler<MttAction> getScheduler() {
		if(scheduler == null) {
			currentSchedule = new MttSchedulerMapDeltaChange(this.data, getClassLoader());
			scheduler = new MttSchedulerImpl(this);
		}
		return scheduler;
	}

	/*public MttTableMap getMttTables() {
		if(tableMap == null) {
			tableMap = new MttTableMapDeltaChange(data);
		}
		return tableMap;
	}*/
	
	
	
	
	// --- PACKAGE METHODS --- //
	
	MttActionScheduler getActionScheduler() {
		return actionScheduler;
	}
	
	List<DeltaChange> getDeltaChanges() {
		return changes;
	}
	
	VersionedStateData getReadData() {
		return data;
	}
	
	MttSchedulerMapDeltaChange getCurrentSchedule() {
		return currentSchedule;
	}
	
	
	
	// --- PRIVATE METHODS --- ///
	
	/*private void commitTables() {
		if(tableMap != null) {
			tableMap.commit();
		}
	}*/
	
	private void commitDelta() {
		for (DeltaChange change : changes) {
			change.commit();
		}
	}
	
	/*private void rollbackTables() {
		if(tableMap != null) {
			tableMap = null;
		}
	}*/
	
	private void rollbackDelta() {
		changes.clear();
	}
	
	private void rollbackSchedule() {
		if(currentSchedule != null) {
			currentSchedule.rollback();
		}
	}

	private void rollbackData() {
		if(delta != null) {
			delta.rollback();
		}
	}
	
	private void commitSchedule() {
		if(currentSchedule != null) {
			currentSchedule.commit();
		}
	}

	private void commitData() {
		if(delta != null) {
			delta.commit();
		}
	}
	
	private ClassLoader getClassLoader() {
		return factory.getMttClassLoader(data.getMttLogicId());
	}
	
	private boolean tryAcquireLock(long timeout) throws InterruptedException {
		return data.getTransientLock().tryLock(timeout, TimeUnit.MILLISECONDS);
	}

	private void tryReleaseLock() {
		data.getTransientLock().unlock();
	}	
	
	private void checkTransaction() {
		if(manager == null) return; // EARlY RETURN
		CoreTransaction trans = manager.currentTransaction();
		if(trans != null) {
			trans.attach(new MttResource());
		}
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class MttResource implements CoreResource {

		public void commit() throws ResourceException {
			TransactionalMttStateImpl.this.commit();
		}

		public void prepare() throws ResourceException { }

		public void rollback() {
			TransactionalMttStateImpl.this.rollback();
		}
	}
}
