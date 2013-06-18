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

/**
 * Internal Firebase transaction object. This transaction is
 * internal to Firebase and used to commit attached resources. It
 * is managed by the {@link CoreTransactionManager} service. 
 * 
 * <p>A core transaction exists in a context of an executing event (either 
 * game, service or tournament events) and attached resources are resources
 * are committed in FILO order. 
 * 
 * @author Lars J. Nilsson
 * @see CoreTransactionManager
 * @see CoreResource
 */
public interface CoreTransaction {
	
	/**
	 * The original context type for this transaction. This is
	 * useful for debugging purposes.
	 * 
	 * @return The type the transaction was originally created for, never null
	 */
	public ContextType getContextType();
	
	/**
	 * Attempt to commit this transaction. This will propagate a 
	 * "prepare" call to all resources, if successful followed by
	 * an actual commit. 
	 * 
	 * @throws TransactionException If the commit fails
	 */
	public void commit() throws TransactionException;
	
	/**
	 * @return The current event context, never null
	 */
	public InternalEventTransactionContext getEventContext();
	
	/**
	 * Attach a resource to the transaction. Resources are handled
	 * in FILO order.
	 * 
	 * @param res The resource to attach, must not be null
	 */
	public void attach(CoreResource res);
	
	
	/**
	 * Detach (remove) a resource from the transaction. If the resource
	 * is not attached, this method silently returns.
	 * 
	 * @param res Resource to detach, must not be null
	 */
	public void dettach(CoreResource res);
	
	
	/**
	 * Check if the transaction is closed. The transaction is closed if
	 * it is either committed or rolled back.
	 * 
	 * @return True if the transaction is closed, false otherwise
	 */
	public boolean isClosed();
	
	
	/**
	 * Attempt a rollback of the transaction.
	 */
	public void rollback();
	
}
