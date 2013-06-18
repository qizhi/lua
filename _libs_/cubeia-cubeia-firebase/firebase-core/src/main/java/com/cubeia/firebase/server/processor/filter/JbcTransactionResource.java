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

import org.apache.log4j.Logger;
import org.jboss.cache.transaction.DummyTransactionManager;
import org.jboss.cache.transaction.DummyUserTransaction;

import com.cubeia.firebase.transaction.CoreResource;
import com.cubeia.firebase.transaction.ResourceException;

public class JbcTransactionResource implements CoreResource {

	private Logger log = Logger.getLogger(getClass());
	private final DummyUserTransaction trans;

	public JbcTransactionResource() {
		trans = new DummyUserTransaction(DummyTransactionManager.getInstance());
		try {
			trans.begin();
		} catch (Exception e) {
			log.error("Failed to start JBC transaction", e);
		} 
	}
	
	public void cancel() { 
		rollback();
	}

	public void close() { }

	public void commit() throws ResourceException { 
		try {
			trans.commit();
		} catch (Exception e) {
			log.error("Failed to commit JBC transaction", e);
		} 
	}

	public void prepare() throws ResourceException { }

	public void rollback() {
		try {
			trans.rollback();
		} catch (Exception e) {
			log.error("Failed to rollback JBC transaction", e);
		} 
	}
}
