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
package com.cubeia.firebase.api.service.router;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceRouter;

/**
 * <p>Provides a router for sending actions to tables, tournaments
 * and Clients.</p>
 * 
 * <p>This service will allow for asynchronous messaging, i.e.
 * send actions outside of event transactions. Messages sent through the 
 * router will not be apart of any transaction and therefore not rolled back.</p>
 *
 * <p>Use the ServiceRouter from the getRouter method to dispatch actions to 
 * tables, services and players.</p>
 * 
 * <p>Use the dispatchToPlayer method if you want to send GameActions
 * asynchronously to clients. Note that this method will not be able to check
 * on the client session state at any table but will deliver the action as is.
 * If the client is disconnected then the action will be dropped silently.</p>
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface RouterService extends Contract {

	/**
	 * Returns a Service Router which can be used for dispatching actions within
	 * the system.
	 * 
	 * @return ServiceRouter instance
	 */
	public ServiceRouter getRouter();
	
}
