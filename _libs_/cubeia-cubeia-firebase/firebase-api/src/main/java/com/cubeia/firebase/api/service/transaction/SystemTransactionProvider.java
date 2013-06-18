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
package com.cubeia.firebase.api.service.transaction;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.cubeia.firebase.api.service.Contract;

/**
 * This is the public interface for the Firebase transaction
 * system. Using this interface modules can reference the transaction
 * manager and the user transaction.
 * 
 * @author Larsan
 */
public interface SystemTransactionProvider extends Contract {

	/**
	 * @return The system transaction manager, never null
	 */
	public TransactionManager getTransactionManager();
	
	/**
	 * @return The system user transaction object, never null
	 */
	public UserTransaction getUserTransaction();
	
	/**
	 * This method returns a context for the currently executing event. 
	 * Ie. if this method is called on by a thread which is executing an 
	 * event for a game, tournament or a service, the returned context is 
	 * legal for the given thread and the given execution.
	 * 
	 * <p>Please note that for external integrations that have started a user
	 * transaction manually, this method will still return null.
	 * 
	 * @return The current event context, or null if called outside an event scope
	 */
	public EventTransactionContext getEventContext();
	
}
