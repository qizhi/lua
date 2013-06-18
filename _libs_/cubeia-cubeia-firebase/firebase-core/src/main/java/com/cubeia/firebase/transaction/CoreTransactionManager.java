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
package com.cubeia.firebase.transaction;

import com.cubeia.firebase.api.service.Contract;

/**
 * This is the transaction manager for core transaction within
 * a Firebase server. These transactions are used to wrap executing 
 * events are independent of any existing JTA transactions.
 * 
 * <p>JTA transactions are managed by the {@link TransactionManagerProvider} and
 * will be attached to the {@link CoreTransaction core transaction} as a resource
 * within a given context. 
 * 
 * <p>Core transactions are thread local and self contained. They are
 * created by this manager, but closed and detached via commit / rollback.
 * 
 * @author Lars J. Nilsson
 * @see CoreTransaction
 * @see TransactionManagerProvider
 */
public interface CoreTransactionManager extends Contract {

	/**
	 * Create a return a new transaction. The transaction will
	 * be started and associated with the current thread immediately. If
	 * a transaction is already in progress, an illegal state exception
	 * will be raised.
	 * 
	 * <p>Transactions are closed only via the methods on the transaction
	 * itself, ie rollback or commit. 
	 * 
	 * @type Context type for the transaction, must not be null
	 * @return A new transaction, never null
	 */
	public CoreTransaction newTransaction(ContextType type);
	
	
	/**
	 * The currently active transaction. If no transaction is in progress
	 * null will be returned. New transactions can be created and attached
	 * to the current thread via the {@link #newTransaction(ContextType)} method.
	 * 
	 * @return The current transaction, or null
	 */
	public CoreTransaction currentTransaction();
	
}
