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
package com.cubeia.firebase.service.localservice;

import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.LocalServiceAction;
import com.cubeia.firebase.api.service.Contract;

public interface LocalServiceManagerContract extends Contract {

	/**
	 * Handle local action data from a client. 
	 * This will be called in an asynchronously manner in order to keep
	 * (possible) distributed calls from blocking the gateway's IO threads.
	 * 
	 * @param request
	 * @return
	 */
	public void handleAction(LocalServiceAction action, LocalActionHandler loopback);

}