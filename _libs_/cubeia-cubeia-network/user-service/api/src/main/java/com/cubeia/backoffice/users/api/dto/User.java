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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * User.
 * @author w
 */
@XmlRootElement(name="User")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long userId;
	private String externalUserId;
	private String userName;
    private String password; //temporary
	private Long operatorId;
	private UserStatus status;
	private UserInformation userInformation;
	private Map<String, String> attributes = new HashMap<String, String>();
    private Date creationDate;
    private Date lastLoginDate;
	private UserType userType = UserType.USER;
    
	public User() {}
	
	public User(String userName) {
	    this.userName = userName;
	}
	
	/** 
	 * @return String, a readable representation
	 */
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * User id. Assigned by the service.
	 * @return user id
	 */
	@XmlElement(name="userId")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	/**
	 * External user id. Optionally assigned by the client if needed.
	 * @return external id
	 */
	@XmlElement(name="externalUserId")
	public String getExternalUserId() {
        return externalUserId;
    }
	
	public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }
	
	/**
	 * Operator id.
	 * @return operator id
	 */
	@XmlElement(name = "operatorId")
	public Long getOperatorId() {
        return operatorId;
    }
	
	public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

	/**
	 * User name.
	 * @return user name
	 */
	@XmlElement(name="userName")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

    /**
     * Password.
     * @return password
     */
    @XmlElement(name="password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
	 * Extended user data.
	 * @return user information
	 */
	@XmlElement(name = "userInformation")
	public UserInformation getUserInformation() {
		return userInformation;
	}

	public void setUserInformation(UserInformation userInformation) {
		this.userInformation = userInformation;
	}
	
	/**
	 * User status.
	 * @return status
	 */
	@XmlElement(name = "status")
	public UserStatus getStatus() {
        return status;
    }
	
	public void setStatus(UserStatus status) {
        this.status = status;
    }
	
	/**
	 * User attribute map.
	 * @return attribute map
	 */
	@XmlElementWrapper(name = "attributes")
	public Map<String, String> getAttributes() {
        return attributes;
    }
	
	public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	/**
	 * User creation date.
	 * @return creation date
	 */
    @XmlElement(name = "creationDate")
	public Date getCreationDate() {
        return creationDate;
    }
	
	public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
	
	/**
	 * Last login date.
	 * @return last login date
	 */
    @XmlElement(name = "lastLoginDate")
	public Date getLastLoginDate() {
        return lastLoginDate;
    }
	
	public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
	
	/**
	 * User type.
	 * @return type
	 */
	@XmlElement
    public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (attributes != null ? !attributes.equals(user.attributes) : user.attributes != null) return false;
        if (creationDate != null ? !creationDate.equals(user.creationDate) : user.creationDate != null) return false;
        if (externalUserId != null ? !externalUserId.equals(user.externalUserId) : user.externalUserId != null)
            return false;
        if (lastLoginDate != null ? !lastLoginDate.equals(user.lastLoginDate) : user.lastLoginDate != null)
            return false;
        if (operatorId != null ? !operatorId.equals(user.operatorId) : user.operatorId != null) return false;
        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (status != user.status) return false;
        if (userId != null ? !userId.equals(user.userId) : user.userId != null) return false;
        if (userInformation != null ? !userInformation.equals(user.userInformation) : user.userInformation != null)
            return false;
        if (userName != null ? !userName.equals(user.userName) : user.userName != null) return false;
        if (userType != user.userType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (externalUserId != null ? externalUserId.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (operatorId != null ? operatorId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (userInformation != null ? userInformation.hashCode() : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (lastLoginDate != null ? lastLoginDate.hashCode() : 0);
        result = 31 * result + (userType != null ? userType.hashCode() : 0);
        return result;
    }
}
