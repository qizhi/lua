/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
 *
 * This program is licensed under a Firebase Enterprise Edition
 * License. You should have received a copy of the Firebase Enterprise
 * Edition License along with this program. If not, contact info@cubeia.com.
 */
package com.cubeia.firebase.service.persistence.jpa;

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.service.InternalServiceRegistry;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;

public class SystemTransactionManagerLookup implements TransactionManagerLookup {

	private TransactionManagerProvider service;

	public SystemTransactionManagerLookup() {
		InternalServiceRegistry reg = InternalComponentAccess.getRegistry();
		service = reg.getServiceInstance(TransactionManagerProvider.class);
	}
	
	public TransactionManager getTransactionManager(Properties props) throws HibernateException {
		return service.getTransactionManager();
	}

	public String getUserTransactionName() {
		return null;
	}
}
