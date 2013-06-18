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

import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceRegistry;

/**
 * Lookup the correct login handler for a login request. The login locator
 * extends contract and is possible to mount as a service.
 * 
 * <p>From Firebase 1.9 it will only be possible to mount a login locator as
 * a service. And as such, the {@link #init(ServiceRegistry)} method will be 
 * removed. 
 *
 * @author Fredrik
 */
public interface LoginLocator extends Contract {
	
	/**
	 * This method is called if the login locator is configured rather than
	 * mounted as a service. Since the service has access to the system it is recommended
	 * that service mounting is preferable.
	 * 
	 * <p><b>NB:</b>Please note that this method will not be called if the locator 
	 * is mounted as a service.
	 * 
	 * <p><b>NB 2:</b>This method will be removed in Firebase 1.9
	 * 
	 * @param serviceRegistry Service registry, never null
	 * @deprecated This method will be removed in Firebase 1.9
	 */
	@Deprecated
	public void init(ServiceRegistry serviceRegistry);
	
	public LoginHandler locateLoginHandler(LoginRequestAction request);
	
}
