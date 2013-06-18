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
package com.cubeia.firebase.server.event;

import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.service.ServiceDiscriminator;
import com.cubeia.firebase.api.util.Arguments;

/**
 * An event for service actions.l This is not strictly an event as it 
 * does not extend <em>Event</em> and does not have to be serializable.
 * 
 * @author Larsan
 */
public class LocalServiceEvent {

	private final ServiceDiscriminator disc;
	private final ServiceAction action;

	
	/**
	 * @param disc Service discriminator, must not be null
	 * @param action Service action, must not be null
	 */
	public LocalServiceEvent(ServiceDiscriminator disc, ServiceAction action) {
		Arguments.notNull(disc, "discriminator");
		Arguments.notNull(action, "action");
		this.disc = disc;
		this.action = action;
	}
	
	public ServiceDiscriminator getTargetService() {
		return disc;
	}

	public ServiceAction getAction() {
		return action;
	}
}
