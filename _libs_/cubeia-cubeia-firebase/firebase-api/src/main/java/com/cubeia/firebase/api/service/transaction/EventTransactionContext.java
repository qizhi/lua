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
package com.cubeia.firebase.api.service.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is a context representing the internal Firebase transaction
 * within the system. It contains methods for accessing transactionally
 * bound resources, such as database connections and entity managers.
 * 
 * <p>The objects returned from methods in this context are tied to the current 
 * transaction. In other words, given an ongoing transaction for an event, 
 * the method will return the same object several times. They will be 
 * committed when the event is finished, and rolled back if the event fails.
 * 
 * <p>Also, this transaction context is JTA agnostic, ie. they will work 
 * even if JTA is enabled for the current event. 
 * 
 * @author Larsan
 */
public interface EventTransactionContext {

	/**
	 * This method returns a connection from a given data source, which is tied
	 * to the current transaction. If JTA is enabled and the data source is a 
	 * "local-tx" type it will be handled by the JTA subsystem. If JTA is not 
	 * used, or the data source is not "local-tx" then the connection will be 
	 * handled by the underlying Firebase transactional stack.
	 * 
	 * @param dsName Name of the data source to get connection from, must not be null
	 * @return A database connection, or null if the data source cannot be found
	 * @throws SQLException On database errors
	 */
	public Connection getConnectionForTransaction(String dsName) throws SQLException;
	
	/*
	 * This method returns an entity manager which is tied to the current transaction. 
	 * If JTA is enabled and the data source is a "JTA" transaction-type it will be handled by
	 * the JTA subsystem. If JTA is not used, or the data source is not "JTA" then
	 * the connection will be handled by the underlying Firebase transactional stack.
	 * 
	 * @param emName Entity manager name, must not be null
	 * @return A deployed entity manager, or null if not found
	 */
	// public EntityManager getEntityManagerForTransaction(String emName);

}
