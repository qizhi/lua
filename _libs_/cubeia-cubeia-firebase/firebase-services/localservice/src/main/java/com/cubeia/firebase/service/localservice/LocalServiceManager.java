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
package com.cubeia.firebase.service.localservice;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.LocalServiceAction;
import com.cubeia.firebase.api.plugin.localhandler.LocalHandlerService;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.util.executor.JmxExecutor;

/**
 * Enables asynchronous handoff for the implemting local handler service.
 *
 * @author Fredrik
 */
public class LocalServiceManager implements Service, LocalServiceManagerContract {
	
	private transient Logger log = Logger.getLogger(this.getClass());

	/** The service registry */
	private ServiceRegistry services;
	
	/** The deployed local handler service */
	private LocalHandlerService handler;
	
	/** Executor service bound to JMX */
	private JmxExecutor executor = new JmxExecutor(1, 4, "LocalHandlerService");
	
	
	
	/*------------------------------------------------
	
	 SERVICE BEAN Methods
	
	-------------------------------------------------*/
	
	public void init(ServiceContext con) throws SystemException {
		services = con.getParentRegistry();
	}

	public void start() {
		handler = initHandlerService();
		log.info("Local Handler Manager started.");
	}

	public void stop() {
		executor.stopNow();
	}
	
	public void destroy() {
		handler = null;
		services = null;
	}

	
	
	
	/*------------------------------------------------
	
	 SERVICE CONTRACT Methods
	
	-------------------------------------------------*/

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.localservice.LocalServiceManagerContract#handleAction(com.cubeia.firebase.api.action.local.LocalServiceAction, com.cubeia.firebase.api.action.local.LocalActionHandler)
	 */
	public void handleAction(LocalServiceAction action, LocalActionHandler loopback) {
		Task task = new Task(action, loopback);
		executor.submit(task);
	}
	
	
	
	
	/*------------------------------------------------
	
	 PRIVATE Methods
	
	-------------------------------------------------*/
	
	private LocalHandlerService initHandlerService() {
		LocalHandlerService service = services.getServiceInstance(LocalHandlerService.class);
		if (service == null) {
			log.info("No explicit Local Handler Service is deployed.");
		}
		return service;
	}
	
	
	
	/*------------------------------------------------
	
	 PRIVATE Classes
	
	-------------------------------------------------*/
	
	/**
	 * Task for performing lookups asynchronously
	 */
	private class Task implements Runnable {

		private final LocalServiceAction action;
		private final LocalActionHandler loopback;

		public Task(LocalServiceAction action, LocalActionHandler loopback) {
			this.action = action;
			this.loopback = loopback;
		}
		
		public void run() {
			if (handler != null) {
				try {
					handler.handleAction(action, loopback);
				} catch (Throwable th) {
					log.error("Error occurred when handling local service action", th);
				}
			} else {
				log.error("No local service is deployed. Discarding service action: "+action);
			}
		}
		
	}




	
}