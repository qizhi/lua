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
package com.cubeia.backoffice.users.dao;

import java.util.Collection;
import java.util.List;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.entity.User;
import com.cubeia.backoffice.users.entity.UserAttribute;
import com.cubeia.backoffice.users.entity.UserInformation;
import com.cubeia.backoffice.users.entity.UserStatus;
import com.cubeia.backoffice.users.entity.UserType;
import com.cubeia.backoffice.users.manager.UserManager;

/**
 * DAO interface for accessing persisted User objects. 
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface UserDAO {
	
	/**
	 * Get a User by user id.
	 * 
	 * @param id
	 * @return User or null if not found.
	 */
	public User getById(Long id);
	
	/**
	 * Add a new user to the storage area.
	 * 
	 * @param user
	 * @throws RunTimeException if the information is insufficient or wrong
	 */
	public void persist(User user);

	public void delete(User user);

    public void merge(User user);
    
	/**
	 * Find a single user by username.
	 * 
	 * @param user, not null
	 * @param operatorId, not null
	 * @return User or null if not found.
	 */
	public User getByUserName(String user, Long operatorId);

	
	/**
	 * Returns a list of users matching the given search criterias. If a criteria is
	 * null it works as a wildcard.
	 * @param userId user id, null for all
     * @param name User/screen name, first name or last name. Null for all.
	 * @param offset result set offset
	 * @param limit result set limit
	 * @param includedStatuses statuses to include, null means all statuses
	 * @param order result sort order
	 * @param ascending true for ascending order, false for descending
	 * @return a list of matching users
	 */
	public List<User> findUsers(Long userId, Long operatorId, String name, Collection<UserStatus> includedStatuses, 
		int offset, int limit, UserOrder order, boolean ascending);

	/**
	 * Count users matching the given query. See {@link #findUsers(Long, String, int, int, Set<UserStatus>)}.
	 * @param userId user id, null for all
	 * @param screenName user name, null for all
	 * @return number of matching users
	 */
    public int countUsers(Long userId, Long operatorId, String screenName, Collection<UserStatus> includedStatuses);

	public User getByExternalId(String externalId, Long operatorId);

	/**
	 * Finds users by {@link UserInformation} example. See {@link UserManager#findUsersByExample(UserInformation)}.
	 * Removed users will not be returned.
	 * @param ui usre information example
	 * @return result collection, never null
	 */
    public Collection<User> findUsersByExample(UserInformation ui, Long operatorId);

    public void delete(UserAttribute attrib);

    /**
     * Find users by attribute key.
     * @param key key, cannot be null
     * @return matching users
     */
    public Collection<User> findUsersByAttributeKey(String key);

    /**
     * Find all users having or not having ONE of the provided attibute keys
     * @param notHavingAttr - true = not having, false = having
     * @param attributeKeys - list of attribute keys
     * @return collection of unique userId's  
     */
	public Collection<Long> findUsersHavingAttributes(boolean notHavingAttr, String ...attributeKeys);

	/**
	 * Finds all users having a specific attribute set with a specific value
	 * @param key - attribute key
	 * @param value - attribute value
	 * @return collection of unique userId's
	 */
	public Collection<Long> findUsersHavingAttributeValue(String key, Object value);

	/**
	 * Get all users from a collection if userId's
	 * @param userIds
	 * @return collection of users
	 */
	public Collection<User> getUsersWithIds(Collection<Long> userIds);

	Collection<User> getUsersWithUserType(Collection<UserType> userTypes);

}
