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

import java.util.LinkedList;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.transaction.ContextType;
import com.cubeia.firebase.transaction.CoreResource;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.InternalEventTransactionContext;

/**
 * This is an implementation of the {@link CoreTransaction} interface
 * which belongs to the {@link LocalCoreManager}. All actual transaction
 * transitions are done by the manager and not this class.
 * 
 * @author Lars J. Nilsson
 * @see CoreTransaction
 * @see LocalCoreManager
 */
public class LocalTransaction implements CoreTransaction {

	final LocalCoreManager manager;
	final LinkedList<CoreResource> resources;
	final ContextType type;
	final LocalEventContext context;
	
	LocalTransaction(LocalCoreManager manager, ContextType type, ServiceRegistry registry) {
		this.type = type;
		this.context = new LocalEventContext(registry);
		resources = new LinkedList<CoreResource>();
		this.manager = manager;		
	}
	
	public ContextType getContextType() {
		return type;
	}
	
	public void attach(CoreResource res) {
		Arguments.notNull(res, "resource");
		resources.addFirst(res);
	}

	public boolean isClosed() {
		return manager.current() != this;
	}
	
	public InternalEventTransactionContext getEventContext() {
		attach(context); // Attach only if used?!
		return context;
	}

	public void commit() {
		manager.commit(this);
	}

	public void dettach(CoreResource res) {
		Arguments.notNull(res, "resource");
		resources.remove(res);
	}

	public void rollback() {
		manager.rollback(this);
	}
}
