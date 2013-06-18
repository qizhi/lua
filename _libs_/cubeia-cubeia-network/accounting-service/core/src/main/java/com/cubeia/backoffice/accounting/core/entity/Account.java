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

package com.cubeia.backoffice.accounting.core.entity;

import static javax.persistence.CascadeType.ALL;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Index;

@Entity
@Cache(region="Accounts", usage=READ_WRITE)
@Inheritance(strategy=InheritanceType.JOINED)
public class Account implements Serializable {

    private static final long serialVersionUID = -2259446823245207846L;
	
    private Long id;
    private Long userId;
    private Long walletId;
    private String name;
    private AccountStatus status;
    private String type;
    private Date created;
    private Date lastModified;
    private Date closed;
    private String currencyCode;
    private int fractionalDigits = 2;
    private boolean negativeBalanceAllowed = true;
    
    private Map<String, AccountAttribute> attributes = new HashMap<String, AccountAttribute>();
    
    public Account() { }

    public Account(Long id) { 
    	this.id = id;
    }

    public Account(Long userId, String currencyCode, int fractionalDigits) {
    	this(userId, null, currencyCode, fractionalDigits);
    }
    
    public Account(Long userId, Long walletId, String currencyCode, int fractionalDigits) {
    	setStatus(AccountStatus.OPEN);
    	setWalletId(walletId);
    	setUserId(userId);
    	setCurrencyCode(currencyCode);
    	setCreated(new Date());
    	setFractionalDigits(fractionalDigits);
    }
    
    public Account(String currencyCode, int fractionalDigits) {
    	setStatus(AccountStatus.OPEN);
    	setCurrencyCode(currencyCode);
        setFractionalDigits(fractionalDigits);
    	setCreated(new Date());
    }
    
    @Transient
    public String getAttribute(String key) {
    	AccountAttribute a = attributes.get(key);
    	return (a == null ? null : a.getValue());
    }
    
    public void setAttribute(String key, String value) {
    	AccountAttribute a = new AccountAttribute(this, key, value);
    	attributes.put(key, a);
    }
    
    @MapKey(name="key")
    // @Cache(region="Accounts", usage=READ_WRITE)
    @OneToMany(mappedBy="account",cascade=ALL,fetch=FetchType.LAZY)
    public Map<String, AccountAttribute> getAttributes() {
		return attributes;
	}
    
    public void setAttributes(Map<String, AccountAttribute> attributes) {
		this.attributes = attributes;
	}
    
	@Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(nullable=false)
	public boolean isNegativeBalanceAllowed() {
		return negativeBalanceAllowed;
	}
	
	public void setNegativeBalanceAllowed(boolean allowNegativeBalance) {
		this.negativeBalanceAllowed = allowNegativeBalance;
	}

    @Column(nullable=false)
	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}
	
	@Column(nullable=true)
	@Index(name="userId_index")
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	@Column(nullable=true)
	@Index(name="walletId_index")
	public Long getWalletId() {
		return walletId;
	}
	
	public void setWalletId(Long walletId) {
		this.walletId = walletId;
	}
	
	@Column(nullable=true)
	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public void updateLastModified() {
		setLastModified(new Date());
	}
	
	@Column(nullable=true)
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(nullable=true)
	@Index(name="type_index")
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Column(nullable=false,updatable=false)
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Column(nullable=true)
    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }
    
    @Column(nullable=false)
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    @Column(nullable = false)
    public int getFractionalDigits() {
        return fractionalDigits;
    }
    
    public void setFractionalDigits(int fractionalDigits) {
        this.fractionalDigits = fractionalDigits;
    }
    
    @Override
    public String toString() {
		return ToStringBuilder.reflectionToString(this);
    }
    
    @Override
    public boolean equals(Object obj) {
    	return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    @Override
    public int hashCode() {
    	return HashCodeBuilder.reflectionHashCode(this);
    }
}
