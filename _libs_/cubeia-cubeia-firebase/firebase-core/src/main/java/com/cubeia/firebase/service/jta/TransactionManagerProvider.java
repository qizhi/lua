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
package com.cubeia.firebase.service.jta;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.transaction.EventTransactionContext;
import com.cubeia.firebase.transaction.CoreTransactionManager;

/**
 * This service provides a JTA transaction manager for the rest of 
 * the Firebase system. It gives access to the underlying user, and 
 * transaction managers and should not be exposed to calling code. 
 * 
 * <p>This service also manages the {@link EventTransactionContext} for events
 * together with the {@link CoreTransactionManager core transaction manager}. 
 * 
 * <p>This service contains methods for managing XA data sources. This is
 * because these needs to be wrapped by the JTA provider to function 
 * properly. 
 * 
 * @see CoreTransactionManager
 * @see EventTransactionContext
 * @author Lars J. Nilsson
 */
public interface TransactionManagerProvider extends Contract {

	/**
	 * This method is used by the deployment system to create 
	 * new data source for JTA managed transactional drivers.
	 * 
	 * @param name Deployment name, must not be null
	 * @param prop Configuration properties, from the deployed data source descriptor, must not be null
	 * @return A new data source for the properties, never null
	 */
	public DataSource createLocalTxDataSource(String name, Properties prop) throws SQLException;

	
	/**
	 * Get the underlying transaction manager. This will return an 
	 * instance from the JTA provider.
	 * 
	 * @return The system transaction manager, never null
	 */
	public TransactionManager getTransactionManager();
	
	
	/**
	 * Get the current user transaction. This will return an 
	 * instance from the JTA provider.
	 * 
	 * @return The system user transaction object, never null
	 */
	
	public UserTransaction getUserTransaction();


	/**
	 * This method returns an event transaction context. This 
	 * context is managed by the internal 
	 * {@link CoreTransactionManager core transaction manager}.
	 * 
	 * @return The current event context, or null
	 */
	public EventTransactionContext getEventContext();
}
