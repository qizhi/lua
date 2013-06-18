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

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.processor.ProcessorChain;
import com.cubeia.firebase.api.action.processor.ProcessorFilter;
import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;

/**
 * This filter attaches a JBC transaction to the internal transaction stack
 * and then forwards the action to the filter chain.
 * 
 * @author Lars J. Nilsson
 */
public class JbcTransactionAttachFilter<T extends Identifiable, A extends Action> implements ProcessorFilter<T, A> {

	private final CoreTransactionManager manager;
	
	public JbcTransactionAttachFilter(CoreTransactionManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void process(A action, T data, ProcessorChain<T, A> filters) {
		attachJbcTransaction();
		filters.next(action, data);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void attachJbcTransaction() {
		CoreTransaction trans = manager.currentTransaction();
		if(trans != null) {
			trans.attach(new JbcTransactionResource());
		}
	}
}
