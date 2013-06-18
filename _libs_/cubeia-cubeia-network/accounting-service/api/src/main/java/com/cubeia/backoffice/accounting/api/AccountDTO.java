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

package com.cubeia.backoffice.accounting.api;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="account")
public class AccountDTO implements Serializable {

	private static final long serialVersionUID = 1895412255700282445L;

	private Long id;
	private Long userId;
	private Long walletId;
	private String name;
	private AccountStatusDTO status;
	private String type;
	private Date created;
	private Date lastModified;
	private Date closed;
	private String currencyCode;
	private int fractionalDigits;
    private HashMap<String, AccountAttributeDTO> attributes = new HashMap<String, AccountAttributeDTO>();
    private boolean negativeBalanceAllowed = true;

	public AccountDTO() { }
	
	public AccountDTO(Long extId, String currencyCode, int fractionalDigits) {
		this(extId, null, currencyCode, fractionalDigits);
	}
	
	public AccountDTO(Long userId, String type, String currencyCode, int fractionalDigits) {
		this(userId, null, type, currencyCode, fractionalDigits);
	}

	public AccountDTO(Long userId, Long walletId, String type, String currencyCode, int fractionalDigits) {
		this.setStatus(AccountStatusDTO.OPEN);
		this.setCurrencyCode(currencyCode);
		this.setFractionalDigits(fractionalDigits);
		this.setUserId(userId);
		this.setCreated(new Date());
		this.setType(type);
		this.setWalletId(walletId);
	}
	
	public void setWalletId(Long walletId) {
		this.walletId = walletId;
	}
	
	@XmlElement
	public Long getWalletId() {
		return walletId;
	}
	
	@XmlElement
	public boolean isNegativeBalanceAllowed() {
		return negativeBalanceAllowed;
	}
	
	public void setNegativeBalanceAllowed(boolean allowNegativeBalance) {
		this.negativeBalanceAllowed = allowNegativeBalance;
	}

	@XmlElement
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@XmlElement
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public AccountStatusDTO getStatus() {
		return status;
	}

	public void setStatus(AccountStatusDTO status) {
		this.status = status;
	}

	@XmlElement
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@XmlElement
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@XmlElement
	public Date getClosed() {
		return closed;
	}

	public void setClosed(Date closed) {
		this.closed = closed;
	}

	@XmlElement
	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	
    @XmlElement
	public int getFractionalDigits() {
        return fractionalDigits;
    }
	
	public void setFractionalDigits(int fractionalDigits) {
        this.fractionalDigits = fractionalDigits;
    }

	@XmlElement
	public HashMap<String, AccountAttributeDTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, AccountAttributeDTO> attributes) {
		this.attributes = attributes;
	}
	
	
	// --- OBJECT METHODS --- //
	
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
