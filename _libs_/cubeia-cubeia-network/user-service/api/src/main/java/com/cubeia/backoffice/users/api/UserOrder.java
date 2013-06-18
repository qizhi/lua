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
package com.cubeia.backoffice.users.api;

/**
 * Sort order for user searched. This enum also contains the corresponding entity field name.
 * @author w
 */
public enum UserOrder {
	/** Order by id */
    ID("id"),
    /** Order by user name */
    USER_NAME("userName"),
    /** Order by status */
    STATUS("status"),
    /** Order by country */
    COUNTRY("information.country"),
    /** Order by creation date */
    CREATION_DATE("creationDate"),
    /** Order by last login date */
    LAST_LOGIN_DATE("lastLoginDate");
    
    private String fieldName;
    
    UserOrder(String colName) {
        this.fieldName = colName;
    }
    
    public String getColumnName() {
        return fieldName;
    }
}
