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
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.ObjectFactoryBuilder;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.datasource.DatasourceServiceContract;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;

/**
 * This is the service implementation of {@link JndiProvider}
 * which uses simple-jndi to wrap invocations. It also mounts
 * the transaction manager, and manages the data source mounting.
 * 
 * @author Lars J. Nilsson
 */
public class ServiceImpl implements Service, JndiProvider {
	
	private final Logger log = Logger.getLogger(getClass());
	
	private AtomicReference<JndiContext> eventContext = new AtomicReference<JndiContext>();

	public void init(ServiceContext con) throws SystemException { 
		// install factory building (for class loading issues)
		installObjectFactoryBuilder();
		// We're depending on the data source service
		DatasourceServiceContract manager = con.getParentRegistry().getServiceInstance(DatasourceServiceContract.class);
		TransactionManagerProvider tmp = con.getParentRegistry().getServiceInstance(TransactionManagerProvider.class);
		// create context
		this.eventContext.set(new JndiContext(manager, tmp));
	}

	public void start() { }
	 
	@Override
	public <T extends Throwable> Object wrapInvocation(InvocationFacade<T> facade) throws T {
		/*
		 * Thread safe startup, probably not needed but you can never be
		 * too careful... 
		 */
		JndiContext c = eventContext.get();
		if(c != null) {
			return c.invokeWithJndi(facade);
		} else {
			return facade.invoke();
		}
	}

	public void stop() {}
	
	public void destroy() {}
	
	
	// --- PRIVATE METHODS --- //
	
	private void installObjectFactoryBuilder() throws SystemException {
		try {
			/*
			 * We're installing a custom factory builder here in order
			 * to get the correct class loader (ie. the internal server
			 * loader). 
			 */
			NamingManager.setObjectFactoryBuilder(new ObjectFactoryBuilder() {
				
				@Override
				public ObjectFactory createObjectFactory(final Object obj, final Hashtable<?, ?> environment) throws NamingException {
					// invoke within server class loader context
					return (ObjectFactory) Classes.switchContextClassLoaderForInvocation(new InvocationFacade<NamingException>() {
						
						@Override
						public Object invoke() throws NamingException {
							return tryLoadReference(obj, environment);
						}
					}, ServiceImpl.this.getClass().getClassLoader());
				}
			});
		} catch(NamingException e) {
			throw new SystemCoreException("Failed to install object factory builder", e);
		}
	}

	/*
	 * Try to create a factory from a reference (using the server class loader).
	 */
	private Object tryLoadReference(Object obj, Hashtable<?, ?> environment) throws NamingException {
		Reference ref = null;
		if (obj instanceof Reference) {
		    ref = (Reference) obj;
		} else if (obj instanceof Referenceable) {
		    ref = ((Referenceable)(obj)).getReference();
		} else {
			throw new UnsupportedOperationException("Unknown reference type: " + (obj == null ? "null" : obj.getClass().getName()));
		}
		String factoryClassName = ref.getFactoryClassName();
		log.debug("Attempting to load factory: " + factoryClassName);
		try {
			return getClass().getClassLoader().loadClass(factoryClassName).newInstance();
		} catch (Exception e) {
			log.error("Failed to load factory: " + factoryClassName, e);
			throw new NamingException("Failed to load factory: " + factoryClassName);
		}
	}
}