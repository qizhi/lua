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
package com.cubeia.backoffice.users.api.dto;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bulk user creation request.
 * @author w
 *
 */
@XmlRootElement(name="CreateMultipleUsersRequest")
public class CreateMultipleUsersRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<CreateUserRequest> users = new LinkedList<CreateUserRequest>();

	/**
	 * List of users to create.
	 * @return list of users
	 */
	@XmlElement(name = "users")
	public List<CreateUserRequest> getUsers() {
		return users;
	}
	
	public void setUsers(List<CreateUserRequest> users) {
		this.users = users;
	}


}
