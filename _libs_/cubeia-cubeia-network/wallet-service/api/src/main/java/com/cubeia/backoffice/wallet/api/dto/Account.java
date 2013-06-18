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

package com.cubeia.backoffice.wallet.api.dto;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Account. All transaction entries must be booked against an account.
 * @author w
 */
@XmlRootElement(name="account")
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum AccountStatus {
    	/** Account is open */
        OPEN,
        /** Account is closed */
        CLOSED
    }    
    
    public enum AccountType {
    	/** Short lived account representing a session. */
        SESSION_ACCOUNT,
        /** Long lived static account (normal user account) */
        STATIC_ACCOUNT,
        /** Internal system account */
        SYSTEM_ACCOUNT,
        /** Operator account */
        OPERATOR_ACCOUNT
    }    
    
    private Long id;
    private Long userId;
    private AccountInformation information;
    private AccountStatus status;
    private AccountType type;
    private Date created = new Date();
    private Date closed;
    private String currency;
    private Boolean negativeAmountAllowed = false;
    
	public Account(){}
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
   
    /**
     * Account id. Assigned by the service.
     * @return the account id
     */
    @XmlElement(name="id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @XmlElement(name="negativeAmountAllowed")
    public Boolean getNegativeAmountAllowed() {
		return negativeAmountAllowed;
	}

	public void setNegativeAmountAllowed(Boolean negativeAmountAllowed) {
		this.negativeAmountAllowed = negativeAmountAllowed;
	}


    /**
     * Extended account information.
     * @return account information
     */
    @XmlElement
	public AccountInformation getInformation() {
		return information;
	}

	public void setInformation(AccountInformation information) {
		this.information = information;
	}

	/**
	 * Account status.
	 * @return status
	 */
	@XmlElement(name="status")
	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	/**
	 * User id (owner of the account).
	 * @return user id
	 */
	@XmlElement(name="userId")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/** Account type.
	 * @return account type
	 */
	@XmlElement(name="type")
	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}
	
	/**
	 * Creation date.
	 * @return creation date
	 */
    @XmlElement(name="created")
    @XmlJavaTypeAdapter(com.cubeia.backoffice.wallet.api.util.DateAdapter.class) 
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Close date (if closed).
     * @return close date, null if not closed
     */
    @XmlElement(name="closed")
    @XmlJavaTypeAdapter(com.cubeia.backoffice.wallet.api.util.DateAdapter.class) 
    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }

    /**
     * ISO 4217 currency code.
     * @return the 3 letter currency code
     */
    @XmlElement(name = "currency")
    public String getCurrencyCode() {
        return currency;
    }
    
    public void setCurrencyCode(String currencyCode) {
        currency = currencyCode;
    }
}
