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
package com.cubeia.firebase.api.plugin.sysinfo;

import com.cubeia.firebase.api.action.local.SystemInfoResponseAction;
import com.cubeia.firebase.api.service.Contract;

/**
 * Plugin interface for service that will handle all system info request actions
 * sent by clients. The implementing service will execute locally on the 
 * same node as the client.
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface SystemInfoQueryService extends Contract {
	
	/**
	 * Handle a system info query request. 
	 * This will be called in an asynchronously manner in order to keep
	 * (possible) distributed calls from blocking the gateway's IO threads.
	 * 
	 * The response action will be prepopulated with all fields except parameters.
	 * If you change a defined field (e.g. players) then this change will propagate
	 * to the client. 
	 * 
	 * @param action, the prepopulated response action
	 * @return A new or the modified system info response action
	 */
	public SystemInfoResponseAction appendResponseData(SystemInfoResponseAction action);
	
}
