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
package com.cubeia.firebase.service.playerinfo.trivial;

import com.cubeia.firebase.api.action.Status;
import com.cubeia.firebase.api.action.local.PlayerQueryRequestAction;
import com.cubeia.firebase.api.action.local.PlayerQueryResponseAction;
import com.cubeia.firebase.api.plugin.playerlookup.PlayerLookupService;
import com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService;

/**
 * Simplest form of player lookup.
 * Will be used if no specific player lookup service is deployed.
 *
 * @author Fredrik
 */
public class TrivialPlayerLookup implements PlayerLookupService {
	
	private final PublicClientRegistryService registry;

	/**
	 * Constructor with the public client registry injected.
	 * 
	 * @param registry
	 */
	public TrivialPlayerLookup(PublicClientRegistryService registry) {
		this.registry = registry;
	}
	
	/**
	 * Handle the request.
	 * We will only lookup the screenname of the player.
	 * 
	 */
	public PlayerQueryResponseAction handleRequest(PlayerQueryRequestAction request) {
	    boolean found = registry.isLoggedIn(request.getPlayerid());
		String screenname  = registry.getScreenname(request.getPlayerid());
		
		// Create response setting no data
		PlayerQueryResponseAction response = new PlayerQueryResponseAction(request.getPlayerid());
		response.setScreenname(screenname);
		if (!found)  {
		    response.setStatus(Status.DENIED);
		}
		
		return response;
	}

}
