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

import java.util.Date;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import com.cubeia.backoffice.users.api.dto.AttributeKeyList;
import com.cubeia.backoffice.users.api.dto.AuthenticationRequest;
import com.cubeia.backoffice.users.api.dto.AuthenticationResponse;
import com.cubeia.backoffice.users.api.dto.AuthenticationTokenRequest;
import com.cubeia.backoffice.users.api.dto.CreateMultipleUsersRequest;
import com.cubeia.backoffice.users.api.dto.CreateMultipleUsersResponse;
import com.cubeia.backoffice.users.api.dto.CreateUserRequest;
import com.cubeia.backoffice.users.api.dto.CreateUserResponse;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserIdList;
import com.cubeia.backoffice.users.api.dto.UserInformation;
import com.cubeia.backoffice.users.api.dto.UserList;
import com.cubeia.backoffice.users.api.dto.UserQuery;
import com.cubeia.backoffice.users.api.dto.UserQueryResult;
import com.cubeia.backoffice.users.api.dto.UserType;

/**
 * Handles resources not coupled to a single, defined user.
 *  
 * @author Fredrik
 */
@Path("/users")
@Component
@Scope("request")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UsersResource {

	private static final String SCREENNAME = "screenname";
	private static final String EXTERNAL_USERNAME = "externalUsername";

	private Logger log = Logger.getLogger(getClass());

	@Autowired
	private UserService userService;


	/* ---------------------------------
	 * GET METHODS
	 * ---------------------------------*/

	/**
	 * Returns all system users. 
	 * @return system users
	 */
	@Path("echo")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String echo() {
		log.info("Echo called");
		return "TIME "+new Date();
    }
	
	/**
	 * Returns all system users. 
	 * @return system users
	 */
	@Path("system")
    @GET
	@Profiled
    public UserList getAllSystemUsers() {
		return new UserList(userService.findUsersWithUserType(UserType.SYSTEM));
    }
	
	/**
	 * Returns all operators.
	 * @return operators
	 */
	@Path("operators")
    @GET
	@Profiled
    public UserList getAllOperatorUsers() {
		return new UserList(userService.findUsersWithUserType(UserType.OPERATOR));
    }
	
	/**
	 * Returns all users that are not of type {@link UserType#USER}.
	 * @return result
	 */
	@Path("nonusers")
    @GET
	@Profiled
    public UserList getAllNonUserUsers() {
		return new UserList(userService.findUsersWithUserType(UserType.SYSTEM, UserType.OPERATOR));
    }
	

	/* ---------------------------------
	 * POST METHODS
	 * ---------------------------------*/

	/**
	 * Create a new user.
	 * @param userData user to create
	 * @return result
	 */
	@POST
	@Profiled
	public CreateUserResponse createUser(CreateUserRequest userData) {
		return userService.createUser(userData);
	}

	/**
	 * Authenticate user.
	 * 
	 * Set screenname depending on available information.
	 * 
	 * 1. Use attributes.screenname
	 * 2. Use attributes.externalUsername
	 * 3. Use user.userName
	 * 
	 * @param request authentication request
	 * @return result
	 */
	@Path("authenticate")
	@POST
	@Profiled
	public AuthenticationResponse authenticateUser(AuthenticationRequest request) {
		Long userId = userService.authenticateUser(request.getUserName(), request.getPassword(), request.getOperatorId());
		if (userId == null) {
			// Authentication failed for some reason
			// TODO: Return a VO with detailed response
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		}
		User user = userService.getUserById(userId);
		AuthenticationResponse response = new AuthenticationResponse(userId);
		response.setAuthenticated(true);
		response.setScreenname(getScreenname(user));
		response.setSessionToken(user.getAttributes().get("SESSION_TOKEN"));
		return response;
	}
	
	@Path("authenticate/token")
	@POST
	@Profiled
	public AuthenticationResponse authenticateUserBySessionToken(AuthenticationTokenRequest request) {
		Long userId = userService.authenticateUserBySessionToken(request.getSessionToken());
		if (userId == null) {
			// Authentication failed for some reason
			// TODO: Return a VO with detailed response
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		}
		
		
		User user = userService.getUserById(userId);
		AuthenticationResponse response = new AuthenticationResponse(userId);
		response.setAuthenticated(true);
		response.setScreenname(getScreenname(user));
		response.setSessionToken(user.getAttributes().get("SESSION_TOKEN"));
		return response;
	}

	
	private String getScreenname(User user) {
		Map<String, String> attr = user.getAttributes();
		if (attr.containsKey(SCREENNAME)) {
			 return attr.get(SCREENNAME);
		} else if (attr.containsKey(EXTERNAL_USERNAME)) {
			return attr.get(EXTERNAL_USERNAME);
		} 
		return user.getUserName();
	}


	/**
	 * Bulk create users.
	 * @param usersRequest users to create
	 * @return result
	 */
	@Path("multiple")
    @POST
	@Profiled
    public CreateMultipleUsersResponse createUsers(CreateMultipleUsersRequest usersRequest) {
	    // FIXME: Fails with 'detached entity passed to persist'
		log.info("Creating multiple users: "+usersRequest.getUsers().size());
    	return new CreateMultipleUsersResponse(userService.createUsers(usersRequest.getUsers()));
    }




	/* ---------------------------------
	 * PUT METHODS
	 * ---------------------------------*/

	/**
	 * Search users by query.
	 * @param query query
	 * @return matching users
	 */
	@Path("search/query")
    @PUT
    @Profiled
    public UserQueryResult findUsers(UserQuery query) {
		//TODO: add operatorId to query and doc
		return userService.findUsers(query.getUserId(), null, query.getUserName(), query.getQueryOffset(), query.getQueryLimit(), query.getOrder(), query.isAscending());
    }

	/**
	 * Find users by example. Users matching the non null elements in the example will be returned.
	 * @param example 
	 * @return matching users
	 */
	@Path("search/info")
    @PUT
	@Profiled
    public UserList findUsersByExample(UserInformation example) {
        return new UserList(userService.findUsersByExample(example));
    }
	
	/**
	 * Find users having attributes with the given keys.
	 * @param keys attribute key list
	 * @return matching user id list
	 */
	@Path("search/attributes")
    @PUT
	@Profiled
	public UserIdList findUsersHavingAttributes(AttributeKeyList keys){
		return new UserIdList(userService.findUsersHavingAnyOfAttributes(keys.getKeys().toArray(new String[]{})));
	}
	
	/**
	 * Find users by attribute key.
	 * @param key key
	 * @return matching users
	 */
	@Path("search/attributes/{key}")
    @PUT
	@Profiled
    public UserIdList findUsersByAttributeKey(@PathParam("key") String key){
		return new UserIdList(userService.findUsersHavingAnyOfAttributes(new String[]{key}));
    }

	/**
	 * Find users by attribute key and value.
	 * @param key key
	 * @param value value
	 * @return matching users
	 */
	@Path("search/attributes/{key}/{value}")
    @PUT
	@Profiled
    public UserIdList findUsersByAttributeValue(@PathParam("key") String key, @PathParam("value") String value){
    	return new UserIdList(userService.findUsersByAttributeValue(key, value));
    }

	/**
	 * Get users by the user id list.
	 * @param userIds user id list
	 * @return users
	 */
	@Path("search/userids")
    @PUT
	@Profiled
    public UserList getUsersWithIds(UserIdList userIds){
    	return new UserList(userService.getUsersWithIds(userIds.getUserIds()));
    }

}
