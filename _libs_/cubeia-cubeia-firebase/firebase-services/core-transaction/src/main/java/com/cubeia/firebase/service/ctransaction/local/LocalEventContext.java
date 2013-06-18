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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.datasource.DatasourceManager;
import com.cubeia.firebase.service.datasource.TxType;
import com.cubeia.firebase.service.datasource.intern.InternalDataSourceProvider;
import com.cubeia.firebase.transaction.CoreResource;
import com.cubeia.firebase.transaction.InternalEventTransactionContext;
import com.cubeia.firebase.transaction.ResourceException;

/**
 * An {@link InternalEventTransactionContext} implementation. It keeps any 
 * "started" JTA resource in a map of associations.
 * 
 * @author Lars J. Nilsson
 */
public class LocalEventContext implements InternalEventTransactionContext, CoreResource {
	
	// keys used for internal associations
	public static final String CONNECTION_KEY = "_connection";
	public static final String ENTITYMANAGER_KEY = "_entitymanager";

	private final Logger log = Logger.getLogger(getClass());
	
	private final ServiceRegistry registry;
	private final Map<String, Object> associations = new HashMap<String, Object>(3);
	
	private boolean isConnectionJta = false;
	// private boolean isEntityManagerJta = false;
	
	public LocalEventContext(ServiceRegistry registry) {
		this.registry = registry;	
	}
	
	public Map<String, Object> getAssociations() {
		return associations;
	}

	public Connection getConnectionForTransaction(String dsName) throws SQLException {
		Arguments.notNull(dsName, "dsName");
		if(associations.containsKey(CONNECTION_KEY)) {
			// reuse existing connections
			return (Connection)associations.get(CONNECTION_KEY);
		} else {
			// create connection
			DatasourceManager man = getDatasourceManager();
			DataSource ds = man.getDatasource(dsName);
			if(ds == null) return null; // SANITY CHECK
			Connection con = ds.getConnection();
			// save association
			associations.put(CONNECTION_KEY, con);
			// check transaction type
			TxType type = man.getDatasourceType(dsName);
			isConnectionJta = type == TxType.LOCAL_TX;
			return con;
		}
	}

	/*public EntityManager getEntityManagerForTransaction(String emName) {
		Arguments.notNull(emName, "emName");
		if(associations.containsKey(ENTITYMANAGER_KEY)) {
			// reuse existing
			return (EntityManager) associations.get(ENTITYMANAGER_KEY);
		} else {
			// create new manager
			PersistenceManager manager = getPersistenceManager();
			EntityManager em = manager.getEntityManager(emName);
			if(em == null) return null; // SANITY CHECK
			associations.put(ENTITYMANAGER_KEY, em);
			isEntityManagerJta = checkEmJta(em);
			return em;
		}
	}*/

	
	// --- CORE RESOURCE --- //

	@Override
	public void commit() throws ResourceException {
		try {
			// commitEntityManager();
			commitConnection();
		} finally {
			close();
		}
	}

	@Override
	public void prepare() throws ResourceException { }

	@Override
	public void rollback() {
		try {
			// rollbackEntityManager();
			rollbackConnection();	
		} finally {
			close();
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void close() {
		// closeEntityManager();
		closeConnection();
	}
	
	private void closeConnection() {
		if(associations.containsKey(CONNECTION_KEY)) {
			Connection con = (Connection)associations.remove(CONNECTION_KEY);
			try {
				con.close();
			} catch (Exception e) {
				log.error("Failed to close connection", e);
			}
		}
	}

	/*private void closeEntityManager() {
		if(associations.containsKey(ENTITYMANAGER_KEY)) {
			EntityManager em = (EntityManager) associations.remove(ENTITYMANAGER_KEY);
			try {
				em.close();
			} catch (Exception e) {
				log.error("Failed to close entity manager", e);
			}
		}
	}*/
	
	private void commitConnection() throws ResourceException {
		if(associations.containsKey(CONNECTION_KEY) && !isConnectionJta) {
			Connection con = (Connection)associations.get(CONNECTION_KEY);
			try {
				con.commit();
			} catch (Exception e) {
				throw new ResourceException("Failed to commit connection", e);
			}
		}
	}

	/*private void commitEntityManager() {
		if(associations.containsKey(ENTITYMANAGER_KEY) && !isEntityManagerJta) {
			EntityManager em = (EntityManager)associations.get(ENTITYMANAGER_KEY);
			EntityTransaction trans = em.getTransaction();
			try {
				trans.commit();
			} catch (Exception e) {
				throw new ResourceException("Failed to commit entity manager", e);
			}
		}
	}*/
	
	private void rollbackConnection() throws ResourceException {
		if(associations.containsKey(CONNECTION_KEY) && !isConnectionJta) {
			Connection con = (Connection)associations.get(CONNECTION_KEY);
			try {
				con.rollback();
			} catch (Exception e) {
				throw new ResourceException("Failed to rollback connection", e);
			}
		}
	}

	/*private void rollbackEntityManager() {
		if(associations.containsKey(ENTITYMANAGER_KEY) && !isEntityManagerJta) {
			EntityManager em = (EntityManager)associations.get(ENTITYMANAGER_KEY);
			EntityTransaction trans = em.getTransaction();
			try {
				trans.rollback();
			} catch (Exception e) {
				throw new ResourceException("Failed to rollback connection", e);
			}
		}
	}*/
	
	/*private boolean checkEmJta(EntityManager em) {
		/*
		 * This is a bit of a hack, we'll see if the transaction 
		 * getter returns or not, it should throw an illegal state
		 * exception if it is configured with JTA /Larsan
		 *
		try {
			EntityTransaction trans = em.getTransaction();
			trans.begin();
			return false;
		} catch(IllegalStateException e) {
			return true;
		}
	}*/
	
	/*private PersistenceManager getPersistenceManager() {
		PersistenceServiceContract service = registry.getServiceInstance(PersistenceServiceContract.class);
		return service.getPersistenceManager();
	}*/
	
	private DatasourceManager getDatasourceManager() {
		InternalDataSourceProvider serv = registry.getServiceInstance(InternalDataSourceProvider.class);
		return serv.getDatasourceManager();
	}
}
