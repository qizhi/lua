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

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.perf4j.aop.Profiled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.users.UserService;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserAttributes;

/**
 * Handles single, specific user resource.
 * 
 * This component needs to be prototype to avoid concurrency issues.
 * 
 * @author Fredrik
 */
@Component
@Scope("prototype")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserAttributesResource {

	@SuppressWarnings("unused")
	private Logger log = Logger.getLogger(getClass());

	@Autowired
	private UserService userService;

	private User user;

	public void setUser(User user) {
		this.user = user;
	}
	

	/* ---------------------------------
	 * GET METHODS
	 * ---------------------------------*/

	/**
	 * Returns the attributes of a user.
	 * @return the attributes
	 */
	@GET
	@Profiled
	public UserAttributes getAttributes() {
		UserAttributes attributes = new UserAttributes(user.getAttributes());
		return attributes;
	}
	
	/**
	 * Returns a single attribute for a user.
	 * @param key attribute key
	 * @return the attribute value
	 */
	@Path("{key}")
	@GET
	@Produces({MediaType.TEXT_PLAIN})
	@Profiled
	public String getAttribute(@PathParam("key") String key) {
		Map<String,String> attributes = user.getAttributes();
		if (attributes != null) {
			return attributes.get(key);
		} else {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}


	/* ---------------------------------
	 * POST METHODS
	 * ---------------------------------*/

	/**
	 * Set a user attribute.
	 * @param key key
	 * @param value value
	 * @return result
	 */
	@Path("{key}")
	@PUT
	@Consumes({MediaType.TEXT_PLAIN})
	@Profiled
	public Response setAttribute(@PathParam("key") String key, String value) {
		user.getAttributes().put(key, value);		
		userService.updateUser(user);
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Remove a user attribute
	 * @param key key
	 * @return result
	 */
	@Path("{key}")
	@DELETE
	@Consumes({MediaType.TEXT_PLAIN})
	@Profiled
	public Response removeAttribute(@PathParam("key") String key) {
		user.getAttributes().remove(key);		
		userService.updateUser(user);
		return Response.status(Response.Status.OK).build();
	}
	
}
