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
package com.cubeia.space.handler;

import javax.transaction.UserTransaction;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.processor.filter.JbcTransactionResource;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.service.space.GameObjectSpace;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;

/**
 * Abstraction layer that provides common handling for transactional
 * support. 
 *
 * @author Fredrik
 */
public abstract class AbstractTXHandler<E extends Identifiable, T extends Action> implements GameObjectSpace<E, T> {

	private final TransactionManagerProvider provider;
	private final CoreTransactionManager manager;
	protected State state = State.STOPPED;

	public AbstractTXHandler(ServiceRegistry reg) {
		this.provider = reg.getServiceInstance(TransactionManagerProvider.class);
		this.manager = reg.getServiceInstance(CoreTransactionManager.class);
	}
	
	
	public boolean isStarted() {
		return state.equals(State.STARTED);
	}
	
    protected UserTransaction getUserTransaction() {
        if(provider == null || !isJtaEnabled()) return null;
        else return provider.getUserTransaction();
    }

	/*protected UserTransaction getJBossTransaction() {
		return new DummyUserTransaction(DummyTransactionManager.getInstance());
	}*/
	
	protected void attachJbcTransaction() {
		CoreTransaction trans = manager.currentTransaction();
		if(trans != null) {
			trans.attach(new JbcTransactionResource());
		}
	}

	protected abstract boolean isJtaEnabled();
	
}
