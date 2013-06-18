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
package com.cubeia.backoffice.users;

import java.util.Collection;
import java.util.List;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.api.dto.CreateUserRequest;
import com.cubeia.backoffice.users.api.dto.CreateUserResponse;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserInformation;
import com.cubeia.backoffice.users.api.dto.UserQueryResult;
import com.cubeia.backoffice.users.api.dto.UserStatus;
import com.cubeia.backoffice.users.api.dto.UserType;

/**
 * This is the business layer facade for the User Service.
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface UserService {

	/**
	 * Get a user by unique id. 
	 * Will return null if the user was not found.
	 * 
	 * @param userId
	 * @return User or null if not found or removed.
	 */
	public User getUserById(Long userId);
	
	/**
	 * Get a user by unique id external id. 
	 * Will return null if the user was not found.
	 * 
	 * @param externalId
	 * @param operatorId
	 * @return User or null if not found or removed.
	 */
	public User getUserByExternalId(String externalId, Long operatorId);
	
	/**
	 * Returns a list of users matching the given search criterias. If a criteria is
	 * null it works as a wildcard. Users marked as removed will not be returned.
	 * @param userId user id, null for all
	 * @param name User/screen name, first name or last name. Null for all.
	 * @param offset result set offset
	 * @param limit result set limit
	 * @param order sort order of the result, null for no ordering
	 * @param ascending true for ascending, false for descending
	 * @return a user query result object
	 */
	public UserQueryResult findUsers(Long userId, Long operatorId, String name, int offset, int limit,UserOrder order, boolean ascending);
	
	/**
	 * User names are constraints that are unique
	 * so this will always only return one User.
	 * @param userName user name
	 * @param operatorId operator id
	 * @return User or null if no or duplicate users found. Null if the user is removed.
	 */
	public User getUserByUserName(String userName, Long operatorId);
	
	/**
	 * Create a new user in the system. Note that the creation date of the user will not be automatically set, it is the
	 * client's responsibility to set the creation date.
	 * 
	 * @param user user creation data
	 * @return The created User result
	 */
	public CreateUserResponse createUser(CreateUserRequest user);
	
	/**
	 * Create users. See {@link #createUser(CreateUserRequest)}.
	 * @param users list of users to create
	 * @return the created users
	 */
	public List<CreateUserResponse> createUsers(List<CreateUserRequest> users);
	
	/**
     * Check if the users exists (for the given operator), the password is correct and the user has status 
     * {@link UserStatus#ENABLED}.
     * Note that the user's last login date will not be updated, it is the client's responsibility to update that field.
	 * @param userName the user name
	 * @param password the user's password
	 * @param operatorId operator id
	 * @return the user service internal user id if successful, null if authentication failed.
	 */
	public Long authenticateUser(String userName, String password, Long operatorId) ;
	
	/**
	 * <p>Authenticate the given token. The token must be a token set from a regular authentication
	 * which has still been invalidated or exceeded its time to live. Time to live can be configured
	 * through user-service.properties and is set as milliseconds. Example setting time to live to 
	 * 12 hours:</p>
	 * 
	 * <pre>user.service.session.timetolive=43200000</pre>
	 * 
	 * @param sessionToken
	 * @return
	 */
	public Long authenticateUserBySessionToken(String sessionToken);

	/**
	 * Update the user data. The user's password will not be updated, use {@link #updatePassword(Long, String)} to
	 * change password.
	 * @param user new user data
	 */
	public void updateUser(User user);

    /**
     * Gets user avatar Id
     * @param userId user Id
     */
    public String getUserAvatarId(Long userId);

    /**
     * Sets user avatar Id
     * @param userId user Id
     * @param avatarId avatar Id
     */
    public void setUserAvatarId(Long userId, String avatarId);

    /**
	 * Update the given user's status.
	 * @param userId user id
	 * @param newStatus new status
	 */
    public void setUserStatus(Long userId, UserStatus newStatus);

    /**
     * Updates the user's password.
     * @param userId the user to update
     * @param newPassword the new password
     */
    public void updatePassword(Long userId, String newPassword);

    /**
     * Updates the user's password if the provided old password matches the users current pwd.
     * @param userId the user to update
     * @param newPassword the new password
     * @param oldPassword the password used before the update
     */
    void updatePasswordWithVerification(Long userId, String newPassword, String oldPassword);
    
    /**
     * Find users by {@link UserInformation} example. Null values and identifier are ignored.
     * @param ui user information example
     * @return collection of matching users
     */
    public Collection<User> findUsersByExample(UserInformation ui);

    /**
     * Find users by {@link UserInformation} example. Null values and identifier are ignored.
     * If operatorId is provided the search will only contain users with that operatorId. 
     * @param ui user information example
     * @param operratorId
     * @return collection of matching users
     */
    public Collection<User> findUsersByExample(UserInformation ui, Long operratorId);
    
    /**
     * Find users by attribute key.
     * @param key key, cannot be null
     * @return matching users
     */
    public Collection<User> findUsersByAttributeKey(String key);
    
    /**
     * This is a shortcut for setting the user status to "removed"
     * and does not actually remove any data from the system.
     * 
     * @param userId Id of user to set status for, must not be null
     */
    public void setUserRemoved(Long userId);

    /**
     * This method removes the given user from the service. It will delete
     * all records, please use with care...
     * 
     * @param userId Id of user to remove, must not be null
     */
    public void deleteUser(Long userId);
    
    /**
     * Find all users having one of the provided attributes set
     * @param keys - attribute keys
     * @return collection of userId's
     */
    public Collection<Long> findUsersHavingAnyOfAttributes(String ... keys);

    /* REMOVED BECAUSE BUGGY. IF USER HAVE MORE THEN ONE ATTRIBUTE 
     * IT WILL APPEAR IN THE RESULTSET EVEN WHEN IT SHOULDN'T 
     * 
     * Find all users missing at least one of the provided attribute keys
     * @param attribute keys
     * @return collection of userId's
    public Collection<Long> findUsersNotHavingAttributes(String ... keys);
     */

    /**
	 * Finds all users having a specific attribute set with a specific value
	 * @param key - attribute key
	 * @param value - attribute value
	 * @return collection of unique userId's
	 */
	public Collection<Long> findUsersByAttributeValue(String key, String value);

	/**
	 * Get all users from a collection if userId's
	 * @param userIds
	 * @return collection of users
	 */
	public Collection<User> getUsersWithIds(Collection<Long> userIds);
	
	public Collection<User> findUsersWithUserType(UserType... userTypes);

	public void invalidateUserSessionToken(User user);
}
