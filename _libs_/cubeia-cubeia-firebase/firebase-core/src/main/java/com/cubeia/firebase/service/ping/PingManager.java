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
package com.cubeia.firebase.service.ping;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.server.gateway.client.Client;

/**
 * The ping manager is the service which is control of pinging
 * clients in order to detect failures. The ping mechanism sits
 * "below" the usual packet layer is as a result this interface is
 * called by the session handler and the game client directly.
 * 
 * @author larsan
 */
public interface PingManager extends Contract {

	/**
	 * This method should be called when a new game client
	 * has been created and will return a session for that
	 * specific client.
	 * 
	 * @param client Client to get a session for, must not be null
	 * @return A new ping session, never null
	 */
	public PingSession register(Client client);

	/**
	 * This method should be called when a client is destroyed. It
	 * will cancel all scheduled pings and clean up the manager.
	 * 
	 * @param client The client to remove, must not be null
	 */
	public void unregister(Client client);
	
}
