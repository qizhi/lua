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
package com.cubeia.firebase.server.activation;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.server.routing.impl.RouterContext;
import com.cubeia.firebase.server.service.jndi.JndiProvider;
import com.cubeia.firebase.util.Classes;
import com.cubeia.firebase.util.InvocationFacade;
import com.cubeia.firebase.util.executor.JmxExecutor;
import com.cubeia.util.threads.SafeRunnable;

/**
 * This sender has an internal thread pool used to dispatch events to 
 * its given service. The thread pool can be configured and monitored via JMX.
 * 
 * @author Larsan
 */
public class ActivatorSender {

	private final RoutableActivator activator;
	private final RouterContext con;
	private final int id;
	
	private JmxExecutor threads;
	private ActivatorConfig conf;
	
	private final Logger log = Logger.getLogger(getClass());

	// private TransactionManagerProvider provider;
	// private CoreTransactionManager manager;
	
	private final boolean isGame;
	private final ClassLoader targetLoader;
	private final JndiProvider jndiProvider;
	
	public ActivatorSender(int id, RouterContext con, RoutableActivator activator, boolean isGame, ClassLoader targetLoader) {
		this.id = id;
		this.con = con;
		this.activator = activator;
		this.isGame = isGame;
		this.targetLoader = targetLoader;
		jndiProvider = con.getServices().getServiceInstance(JndiProvider.class);
		// setupManagers();
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

	public void dispatch(ActivatorCommand com) {
		if(com != null) {
			threads.submit(new Task(com));
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	/*private void setupManagers() {
		provider = con.getServices().getServiceInstance(TransactionManagerProvider.class);
		manager = con.getServices().getServiceInstance(CoreTransactionManager.class);
	}*/
	
	private void setupThreads() {
		threads = new JmxExecutor(conf.getSenderPoolProperties().getCoreSize(), conf.getSenderPoolProperties().getMaxSize(), conf.getSenderPoolProperties().getTimeout(), (isGame ? "Game" : "Mtt") + "ActivatorSender", String.valueOf(id));
	}

	private void setupConf() {
		conf = ActivatorUtil.createConfig(con.getServices(), !isGame);
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class Task extends SafeRunnable {
		
		private final ActivatorCommand event;
		
		private Task(ActivatorCommand event) {
			this.event = event;
		}
		
		@Override
		protected void innerRun() {
	    	/*CoreTransaction trans = manager.newTransaction(ContextType.GAME_EVENT);
	    	try {*/
	    		checkWrapJta();	
	    		/*trans.commit();
	    	} catch(TransactionException e) {
	    		log.error("Could not commit transaction!", e);
	    	} catch(Throwable th) {
	    		log.error("Error when handling action, rolling back transaction", th);
	            trans.rollback();
	    	}*/
		}

		private void checkWrapJta() {
			/*if(conf.jtaEnabled()) {
				wrapJta();
			} else {*/
				doHandoff();
			// }
		}
		
		private void doHandoff() {
			try {
			 	final ActivatorAction<?> a = event.getAction(targetLoader);
			 	jndiProvider.wrapInvocation(new InvocationFacade<RuntimeException>() {
					@Override
					public Object invoke() throws RuntimeException {
						return Classes.switchContextClassLoaderForInvocation(new InvocationFacade<RuntimeException>() {
							
							@Override
							public Object invoke() throws RuntimeException {
								activator.onAction(a);
								return null;
							}
						}, targetLoader);
					}
				});
			} catch (Exception e) {
				log.error("Failed to unwrap activator action", e);
			} 	
		}

		/*private void wrapJta() {
			UserTransaction trans = provider.getUserTransaction();
			boolean done = false;
			try {
				trans.begin();
				doHandoff();
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
		}*/
	}
}