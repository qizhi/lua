/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.backoffice.users.client;

import com.cubeia.backoffice.users.api.dto.AuthenticationResponse;

public class Runner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UserServiceClient userService = new UserServiceClientHTTP("http://localhost:9090/user-service-rest/rest");
		
//		User user = userService.getUserById(3L);
//		System.out.println("User read: "+user);
//		user.getAttributes().put("test2", "TEST2");
//		userService.updateUser(user);
		
		/*
		System.out.println("---");
		
		AuthenticationResponse authenticate = userService.authenticate(1l, "u1", "pwd");
		System.out.println("Auth: "+authenticate);
		
		System.out.println("---");
		
		Map<String, String> userAttributes = userService.getUserAttributes(1l);
		System.out.println("User attributes read: "+userAttributes);
		
		System.out.println("---");
		
		String username = userService.getUsername(1l);
		System.out.println("Username: "+username);
		
		System.out.println("---");
		
		UserQueryResult queryResult = userService.findUsers(null, null, 0, 10, UserOrder.ID, true);
		System.out.println("Query Result["+queryResult.getTotalQueryResultSize()+"] : "+queryResult);
		*/
		
		AuthenticationResponse authenticate = userService.authenticate(1l, "banan", "banan");
		System.out.println("Primary Auth: "+authenticate);
		
		AuthenticationResponse tokenAuth = userService.authenticateSessionToken(authenticate.getSessionToken());
		System.out.println("Toiken Auth: "+tokenAuth);
		
		userService.invalidateSessionToken(authenticate.getUserId());
		
		AuthenticationResponse tokenAuth2 = userService.authenticateSessionToken(authenticate.getSessionToken());
		System.out.println("Toiken Auth 2: "+tokenAuth2);
		
	}

}
