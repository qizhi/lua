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
package com.cubeia.backoffice.users.entity;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames={"userName", "operatorId"})
})
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id = null;
	private String externalId = null;
	private Long operatorId = null;
	private String userName;
	private String password;
	private UserInformation information = new UserInformation();
    private Map<String, UserAttribute> attributes = new HashMap<String, UserAttribute>();
	private UserStatus status = UserStatus.ENABLED;
    private Date creationDate;
    private Date lastLoginDate;
    private UserType userType = UserType.USER;
	
	private String decrypedPassword;
	
	public User() {};
	
	public User(String userName, Long operatorId) {
		this.userName = userName;
		this.operatorId = operatorId;
	}

	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Index(name="externalId_index")
	public String getExternalId() {
        return externalId;
    }
	
	public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

	@Column(nullable=false)
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Fetch(FetchMode.JOIN)
	@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE})
	@JoinColumn(name = "information_fk", nullable = true)
	public UserInformation getInformation() {
		return information;
	}

	public void setInformation(UserInformation information) {
		this.information = information;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Enumerated(EnumType.STRING)
	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}
	
	@Column(nullable=false)
	public Long getOperatorId() {
        return operatorId;
    }
	
	public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

	@Transient
	public String getDecrypedPassword() {
		return decrypedPassword;
	}

	public void setDecrypedPassword(String decrypedPassword) {
		this.decrypedPassword = decrypedPassword;
	}
	
	@Temporal(TIMESTAMP)
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Temporal(TIMESTAMP)
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @MapKey(name="key")
    @BatchSize(size=10)
    @OneToMany(mappedBy="user", cascade = ALL, fetch = EAGER)
    public Map<String, UserAttribute> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, UserAttribute> attributes) {
        this.attributes = attributes;
    }
    
    public void addAttribute(String key, String value) {
        getAttributes().put(key, new UserAttribute(this, key, value));
    }
    
    @Enumerated(EnumType.STRING)
    public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	@Transient
    public String getAttributeValue(String key) {
        if (getAttributes().containsKey(key)) {
            return getAttributes().get(key).getValue();
        } else {
            return null;
        }
    }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((decrypedPassword == null) ? 0 : decrypedPassword.hashCode());
        result = prime * result + ((operatorId == null) ? 0 : operatorId.hashCode());
		result = prime * result
				+ ((information == null) ? 0 : information.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		User other = (User) obj;
		if (decrypedPassword == null) {
			if (other.decrypedPassword != null)
				return false;
		} else if (!decrypedPassword.equals(other.decrypedPassword))
			return false;
        if (operatorId == null) {
            if (other.operatorId != null)
                return false;
		} else if (!id.equals(other.id))
			return false;
		if (information == null) {
			if (other.information != null)
				return false;
		} else if (!information.equals(other.information))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
