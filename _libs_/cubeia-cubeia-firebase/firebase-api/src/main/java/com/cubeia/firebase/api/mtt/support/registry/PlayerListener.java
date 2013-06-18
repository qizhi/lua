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
package com.cubeia.firebase.api.mtt.support.registry;

import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;

/**
 * <p>Listen to changes in players and the player registry.</p>
 * 
 * <p>You cannot change the outcome of the event in this class,
 * if you are interested in being able to change the handling
 * of the events, check out the  @link com.cubeia.firebase.api.mtt.support.registry.PlayerInterceptor</p>
 *
 * @author Fredrik
 */
public interface PlayerListener {

	/**
	 * <p>A player has registered with the tournament.</p>
	 * 
	 * @param instance the tournament instance
	 * @param request the player
	 */
	void playerRegistered(MttInstance instance, MttRegistrationRequest request);

	/**
	 * <p>A player has unregistered from the tournament.</p>
	 * @param instance the tournament instance
	 * @param pid player id
	 */
	void playerUnregistered(MttInstance instance, int pid);
	
}
