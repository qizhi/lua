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
package com.cubeia.firebase.api.plugin.playerlookup;

import com.cubeia.firebase.api.action.local.PlayerQueryRequestAction;
import com.cubeia.firebase.api.action.local.PlayerQueryResponseAction;
import com.cubeia.firebase.api.service.Contract;

/**
 * Plugin interface for service that will handle all local service actions
 * sent by clients. The implementing service will execute locally on the 
 * same node as the client.
 * 
 * @author Fredrik
 */
public interface PlayerLookupService extends Contract {
	
	/**
	 * Handle a player query request. 
	 * This will be called in an asynchronously manner in order to keep
	 * (possible) distributed calls from blocking the gateway's IO threads.
	 * 
	 * @param request
	 * @return
	 */
	public PlayerQueryResponseAction handleRequest(PlayerQueryRequestAction request);
	
}
