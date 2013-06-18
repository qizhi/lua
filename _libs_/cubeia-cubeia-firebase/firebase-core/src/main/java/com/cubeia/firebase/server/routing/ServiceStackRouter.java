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
package com.cubeia.firebase.server.routing;

import com.cubeia.firebase.api.service.ServiceDiscriminator;
import com.cubeia.firebase.server.event.LocalServiceEvent;
import com.cubeia.firebase.service.messagebus.Sender;

public interface ServiceStackRouter {

	/**
	 * Each service in the system which implements the {@com.cubeia.firebase.api.service.RoutableService} interface
	 * can have an attached sender for asynchronous receival of events. The disciminator given in as an argument
	 * is used to find the wanted service, should the service not be found, or found but not implement the above
	 * interface, null will be returned.
	 * 
	 * @param disc Service discriminator, must not be null
	 * @return The sender for the local service stack, or null if not found
	 */
	public Sender<LocalServiceEvent> getServiceStackSender(ServiceDiscriminator disc);
	
}
