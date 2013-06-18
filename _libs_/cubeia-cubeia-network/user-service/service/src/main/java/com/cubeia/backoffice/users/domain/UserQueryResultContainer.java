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
package com.cubeia.backoffice.users.domain;

import java.util.List;

import com.cubeia.backoffice.users.entity.User;

/**
 * A holder for a user query result.
 * @author w
 *
 */
public class UserQueryResultContainer {
    private List<User> users;
    private int totalQueryResultSize;
    
    public UserQueryResultContainer(int size, List<User> users) {
        totalQueryResultSize = size;
        this.users = users;
    }

    /**
     * List of users returned by the query and limited to the offset and size limit.
     * @return users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * The total query result size without limits.
     * @return the total result size
     */
    public int getTotalQueryResultSize() {
        return totalQueryResultSize;
    }
}
