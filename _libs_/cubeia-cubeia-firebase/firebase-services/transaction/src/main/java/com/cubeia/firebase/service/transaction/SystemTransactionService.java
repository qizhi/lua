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
package com.cubeia.firebase.service.transaction;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.transaction.EventTransactionContext;
import com.cubeia.firebase.api.service.transaction.SystemTransactionProvider;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;

public class SystemTransactionService implements SystemTransactionProvider, Service {

	private TransactionManagerProvider serv;

	public EventTransactionContext getEventContext() {
		return serv.getEventContext();
	}
	
	public TransactionManager getTransactionManager() {
		return serv.getTransactionManager();
	}

	public UserTransaction getUserTransaction() {
		return serv.getUserTransaction();
	}

	public void destroy() {
		serv = null;
	}

	public void init(ServiceContext con) throws SystemException {
		ServiceRegistry reg = con.getParentRegistry();
		serv = reg.getServiceInstance(TransactionManagerProvider.class);
	}

	public void start() { }

	public void stop() { }

}
