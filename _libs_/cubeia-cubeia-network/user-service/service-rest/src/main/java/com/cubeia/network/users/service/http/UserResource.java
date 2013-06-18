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

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.cubeia.backoffice.users.api.dto.*;
import org.apache.log4j.Logger;
import org.perf4j.aop.Profiled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.users.UserService;

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
public class UserResource {

	private Logger log = Logger.getLogger(getClass());
	
	@Autowired
	private UserAttributesResource userAttributesResource;
	
	@Autowired
	private UserService userService;

	private User user;

	public void setUser(User user) {
		this.user = user;
	}

	/* ---------------------------------
	 * DELEGATE RESOURCES
	 * ---------------------------------*/
	
	@Path("attributes")
	@Profiled
	public UserAttributesResource getAttributes() {
		userAttributesResource.setUser(user);
		return userAttributesResource;
	}

	
	/* ---------------------------------
	 * GET METHODS
	 * ---------------------------------*/

	/**
	 * Get a user by id.
	 * @return the user
	 */
	@GET
	@Profiled
	public User getUserById() {
		return user;
	}
	
	/**
	 * Get a user's status.
	 * @return
	 */
	@Path("status")
	@GET
	@Produces({MediaType.TEXT_PLAIN})
	@Profiled
	public String getUserStatus() {
		return user.getStatus().name();
	}
	
	/**
	 * Get a user's user name.
	 * @return user name
	 * @throws IOException
	 */
	@Path("username")
	@GET
	@Produces({MediaType.TEXT_PLAIN})
	@Profiled
	public String getUsername() {
		return user.getUserName();
	}

    /**
     * Get a user's avatar Id.
     * @return avatar Id
     * @throws IOException
     */
    @Path("avatarid")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    @Profiled
    public String getAvatarId() {
        return user.getUserInformation().getAvatarId();
    }
	
	/* ---------------------------------
	 * POST METHODS
	 * ---------------------------------*/

	/* ---------------------------------
	 * PUT METHODS
	 * ---------------------------------*/

	/**
	 * Update a user.
	 * @param updated the updated data
	 * @return result
	 */
	@PUT
	@Profiled
	public Response updateUser(User updated) {
		updated.setUserId(user.getUserId());
		updated.setOperatorId(user.getOperatorId());
		log.debug("New user: "+updated);
		userService.updateUser(updated);
		return Response.status(Response.Status.OK).build();
	}

	/**
	 * Set the status of a user.
	 * @param newStatus status to set
	 * @return result
	 */
	@Path("status")
	@PUT
	@Profiled
	public Response setUserStatus(ChangeUserStatusRequest newStatus) {
		userService.setUserStatus(user.getUserId(), newStatus.getUserStatus());
		return Response.status(Response.Status.OK).build();
	}

    /**
     * Set the avatar Id of a user.
     * @param avatarIdRequest new avatar Id request
     * @return result
     */
    @Path("avatarid")
    @PUT
    @Profiled
    public Response setUserAvatarId(ChangeUserAvatarIdRequest avatarIdRequest) {
        userService.setUserAvatarId(user.getUserId(), avatarIdRequest.getUserAvatarId());
        return Response.status(Response.Status.OK).build();
    }

    /**
	 * Update a user's password.
	 * @param newPassword new password 
	 * @return result
	 */
	@Path("password")
	@PUT
	@Profiled
	public Response updatePassword(ChangeUserPasswordRequest newPassword) {
		userService.updatePassword(user.getUserId(), newPassword.getPassword());
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Update a user's password if the current password is correct.
	 * @param password password change request
	 * @return result
	 */
	@Path("password/verifyold")
	@PUT
	@Profiled (tag="updatePasswordWOld")
	public Response updatePassword(ChangeUserPasswordWithVerificationRequest password) {
		try {
			userService.updatePasswordWithVerification(user.getUserId(), password.getPassword(), password.getOldPassword());
		}catch (Exception e) {
			log.error("Failed to change pwd for user " + user.getUserName());
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		return Response.status(Response.Status.OK).build();
	}

	/* ---------------------------------
	 * DELETE METHODS
	 * ---------------------------------*/

	/**
	 * Delete a user. Note that this method removes all records of the user. There is no
	 * way to resurrect a deleted user.
	 * The preferred way to remove a user is to update it's status to removed. 
	 * @return result
	 */
	@DELETE
	@Profiled
	public Response deleteUser() {
		userService.deleteUser(user.getUserId());
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Remove and invalidate any session token set for this user. This should
	 * typically be called when a user is forcibly logged out from the system.
	 * 
	 * @return response, 200 if ok.
	 */
	@Path("session")
	@DELETE
	@Profiled
	public Response invalidateSessionToken() {
		userService.invalidateUserSessionToken(user);
		return Response.status(Response.Status.OK).build();
	}

	/*
	@Path("authenticate")
	@OPTIONS	
	public Response getOptions(){
		return Response.ok()
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
				.header("Access-Control-Allow-Headers", "Content-Type")
				.header("Access-Control-Max-Age", "86400")
				.build();
	 */
}