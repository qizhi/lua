/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
 *
 * This program is licensed under a Firebase Enterprise Edition
 * License. You should have received a copy of the Firebase Enterprise
 * Edition License along with this program. If not, contact info@cubeia.com.
 */
package com.cubeia.firebase.service.pubpersistence;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.persistence.PublicPersistenceService;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.service.jta.util.Transactions;
import com.cubeia.firebase.service.persistence.PersistenceManager;
import com.cubeia.firebase.service.persistence.PersistenceServiceContract;

/**
 * Public persistence service interface
 *
 * @author Fredrik
 */
public class PersistenceServiceImpl implements Service, PublicPersistenceService {
	
	private static transient Logger log = Logger.getLogger(PersistenceServiceImpl.class);
	
	private PersistenceManager persistenceManager;
	private TransactionManager tmanager;
	
	public void destroy() {
		persistenceManager = null;
	}

	public void init(ServiceContext con) throws SystemException {
		ServiceRegistry reg = con.getParentRegistry();
		PersistenceServiceContract service = reg.getServiceInstance(PersistenceServiceContract.class);
		persistenceManager = service.getPersistenceManager();
		TransactionManagerProvider tserv = reg.getServiceInstance(TransactionManagerProvider.class);
		tmanager = tserv.getTransactionManager();
	}

	// Nothing to start or stop really
	public void start() {}
	public void stop() {}
	
	public EntityManager getEntityManager(String name) {
		return getEntityManager(name, false);
	}

	public EntityManager getEntityManager(String name, boolean suppressJoin) {
		try {
			EntityManager em = persistenceManager.getEntityManager(name);
			if (em != null && !suppressJoin && isInTransaction()) {
				em.joinTransaction();
			}
			return em;
		} catch (IllegalStateException e) {
			// The PM is not ready
			log.error("A persistence manager was not ready: "+name+" Msg: "+e.getMessage());
			return null;
		}
	}

	private boolean isInTransaction() {
		try {
			return Transactions.inProgress(tmanager.getStatus());
		} catch(javax.transaction.SystemException e) {
			Logger.getLogger(getClass()).error(e);
			return false;
		}
	}

	public boolean isReady(String name) {
		return persistenceManager.isReady(name);
	}
	
}