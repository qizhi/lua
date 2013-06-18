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
import com.cubeia.firebase.api.mtt.model.MttRegisterResponse;
import com.cubeia.firebase.api.mtt.model.MttRegistrationRequest;


/**
 * <p>Intercept events regarding players in the tournament
 * player registry.</p>
 * 
 * <p>Use this interface to provide your own logic surrounding 
 * tournemant registration. Usually you want to check some constraints
 * and then deduct funds (registration fees) before allowing a registration.
 * This interface allows you to do this.</p>
 *
 * @author Fredrik
 */
public interface PlayerInterceptor {

	/**
	 * <p>A player wants to register with the tournament.</p>
	 * 
	 * <p>The returned response will be used to see if the player
	 * is allowed or not to join the tournament.</p>
	 * <p>
	 * Note that it is allowed to call register on the same player multiple times 
	 * on a tournament. If you want to send an error to the client when registering
	 * more than once return a denied or failure response here.
	 * </p>
	 * 
	 * @param instance the tournament instance
	 * @param request the player
	 * @return MttRegisterResponse, the response
	 */
	MttRegisterResponse register(MttInstance instance, MttRegistrationRequest request);

	/**
	 * <p>A player wants to unregister from the tournament.</p>
	 * 
	 * <p>The returned response will be used to see if the player
	 * is allowed or not to leave the tournament.</p>
	 * 
	 * @param instance the tournament instance
	 * @param pid, player id
	 * @return MttRegisterResponse, the response
	 */
	MttRegisterResponse unregister(MttInstance instance, int pid);
	
}
