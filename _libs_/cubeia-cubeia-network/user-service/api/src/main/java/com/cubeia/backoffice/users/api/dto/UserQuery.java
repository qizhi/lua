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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.cubeia.backoffice.users.api.UserOrder;

@XmlRootElement(name="UserQuery")
public class UserQuery {
	
    private Long userId;
    private String userName;
    private int queryOffset;
    private int queryLimit;
    private UserOrder order;
    private boolean ascending;
    
    
    public UserQuery() {}
    
    public UserQuery(int offset, int limit, UserOrder order, boolean ascending) {
        this.queryLimit = limit;
        this.queryOffset = offset;
    }

    @XmlElement
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@XmlElement
	public String getUserName() {
		return userName;
	}

	public void setUserName(String screenName) {
		this.userName = screenName;
	}

	@XmlElement
	public int getQueryOffset() {
		return queryOffset;
	}

	public void setQueryOffset(int offset) {
		this.queryOffset = offset;
	}

	@XmlElement
	public int getQueryLimit() {
		return queryLimit;
	}

	public void setQueryLimit(int limit) {
		this.queryLimit = limit;
	}

	@XmlElement
	public UserOrder getOrder() {
		return order;
	}

	public void setOrder(UserOrder order) {
		this.order = order;
	}

	@XmlElement
	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
  
    
}
