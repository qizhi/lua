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
package com.cubeia.firebase.server.processor.filter;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.action.processor.ProcessorFilter;
import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;

/**
 * This filter attaches a JTA transaction to the execution of the action. It
 * does not look at configuration and should only be put in the filter chain
 * if it should indeed be used. 
 * 
 * @author Lars J. Nilsson
 */
// TODO Shouldn't this be attached to the internal transaction instead?
public class JtaTransactionAttachFilter <T extends Identifiable, A extends Action> implements ProcessorFilter<T, A> {

	private final Logger log = Logger.getLogger(getClass());
	
	private final TransactionManagerProvider manager;

	public JtaTransactionAttachFilter(TransactionManagerProvider manager) {
		this.manager = manager;
	}
	
	@Override
	public void process(A action, T data, ProcessorChain<T, A> filters) {
		UserTransaction trans = getUserTransaction();
		if(trans != null) {
			wrapJtaAndDispatch(action, data, trans, filters);
		} else {
			doDispatch(action, data, filters);
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void wrapJtaAndDispatch(A action, T data, UserTransaction trans, ProcessorChain<T, A> filters) {
		if(log.isTraceEnabled()) {
        	log.trace("Entering JTA wrpa/dispatch for action [" + action + "] to data " + data.getId());
        }
		boolean done = false;
		/*
		 * Note: don't catch all here, any exception from filters further
		 * in the chain or from the game itself should propagate normally
		 */
		try {
			trans.begin();
			doDispatch(action, data, filters);
			trans.commit();
			done = true;
		} catch (NotSupportedException e) {
			failTransaction(e);
		} catch (SystemException e) {
			failTransaction(e);
		} catch (SecurityException e) {
			failTransaction(e);
		} catch (IllegalStateException e) {
			failTransaction(e);
		} catch (RollbackException e) {
			failTransaction(e);
		} catch (HeuristicMixedException e) {
			failTransaction(e);
		} catch (HeuristicRollbackException e) {
			failTransaction(e);
		} finally {
			if(log.isTraceEnabled()) {
	        	log.trace("Exiting JTA wrpa/dispatch for action [" + action + "] to data " + data.getId() + "; done: " + done);
	        }
			if(!done) {
				try {
					trans.rollback();
				} catch (Exception e) {
					log.fatal("Failed to rollback transaction!", e);
				}
			}
		}
	}
	
	private void failTransaction(Exception e) {
		throw new IllegalStateException("Failed user transaction", e);
	}
	
	private void doDispatch(A action, T data, ProcessorChain<T, A> filters) {
		filters.next(action, data);
	}

	private UserTransaction getUserTransaction() {
        if(manager == null) {
        	log.warn("JTA transaction filter attached, but no transaction manager found!");
        	return null;
        } else {
        	return manager.getUserTransaction();
        }
    }
}
