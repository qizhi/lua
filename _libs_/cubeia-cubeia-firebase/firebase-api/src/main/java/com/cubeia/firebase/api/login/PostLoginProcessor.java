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
package com.cubeia.firebase.api.login;

import com.cubeia.firebase.api.service.Contract;

/**
 * This is a so called "plugin service" for reacting on 
 * login/logout events. This service will be called only when an
 * instance is deployed. The methods are called <em>after</em>
 * the event they are associated with has occurred. 
 * 
 * <p>For clarity: a service implementing this interface will be
 * called when a client has been logged in/out on the local node.
 * This means that in scenarios with multiple client nodes, the service will 
 * only get called when a login/logout happens on the current server.
 * 
 * @author Larsan
 */
public interface PostLoginProcessor extends Contract {

	/**
	 * Invoked when a client has been successfully logged in. The
	 * screen name is optional.
	 *  
	 * @param playerId Player id
	 * @param screenName Optional screen name, may be null
	 */
	public void clientLoggedIn(int playerId, String screenName);
	
	/**
	 * Invoked when a client has unexpectedly lost its connection. It
	 * will not be called if the the client is not logged in before the disconnect 
	 * happens. Note that the client might still be seated at tables which are waiting
	 * for the client to come back at the point of the disconnect. 
	 * 
	 * @param playerId Player id
	 */
	public void clientDisconnected(int playerId);
	
	/**
	 * Invoked when a client has successfully logged out via
	 * a logout packet.
	 * 
	 * @param playerId Player id
	 */
	public void clientLoggedOut(int playerId);

}
