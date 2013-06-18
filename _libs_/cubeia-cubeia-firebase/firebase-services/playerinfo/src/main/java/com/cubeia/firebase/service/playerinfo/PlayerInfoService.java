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
package com.cubeia.firebase.service.playerinfo;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.PlayerQueryRequestAction;
import com.cubeia.firebase.api.action.local.PlayerQueryResponseAction;
import com.cubeia.firebase.api.plugin.playerlookup.PlayerLookupService;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService;
import com.cubeia.firebase.api.util.HitCounter;
import com.cubeia.firebase.server.gateway.event.local.ClientLocalActionHandler;
import com.cubeia.firebase.service.playerinfo.trivial.TrivialPlayerLookup;
import com.cubeia.firebase.util.executor.JmxExecutor;

public class PlayerInfoService implements Service, PlayerInfoServiceContract {
	
	private transient Logger log = Logger.getLogger(this.getClass());

	/** The service registry */
	private ServiceRegistry services;
	
	/** Used to get player information */
	private PlayerLookupService playerLookup;
	
	/** Executor service bound to JMX */
	private JmxExecutor executor;
	
	
	
	/*------------------------------------------------
	
	 SERVICE BEAN Methods
	
	-------------------------------------------------*/
	
	public void init(ServiceContext con) throws SystemException {
		services = con.getParentRegistry();
	}

	public void start() {
		playerLookup = initLookupService();
		executor = new JmxExecutor(1, 4, "PlayerLookup");
		log.info("Player Info Service started. Lookup class: "+playerLookup);
	}

	public void stop() {
		executor.stopNow();
	}
	
	public void destroy() {
		playerLookup = null;
	}

	
	
	
	/*------------------------------------------------
	
	 SERVICE CONTRACT Methods
	
	-------------------------------------------------*/

	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.playerinfo.PlayerInfoServiceContract#handlePlayerQueryRequest(com.cubeia.firebase.api.action.local.PlayerQueryRequestAction)
	 */
	public void handlePlayerQueryRequest(PlayerQueryRequestAction request, ClientLocalActionHandler loopback) {
		executor.submit(new LookupTask(request, loopback));
	}
	
	
	
	
	/*------------------------------------------------
	
	 PRIVATE Methods
	
	-------------------------------------------------*/
	
	private PlayerLookupService initLookupService() {
		PlayerLookupService lookup = checkPlayerLookupService();
		if (lookup == null) {
			log.info("No explicit PlayerLookup service found. Using default (TrivialPlayerLookup)");
			lookup = new TrivialPlayerLookup(services.getServiceInstance(PublicClientRegistryService.class));
		}
		
		return lookup;
	}
	
	private PlayerLookupService checkPlayerLookupService() {
		return services.getServiceInstance(PlayerLookupService.class);
	}
	
	/**
	 * Send a response back to the client.
	 * Will use the same loopback as supplied in the handle request.
	 * 
	 * @param response
	 * @param loopback
	 */
	private void sendResponse(PlayerQueryResponseAction response, LocalActionHandler loopback) {
		// Log statistics
		HitCounter.getInstance().inc("PlayerLookup");
		
		if (loopback != null){
			// Notify the action directly on the loopback handler
			loopback.handleAction(response);
			
		} else {
			// All is lost. Log a serious error and go back into the hole you came from.
			log.fatal("There is no return channel defined for the login responses!");
		}
	}
	
	
	
	
	/*------------------------------------------------
	
	 PRIVATE Classes
	
	-------------------------------------------------*/
	
	/**
	 * Task for performing lookups asynchronously
	 */
	private class LookupTask implements Runnable {

		private final PlayerQueryRequestAction request;
		private final ClientLocalActionHandler loopback;

		public LookupTask(PlayerQueryRequestAction request, ClientLocalActionHandler loopback) {
			this.request = request;
			this.loopback = loopback;
		}
		
		public void run() {
			try {
				PlayerQueryResponseAction response = playerLookup.handleRequest(request);
				sendResponse(response, loopback);
			} catch (Throwable th) {
				log.error("Could not perform lookup", th);
			}
		}
		
	}
}