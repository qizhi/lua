/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
 *
 * This program is licensed under a Firebase Enterprise Edition
 * License. You should have received a copy of the Firebase Enterprise
 * Edition License along with this program. If not, contact info@cubeia.com.
 */
package com.cubeia.firebase.service.persistence.jpa;

import java.util.Properties;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.jdbc.JDBCContext;
import org.hibernate.transaction.TransactionFactory;
import org.hibernate.util.JTAHelper;

import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.service.InternalServiceRegistry;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;

public class SystemTransactionFactory implements TransactionFactory {

	private TransactionManagerProvider service;

	public SystemTransactionFactory() {
		InternalServiceRegistry reg = InternalComponentAccess.getRegistry();
		service = reg.getServiceInstance(TransactionManagerProvider.class);
	}

	public void configure(Properties props) throws HibernateException { }

	public Transaction createTransaction(JDBCContext jdbcContext, Context context) throws HibernateException {
		return new SystemJTATransaction(service, jdbcContext, context);
	}

	public ConnectionReleaseMode getDefaultReleaseMode() {
		return ConnectionReleaseMode.AFTER_STATEMENT;
	}

	public boolean isTransactionInProgress(JDBCContext jdbcContext, Context transactionContext, Transaction transaction) {
		try {
            // Essentially:
			// 1) If we have a local (Hibernate) transaction in progress
			//      and it already has the UserTransaction cached, use that
			//      UserTransaction to determine the status.
			// 2) If a transaction manager has been located, use
			//      that transaction manager to determine the status.
			// 3) Finally, as the last resort, try to lookup the
			//      UserTransaction via JNDI and use that to determine the
			//      status.
            if ( transaction != null ) {
				UserTransaction ut = ( ( SystemJTATransaction ) transaction ).getUserTransaction();
                if ( ut != null ) {
                    return JTAHelper.isInProgress( ut.getStatus() );
                }
            }

            if ( jdbcContext.getFactory().getTransactionManager() != null ) {
                return JTAHelper.isInProgress( jdbcContext.getFactory().getTransactionManager().getStatus() );
            }
            else {
                UserTransaction ut = service.getUserTransaction();
			    return ut != null && JTAHelper.isInProgress( ut.getStatus() );
            }
		}
		catch( SystemException se ) {
			throw new TransactionException( "Unable to check transaction status", se );
		}
	}

	public boolean isTransactionManagerRequired() {
		return false;
	}

	public boolean areCallbacksLocalToHibernateTransactions() {
		return false;
	}
}
