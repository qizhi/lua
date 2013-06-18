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
package com.cubeia.backoffice.users.manager;

import java.util.Collection;

import javax.persistence.EntityExistsException;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.domain.UserQueryResultContainer;
import com.cubeia.backoffice.users.entity.User;
import com.cubeia.backoffice.users.entity.UserInformation;
import com.cubeia.backoffice.users.entity.UserStatus;
import com.cubeia.backoffice.users.entity.UserType;

/**
 * This is the business layer facade for the User Service.
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface UserManager {

	/**
	 * Get a user by unique id. 
	 * Will return null if the user was not found or is removed.
	 * 
	 * @param id
	 * @return User or null if not found.
	 */
	public User getUserById(Long id);
	
	/**
	 * Returns a list of users matching the given search criterias. If a criteria is
	 * null it works as a wildcard. Users marked as removed will not be returned.
	 * @param userId user id, null for all
     * @param name User/screen name, first name or last name. Null for all.
	 * @param offset result set offset
	 * @param limit result set limit
	 * @param order ordering of the result, null for no ordering
	 * @param ascending true for ascending, false for descending
	 * @return a container holding the result and meta information about the query
	 */
	public UserQueryResultContainer findUsers(Long userId, Long operatorId, String userName, 
			int offset, int limit, UserOrder order, boolean ascending);
	
	/**
	 * User names are constraints that are unique
	 * so this will always only return one User. Null if not found or removed.
	 * 
	 * @param userName
	 * @param operatorId
	 * @return User or null if no or duplicates found.
	 */
	public User getUserByUserName(String userName, Long operatorId);
	
	/**
	 * Create a new user in the system.
	 * ID of the user mus not be set (should be null).
	 * 
	 * @param name
	 * @return The created User (with id set) or null if not valid
	 * 
	 * @throws EntityExistsException if the username already exists
	 */
	public User createUser(User user);
	
	/**
	 * Permanently remove a user.
	 */
	public boolean deleteUser(Long userId);
	
	/**
	 * Check if the users exists, the password is correct and the user has status {@link UserStatus#ENABLED}.
	 * 
	 * @param userName user name
	 * @param password password
	 * @param operatorId Operator id
	 * @return the user id if successful, null if authentication failed
	 */
	public Long authenticateUser(String userName, String password, Long operatorId);

	/**
	 * Update the given user with the new data. The user's password will not be updated.
	 * @param user updated user data
	 */
	public void updateUser(User user);

	/**
	 * Update the given user's status.
	 * @param userId user id
	 * @param newStatus the new status
	 */
    public void setUserStatus(Long userId, UserStatus newStatus);

    /**
     * Update the given user's password.
     * @param userId the user id
     * @param newPassword the new password (in cleartext)
     */
    public void updatePassword(Long userId, String newPassword);
    
    /**
     * Attempt to load user information using configured lookup strategies.
     * There are no guarantees that the information will be correct.
     * Will return null if not found or problems occured.
     * 
     * @param country
     * @param phoneNumber
     */
    public UserInformation lookupUserInformationFromPhone(String country, String phoneNumber);
    
	/**
	 * Get a user with its external id. Null if not found or removed.
	 * @param externalId
	 * @param operatorId
	 */
	public User getUserByExternalId(String externalId, Long operatorId);
    
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
     * Find all users having one of the provided attributes set
     * @param keys - attribute keys
     * @return collection of userId's
     */
    public Collection<Long> findUsersHavingAttributes(String ... keys);

    /**
     * Find all users missing at least one of the provided attribute keys
     * @param attribute keys
     * @return collection of userId's
     */
    public Collection<Long> findUsersNotHavingAttributes(String ... keys);

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

	Collection<User> getUsersWithUserType(Collection<UserType> userTypes);
    
}
