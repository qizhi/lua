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
package com.cubeia.firebase.service.ctransaction.local;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.ctransaction.CoreManager;
import com.cubeia.firebase.transaction.ContextType;
import com.cubeia.firebase.transaction.CoreResource;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.ResourceException;
import com.cubeia.firebase.transaction.TransactionException;

/**
 * A {@link CoreManager} implementation that keeps core transactions in a
 * thread local. Resources attached to a transaction is handled in strict order
 * of appearance. 
 * 
 * @author Lars J. Nilsson
 */
public class LocalCoreManager implements CoreManager {

	private static final long DEFAULT_RETRY_WAIT = 15;
	
	private final Logger log = Logger.getLogger(getClass());
	private final ThreadLocal<CoreTransaction> current = new ThreadLocal<CoreTransaction>();

	private final ServiceRegistry registry;
	
	public LocalCoreManager(ServiceRegistry registry) {
		this.registry = registry;
	}
	
	public CoreTransaction create(ContextType type) {
		Arguments.notNull(type, "type");
		if(current.get() != null) throw new IllegalStateException("Transaction already in progress");
		CoreTransaction tr = new LocalTransaction(this, type, registry);
		current.set(tr);
		return tr;
	}

	public CoreTransaction current() {
		return current.get();
	}

	
	// --- TRANSACTION METHODS --- //

	void commit(final LocalTransaction trans) {
		checkIsCurrent(trans);
		doAndClose(trans, new Task() {
			
			@Override
			public void perform() {
				doPrepare(trans);
				doCommit(trans);	
			}
		});
	}

	void rollback(final LocalTransaction trans) {
		checkIsCurrent(trans);
		doAndClose(trans, new Task() {
			
			@Override
			public void perform() {
				doRollback(trans, null);
			}
		});
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void doCommit(final LocalTransaction trans) {
		Set<CoreResource> waiting = new HashSet<CoreResource>(trans.resources);
		for (CoreResource res : trans.resources) {
			if(doCommit(res)) {
				waiting.remove(res);
			} else {
				/*
				 * We're doing nothing here, all other participants 
				 * should be committed.
				 */
			}
		}
		if(waiting.size() > 0) {
			throw new TransactionException("Failed to commit " + waiting.size() + " transactions");
		}
	}
	
	private void doPrepare(final LocalTransaction trans) {
		checkIsCurrent(trans);
		CoreResource current = null;
		for (CoreResource res : trans.resources) {
			current = res;
			if(!doPrepare(res)) {
				/*
				 * Damn: We've got a failure so break this
				 * and rollback all except the failed transactions.
				 */
				break;
			}
			current = null;
		}
		if(current != null) {
			doRollback(trans, current);
			throw new TransactionException("Failed to prepare transaction: " + current);
		} 
	}
	
	public void doAndClose(LocalTransaction trans, Task task) {
		try {
			task.perform();
		} finally {
			close(trans);
		}
	}
	
	private void doRollback(LocalTransaction trans, CoreResource except) {
		for (CoreResource res : trans.resources) {
			if(res != except) {
				res.rollback();
			}
		}
	}
	
	private void checkIsCurrent(LocalTransaction trans) {
		if(!isCurrent(trans)) {
			throw new IllegalStateException("Transaction is not active");
		}
	}

	private boolean isCurrent(LocalTransaction trans) {
		return trans == current();
	}

	private boolean doCommit(CoreResource res) {
		int retries = -1;
		boolean done = false;
		do {
			try {
				res.commit(); 
				done = true;
			} catch(ResourceException e) {
				retries = checkRetry("commit", res, retries, e);
			}
		} while(retries > 0);
		return done;
	}

	private int checkRetry(String comp, CoreResource res, int retries, ResourceException e) {
		if(e.getRetries() > 0) {
			if(retries == -1) {
				retries = e.getRetries(); // first time, set retry
			} else {
				retries--; // count down
			}
			if(retries > 0) {
				retryWait(e);
			}
		} else {
			log.error("Failed " + comp + " for resource; Class: " + res.getClass().getName(), e);
		}
		return retries;
	}
	
	private void retryWait(ResourceException e) {
		long sleep = DEFAULT_RETRY_WAIT;
		if(e.getRetryHint() != -1) {
			sleep = e.getRetryHint();
		}
		try {
			Thread.sleep(sleep);
		} catch(InterruptedException x) { }
	}

	private boolean doPrepare(CoreResource res) {
		int retries = -1;
		boolean done = false;
		do {
			try {
				res.prepare(); 
				done = true;
			} catch(ResourceException e) {
				retries = checkRetry("prepare", res, retries, e);
			}
		} while(retries > 0);
		return done;
	}
	
	private void close(LocalTransaction trans) {
		trans.resources.clear();
		current.set(null);
	}
	
	
	//--- PRIVATE CLASSES --- //
	
	private static interface Task {
		
		public void perform();
		
	}
}
