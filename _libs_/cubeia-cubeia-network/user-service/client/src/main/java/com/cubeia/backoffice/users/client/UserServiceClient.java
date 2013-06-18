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

import java.util.Map;

import com.cubeia.backoffice.users.api.UserOrder;
import com.cubeia.backoffice.users.api.dto.AuthenticationResponse;
import com.cubeia.backoffice.users.api.dto.CreateUserRequest;
import com.cubeia.backoffice.users.api.dto.CreateUserResponse;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.api.dto.UserQueryResult;
import com.cubeia.backoffice.users.api.dto.UserStatus;

/**
 * 
 * @author Fredrik
 */
public interface UserServiceClient {

	/**
	 * 
	 * @param password 
	 * @param username 
	 * @param userId 
	 * @return 
	 */
	AuthenticationResponse authenticate(Long operatorId, String username, String password);
	
	AuthenticationResponse authenticateSessionToken(String sessionToken);
	
	void invalidateSessionToken(Long userId);

	String getUsername(Long userId);
	
	User getUserById(Long userId);

    User getUserByExternalId(String externalId, Long operatorId);

	Map<String, String> getUserAttributes(Long userId);
	
	String getUserAttribute(Long userId, String key);

	UserQueryResult findUsers(Long userId, String name, int offset, int limit, UserOrder sortOrder, boolean ascending);


	void setUserStatus(Long userId, UserStatus removed);

	void setUserAttribute(Long userId, String key, String attribute);

	void updateUser(User user);

	void updatePassword(Long userId, String password1);

	CreateUserResponse createUser(CreateUserRequest createUserData);

	void setBaseUrl(String baseUrl);
	
	String getBaseUrl();
	
	/**
	 * Returns "pong" if the service is up and running.
	 * @return "pong"
	 */
	String ping();

    void setUserAvatarId(Long userId, String avatarId);

    String getUserAvatarId(Long userId);
}
