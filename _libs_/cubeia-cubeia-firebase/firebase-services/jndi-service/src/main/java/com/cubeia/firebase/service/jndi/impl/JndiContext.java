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
package com.cubeia.firebase.service.jndi.impl;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.osjava.sj.memory.MemoryContext;

import com.cubeia.firebase.api.jndi.java.javaURLContextFactory;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.datasource.DatasourceListener;
import com.cubeia.firebase.api.service.datasource.DatasourceServiceContract;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.util.InvocationFacade;

/**
 * A context helper class that manages the root and the sub-contexts, and
 * mounts the transaction manager and all data sources.
 * 
 * @author Lars J. Nilsson
 */
public class JndiContext {

	private final Logger log = Logger.getLogger(getClass());
	
	private Context root;
	private Context ctx;

	private final DatasourceServiceContract manager;
	private final TransactionManagerProvider tmp;

	/**
	 * @param sources Data sources, must not be null
	 * @param tmp Transaction manager, must not be null
	 * @throws SystemException
	 */
	public JndiContext(DatasourceServiceContract sources, TransactionManagerProvider tmp) throws SystemException {
		this.manager = sources;
		this.tmp = tmp;
		try {
			createContexts();
			mountSources();
			listen();	
		} catch(NamingException e) {
			throw new SystemCoreException("Failed to create JNDI contexts", e);
		}
	}
	
	/**
	 * @param facade Invocation to do, must not be null
	 * @return The invocation return
	 * @throws T Any invocation error
	 */
	public <T extends Throwable> Object invokeWithJndi(final InvocationFacade<T> facade) throws T {
		return wrapWithJndi(facade);
	}
	
	
	// --- PRIVATE METHDOS --- //
	
	/*
	 * Listen for added/removed data sources.
	 */
	private void listen() {
		manager.addDatasourceListener(new DatasourceListener() {
			
			@Override
			public void datasourceRemoved(String name) {
				try {
					log.debug("Unbinding data source: " + name);
					ctx.unbind(name);
				} catch (NamingException e) {
					log.error("Failed to unbind data source '" + name + "' from JNDI context.", e);
				}
			}
			
			@Override
			public void datasourceRedeployed(String name) { }
			
			@Override
			public void datasourceAdded(String name) {
				DataSource src = manager.getDatasource(name);
				if(src != null) {
					try {
						log.debug("Binding data source: " + name);
						ctx.bind(name, src);
					} catch(NamingException e) {
						log.error("Failed to bind data source '" + name + "' to JNDI context.", e);
					}
				}
			}
		});
	}

	/*
	 * Mount initial data sources
	 */
	private void mountSources() throws NamingException {
		for (String name : manager.getDatasources()) {
			DataSource src = manager.getDatasource(name);
			if(src != null) {
				log.debug("Binding data source: " + name);
				ctx.bind(name, src);
			}
		}
	}

	/*
	 * Create contexts and mount transaction manager
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createContexts() throws NamingException {
		log.debug("Creating default JNDI context comp/env/jdbc");
		Hashtable env = new Hashtable();
		env.put("jndi.syntax.direction", "left_to_right");
		env.put("jndi.syntax.separator", "/");
		root = new MemoryContext(env);
		ctx = root.createSubcontext("comp");
		ctx = ctx.createSubcontext("env");
		TransactionManager tm = tmp.getTransactionManager();
		if(tm != null) {
			log.debug("Binding transaction manager at comp/env/TransactionManager");
			ctx.bind("TransactionManager", tm);
			log.debug("Binding user transaction at comp/env/UserTransaction");
			ctx.bind("UserTransaction", tmp.getUserTransaction());
		}
		ctx = ctx.createSubcontext("jdbc");
	}
	
	/*
	 * Wrap and invoke
	 */
	private <T extends Throwable> Object wrapWithJndi(InvocationFacade<T> facade) throws T {
		javaURLContextFactory.enter(root);
		try {
			return facade.invoke();
		} finally {
			javaURLContextFactory.exit();
		}
	}
}
