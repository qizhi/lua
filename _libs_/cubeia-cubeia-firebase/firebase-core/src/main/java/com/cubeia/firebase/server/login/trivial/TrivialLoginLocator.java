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
package com.cubeia.firebase.server.login.trivial;

import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.login.LoginLocator;
import com.cubeia.firebase.api.service.ServiceRegistry;

/**
 * Always returns a TrivialLoginHandler
 *
 * @author Fredrik
 */
public class TrivialLoginLocator implements LoginLocator {

	/**
	 * Will return a TrivialLoginHandler. Every time.
	 */
	public LoginHandler locateLoginHandler(LoginRequestAction request) {
		return new TrivialLoginHandler();
	}

	/** The Trivial Login Locator does not care about other services */
	public void init(ServiceRegistry serviceRegistry) {
	    // Do nothing.
    }

	
}
