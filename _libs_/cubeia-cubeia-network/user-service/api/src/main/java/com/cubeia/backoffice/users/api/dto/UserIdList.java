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
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="UserIdList")
public class UserIdList implements Serializable {
	private static final long serialVersionUID = 1L;

	private Collection<Long> userIds = new LinkedList<Long>();

	public UserIdList() {}

	
	public UserIdList(Collection<Long> users) {
		this.userIds = users;
	}

	@XmlElement(name = "userIds")
	public Collection<Long> getUserIds() {
		return userIds;
	}
	
	public void setUserIds(Collection<Long> userIds) {
		this.userIds = userIds;
	}


}
