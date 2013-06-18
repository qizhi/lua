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
package com.cubeia.firebase.api.plugin.clientlogout;

import com.cubeia.firebase.api.service.Contract;

/**
 * Plugin interface for service that will be notified of clients that are
 * removed from the system. 
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface ClientLogoutListenerService extends Contract {
	
	/**
	 * A client has been removed from the system. 
	 * It is important that the reason is inspected properly since the 
	 * system behavior varies wildly depending on the reason. 
	 * 
	 * You need to match your desired approach based on the client closed reason.
	 * 
	 * @param playerId
	 * @param reason, what are the context for the client removal.
	 * @return
	 */
	public void clientClosed(int playerId, ClientClosedReason reason);
	
}
