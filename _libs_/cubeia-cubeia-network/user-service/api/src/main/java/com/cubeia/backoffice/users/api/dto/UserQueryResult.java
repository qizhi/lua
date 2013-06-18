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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.cubeia.backoffice.users.api.UserOrder;

@XmlRootElement(name="UserQueryResult")
public class UserQueryResult {
    private List<User> users = new ArrayList<User>();
    private int totalQueryResultSize;
    private int queryOffset;
    private int queryLimit;
    private UserOrder order;
    private boolean ascending;
    
    public UserQueryResult() {
    }
    
    public UserQueryResult(int queryOffset, int queryLimit,
        int totalQueryResultSize, List<User> users, UserOrder order, boolean ascending) {
        
        this.queryLimit = queryLimit;
        this.queryOffset = queryOffset;
        this.totalQueryResultSize = totalQueryResultSize;
        this.users = users;
        this.ascending = ascending;
    }
    
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
    @XmlElement(name="users")
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
    
    @XmlElement
    public int getTotalQueryResultSize() {
        return totalQueryResultSize;
    }
    
    public void setTotalQueryResultSize(int totalQueryResultSize) {
        this.totalQueryResultSize = totalQueryResultSize;
    }
    
    @XmlElement
    public int getQueryOffset() {
        return queryOffset;
    }
    
    public void setQueryOffset(int queryOffset) {
        this.queryOffset = queryOffset;
    }
    
    @XmlElement
    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
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
