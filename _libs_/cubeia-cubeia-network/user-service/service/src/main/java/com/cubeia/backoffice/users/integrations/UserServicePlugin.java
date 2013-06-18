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

package com.cubeia.backoffice.users.integrations;

import com.cubeia.backoffice.users.api.dto.CreationStatus;
import com.cubeia.backoffice.users.api.dto.User;

public interface UserServicePlugin {
	
	/**
	 * Called before a User has been created and persisted.
	 * Changes to the user object will be persisted and will
	 * propagate in the reply to the caller. User id will not be set.
	 */
	public void beforeCreate(User user);
	
	/**
	 * Called immediately after the user has been created, but before
	 * the call returns. 
	 */
	public void afterCreate(CreationStatus status, User user);
	
	/**
	 * Called immediately before the user is updated. Changes to the 
	 * user object will be persisted and will propagate in the reply to the caller.
	 */
	public void beforeUpdate(User user);
	
	/**
	 * Called immediately after the user has been updated, but before
	 * the call returns. 
	 */
	public void afterUpdate(User user);

	
	/**
	 * Called immediately after a user is deleted. 
	 */
	public void afterDeletion(Long userId);
}
