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
package com.cubeia.firebase.server.login;

import com.cubeia.firebase.api.action.local.LocalActionHandler;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.util.ServiceMBean;

/**
 * Dispatches login requests to login handlers.
 * The dispatching will be done in an asynchronous manner,
 * i.e. the handleLoginRequest will be non-blocking. 
 * 
 * @author Fredrik
 */
public interface LoginManager extends ServiceMBean {
	
	/**
	 * Handle a login request asynchronous.
	 * The implementation will dispatch the request and send
	 * a response notification.
	 * 
	 * @param request
	 */
	public void handleLoginRequest(LoginRequestAction request, LocalActionHandler loopback);
	
}
