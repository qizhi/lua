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
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;

/**
 * Uses password as id, so you better send in an int as password! =)
 * This Login Handler should only be used for testing purposes.
 *
 * @author Fredrik
 */
public class TrivialLoginHandler implements LoginHandler {

	/**
	 * Parses playerid from password.
	 */
	public LoginResponseAction handle(LoginRequestAction request) {
		LoginResponseAction response;
		try {
			int playerid = Integer.parseInt(request.getPassword());
			response = new LoginResponseAction(true, request.getUser(), playerid);
			
			
		} catch (NumberFormatException e) {
			response = new LoginResponseAction(false, request.getUser(), -1);
			response.setErrorMessage("Password must be an integer");
		}
		
		return response;
	}

}
