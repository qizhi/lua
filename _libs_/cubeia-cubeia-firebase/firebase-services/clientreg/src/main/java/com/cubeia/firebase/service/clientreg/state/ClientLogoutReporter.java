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
package com.cubeia.firebase.service.clientreg.state;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.plugin.clientlogout.ClientClosedReason;
import com.cubeia.firebase.api.plugin.clientlogout.ClientLogoutListenerService;
import com.cubeia.firebase.api.service.ServiceRegistry;

/**
 * This class is responsible for notifying any deployed client logout plugin service.
 *
 * If no plugin service is deployed we will simply not notify anyone.
 * 
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class ClientLogoutReporter implements ClientLogoutListenerService {

	private static transient Logger log = Logger.getLogger(ClientLogoutReporter.class);
	
	private final ServiceRegistry registry;
	
	/**
	 * Constructor with injected service registry. The registry
	 * is need for looking up deployed services.
	 * 
	 * @param registry
	 */
	public ClientLogoutReporter(ServiceRegistry registry) {
		this.registry = registry;
	}
	
	/**
	 * Report a client closed event to listeners if applicable.
	 */
	public void clientClosed(int playerId, ClientClosedReason reason) {
		ClientLogoutListenerService service = registry.getServiceInstance(ClientLogoutListenerService.class);
		if (service != null) {
			try {
				service.clientClosed(playerId, reason);
			} catch (Throwable th) {
				log.error("Failed to notify Client Logout Listener plugin service.", th);
			}
		}
	}
		
	
}
