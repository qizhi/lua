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
package com.cubeia.firebase.api.service;

import com.cubeia.firebase.api.action.service.ServiceAction;

/**
 * This interface should be implemented by services wishing to be
 * available within the routing system to receive service events from 
 * the rest of the system. 
 * 
 * <p>Implementing services should be aware that this interface works much like
 * the message driven enterprise bean interface. It's accessor method should
 * be able to work asynchronously.
 * 
 * <p>The contract of a service does not have to extend this interface, only the
 * underlying service implementation does.
 * 
 * <p>Unless ordinary dependencies, the outgoing router is set via a usual 
 * "setter" method and not via the service context. This is because a routable
 * service implicitly depends on the Firebase message bus, which in itself is
 * a service. The {@link #setRouter(ServiceRouter)} method will be invoked after
 * initialization but before starting.
 * 
 * @author Larsan
 */
public interface RoutableService {

	/**
	 * This method gives the service access to a router for service 
	 * events. This method will be invoked after initialization but before 
	 * starting.
	 * 
	 * @param router Service router, never null
	 */
	public void setRouter(ServiceRouter router);
	
	
	/**
	 * @param e Received service action, never null
	 */
	public void onAction(ServiceAction e);

}
