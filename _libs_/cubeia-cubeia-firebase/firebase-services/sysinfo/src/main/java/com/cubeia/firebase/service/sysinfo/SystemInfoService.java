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
package com.cubeia.firebase.service.sysinfo;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.SystemInfoRequestAction;
import com.cubeia.firebase.api.action.local.SystemInfoResponseAction;
import com.cubeia.firebase.api.plugin.sysinfo.SystemInfoQueryService;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.server.gateway.event.local.ClientLocalActionHandler;
import com.cubeia.firebase.service.clientreg.ClientRegistryServiceContract;
import com.cubeia.firebase.util.executor.JmxExecutor;

public class SystemInfoService implements Service, SystemInfoServiceContract {
	
	private transient Logger log = Logger.getLogger(this.getClass());

	/** The service registry */
	private ServiceRegistry services;
	
	/** Used to get deployed customary system information */
	private SystemInfoQueryService infoLookup;
	
	/** Executor service bound to JMX */
	private JmxExecutor executor;
	
	
	
	/*------------------------------------------------
	
	 SERVICE BEAN Methods
	
	-------------------------------------------------*/
	
	public void init(ServiceContext con) throws SystemException {
		services = con.getParentRegistry();
	}

	public void start() {
	    infoLookup = initLookupService();
		executor = new JmxExecutor(1, 1, "SystemInfoQuery");
		log.info("System Info Service started. Lookup class: "+infoLookup);
	}

	public void stop() {
		executor.stopNow();
	}
	
	public void destroy() {
	    infoLookup = null;
	}

	
	
	
	/*------------------------------------------------
	
	 SERVICE CONTRACT Methods
	
	-------------------------------------------------*/

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.playerinfo.PlayerInfoServiceContract#handlePlayerQueryRequest(com.cubeia.firebase.api.action.local.PlayerQueryRequestAction)
	 */
    public void handleSystemInfoRequest(SystemInfoRequestAction request, ClientLocalActionHandler loopback) {
        executor.submit(new LookupTask(request, loopback));
    }
	
	
	
	/*------------------------------------------------
	
	 PRIVATE Methods
	
	-------------------------------------------------*/
	
	private SystemInfoQueryService initLookupService() {
	    SystemInfoQueryService lookup = checkSystemInfoLookupService();
		if (lookup == null) {
			log.info("Found no deployed System Info Query Service.");
		}
		return lookup;
	}
	
	private SystemInfoQueryService checkSystemInfoLookupService() {
		return services.getServiceInstance(SystemInfoQueryService.class);
	}
	
	/**
	 * Send a response back to the client.
	 * Will use the same loopback as supplied in the handle request.
	 * 
	 * @param response
	 * @param loopback
	 */
	private void sendResponse(SystemInfoResponseAction response, LocalActionHandler loopback) {
		if (loopback != null){
			// Notify the action directly on the loopback handler
			loopback.handleAction(response);
			
		} else {
			// All is lost. Log a serious error and go back into the hole you came from.
			log.fatal("There is no return channel defined for the system info responses!");
		}
	}
	
	
	private SystemInfoResponseAction handleRequest(SystemInfoRequestAction request) {
	    ClientRegistryServiceContract clientRegistry = services.getServiceInstance(ClientRegistryServiceContract.class);
	    SystemInfoResponseAction response = new SystemInfoResponseAction(clientRegistry.getClientRegistry().getNumberOfGlobalClients());
	    return response;
	}
	
	
	/*------------------------------------------------
	
	 PRIVATE Classes
	
	-------------------------------------------------*/
	
	/**
	 * Task for performing lookups asynchronously
	 */
	private class LookupTask implements Runnable {

		private final SystemInfoRequestAction request;
		private final ClientLocalActionHandler loopback;

		public LookupTask(SystemInfoRequestAction request, ClientLocalActionHandler loopback) {
			this.request = request;
			this.loopback = loopback;
		}
		
		public void run() {
			try {
			    SystemInfoResponseAction response = handleRequest(request);
			    if (infoLookup != null) {
			        log.debug("System Info plugin service found. Executing response: "+response);
			        response = infoLookup.appendResponseData(response);
			    }
			    log.debug("Send system info respone: "+response);
				sendResponse(response, loopback);
			} catch (Throwable th) {
				log.error("Could not perform lookup", th);
			}
		}
		
	}


}