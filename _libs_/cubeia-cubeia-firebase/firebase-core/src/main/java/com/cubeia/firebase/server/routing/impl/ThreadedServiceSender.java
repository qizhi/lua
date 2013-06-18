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
package com.cubeia.firebase.server.routing.impl;

import static com.cubeia.firebase.util.Classes.switchContextClassLoaderForInvocation;

import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.server.event.LocalServiceEvent;
import com.cubeia.firebase.server.instance.ServerServiceConfig;
import com.cubeia.firebase.server.service.DefaultServiceRegistry;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.service.jta.TransactionManagerProvider;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.transaction.ContextType;
import com.cubeia.firebase.transaction.CoreTransaction;
import com.cubeia.firebase.transaction.CoreTransactionManager;
import com.cubeia.firebase.transaction.TransactionException;
import com.cubeia.firebase.util.InvocationFacade;
import com.cubeia.firebase.util.executor.JmxExecutor;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This sender has an internal thread pool used to dispatch events to 
 * its given service. The thread pool can be configured and monitored via JMX.
 * 
 * @author Larsan
 */
public class ThreadedServiceSender implements Sender<LocalServiceEvent> {
	
	private final static AtomicInteger COUNT = new AtomicInteger();

	private final String serviceId;
	private final RoutableService service;
	private final RouterContext con;

	private JmxExecutor threads;
	private ServerServiceConfig conf;
	
	private final Logger log = Logger.getLogger(getClass());

	private TransactionManagerProvider provider;
	private CoreTransactionManager manager;
	
	ThreadedServiceSender(String serviceId, RouterContext con, RoutableService service) {
		this.serviceId = serviceId;
		this.con = con;
		this.service = service;
		setupManagers();
		setupConf();
		setupThreads();
	}

	public String getOwnerId() {
		return null;
	}
	
	public void destroy() { }
	
	public void stop() {
		threads.stop();
	}

	public void dispatch(LocalServiceEvent event) {
		if(event != null) {
			threads.submit(new Task(event));
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void setupManagers() {
		provider = con.getServices().getServiceInstance(TransactionManagerProvider.class);
		manager = con.getServices().getServiceInstance(CoreTransactionManager.class);
	}
	
	private void setupThreads() {
		threads = new JmxExecutor(conf.getSenderPoolProperties().getCoreSize(), conf.getSenderPoolProperties().getMaxSize(), conf.getSenderPoolProperties().getTimeout(), "ServiceSender-" + service.getClass().getSimpleName() + "-" + COUNT.incrementAndGet());
	}

	private void setupConf() {
		conf = DefaultServiceRegistry.getServiceConf(con.getServices(), serviceId);
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Task extends SafeRunnable {
		
		private final LocalServiceEvent event;
		
		private Task(LocalServiceEvent event) {
			this.event = event;
		}
		
		@Override
		protected void innerRun() {
	    	CoreTransaction trans = manager.newTransaction(ContextType.GAME_EVENT);
	    	try {
	    		checkWrapJta();	
	    		trans.commit();
	    	} catch(TransactionException e) {
	    		log.error("Could not commit transaction!", e);
	    	} catch(Throwable th) {
	    		log.error("Error when handling action, rolling back transaction", th);
	            trans.rollback();
	    	}
		}

		private void checkWrapJta() {
			if(conf.jtaEnabled()) {
				wrapJta();
			} else {
				doAction();
			}
		}

		private void doAction() {
			final ServiceAction a = event.getAction();
			con.getServices().getServiceInstance(JndiProvider.class).wrapInvocation(new InvocationFacade<RuntimeException>() {
				@Override
				public Object invoke() throws RuntimeException {
					return switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
						@Override
						public Object invoke() throws RuntimeException {
							service.onAction(a);
							return null;
						}
					}, service.getClass().getClassLoader());
				}
			});
		}

		private void wrapJta() {
			UserTransaction trans = provider.getUserTransaction();
			boolean done = false;
			try {
				trans.begin();
				doAction();
				trans.commit();
				done = true;
			} catch (NotSupportedException e) {
				failTransaction(e);
			} catch (SystemException e) {
				failTransaction(e);
			} catch (SecurityException e) {
				failTransaction(e);
			} catch (IllegalStateException e) {
				failTransaction(e);
			} catch (RollbackException e) {
				failTransaction(e);
			} catch (HeuristicMixedException e) {
				failTransaction(e);
			} catch (HeuristicRollbackException e) {
				failTransaction(e);
			} finally {
				if(!done) {
					try {
						trans.rollback();
					} catch (Exception e) {
						log.fatal("Failed to rollback transaction!", e);
					}
				}
			}
		}
		
		private void failTransaction(Exception e) {
			throw new IllegalStateException("Failed user transaction", e);
		}
	}
}