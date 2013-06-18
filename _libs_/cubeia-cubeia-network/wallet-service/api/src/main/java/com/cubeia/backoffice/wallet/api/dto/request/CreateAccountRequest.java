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

package com.cubeia.backoffice.wallet.api.dto.request;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.cubeia.backoffice.wallet.api.dto.MetaInformation;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML and JSON specification by using JAXB
 * annotation framework.</p> 
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
@XmlRootElement(name="CreateAccountRequest")
public class CreateAccountRequest {
	
	private UUID requestId;
	private Long userId;
	private AccountType type;
	private String currencyCode;
	private MetaInformation information;
	
	private boolean negativeBalanceAllowed = false;
	
	public CreateAccountRequest() {};
	
	public CreateAccountRequest(UUID requestId, long userId, String currencyCode, AccountType type, MetaInformation information) {
		this.requestId = requestId;
		this.userId = userId;
		this.currencyCode = currencyCode;
		this.type = type;
		this.information = information;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	@XmlElement
	public boolean isNegativeBalanceAllowed() {
		return negativeBalanceAllowed;
	}
	
	public void setNegativeBalanceAllowed(boolean negativeBalanceAllowed) {
		this.negativeBalanceAllowed = negativeBalanceAllowed;
	}
	

	@XmlElement
	public UUID getRequestId() {
		return requestId;
	}

	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

	@XmlElement
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@XmlElement()
	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	@XmlElement
	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	@XmlElement
	public MetaInformation getInformation() {
		return information;
	}

	public void setInformation(MetaInformation information) {
		this.information = information;
	}

}
