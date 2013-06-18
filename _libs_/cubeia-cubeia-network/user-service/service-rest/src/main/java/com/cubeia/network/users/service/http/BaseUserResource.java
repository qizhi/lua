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
package com.cubeia.network.users.service.http;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.users.UserService;
import com.cubeia.backoffice.users.api.dto.User;

/**
 * Handles single, specific user resource.
 * 
 * @author Fredrik
 */
@Path("/user")
@Component
@Scope("request")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class BaseUserResource {

	// private Logger log = Logger.getLogger(getClass());

	@Autowired
	private UserResource userResource;
	
	@Autowired
	private UserService userService;

	/**
	 * Get user by id.
	 * @param userId user id
	 * @return user sub resource
	 */
	@Path("id/{userId}")
	public UserResource getUserById(@PathParam("userId") Long userId) {
		User user = userService.getUserById(userId);
		checkUserFound(user);
		userResource.setUser(user);
		return userResource;
	}
	/**
	 * Get user by operator id and user name.
	 * @param operatorId operator id
	 * @param userName user name
	 * @return user sub resource
	 */
	@Path("name/{operatorId}/{userName}")
	public UserResource getUserByUserName(@PathParam("operatorId") Long operatorId, @PathParam("userName") String userName) {
		User user =  userService.getUserByUserName(userName, operatorId);
		checkUserFound(user);
		userResource.setUser(user);
		return userResource;
	}

    /**
	 * Get user by operator id and external id.
	 * @param externalId external id
	 * @param operatorId operator id
	 * @return user sub resource
	 */
	@Path("getbyexternalid/{operatorId}/{externalId}")
	public UserResource getUserByExternalId(@PathParam("externalId") String externalId, @PathParam("operatorId") Long operatorId) {
		User user =  userService.getUserByExternalId(externalId, operatorId);
		checkUserFound(user);
		userResource.setUser(user);
		return userResource;
	}
	
	private void checkUserFound(User user) {
		if (user == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}
}