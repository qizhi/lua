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

import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.cubeia.backoffice.wallet.api.dto.Account.AccountStatus;
import com.cubeia.backoffice.wallet.api.dto.Account.AccountType;
import com.cubeia.backoffice.wallet.api.dto.AccountsOrder;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML specification by using JAXB
 * annotation framework.</p> 
 * 
 * NULL Values are considered wild cards. 
 * 
 * If you do not provide a limit then the response will not contain any Account
 * data but only the total count for the query.
 * 
 * @author Fredrik Johansson, Cubeia Ltd
 */
@XmlRootElement(name="ListAccountsRequest")
public class ListAccountsRequest {
	
	private Long accountId;
	private Long userId;
	private Collection<AccountStatus> statuses;
	private Collection<AccountType> types;
	private int offset;
	private int limit;
	private AccountsOrder sortOrder;
	private boolean ascending;

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}


	@XmlElement
	public Collection<AccountStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(Collection<AccountStatus> statuses) {
		this.statuses = statuses;
	}
	
	public void setStatus(AccountStatus status) {
		this.statuses = Arrays.asList(status);
	}

	@XmlElement
	public Long getAccountId() {
		return accountId;
	}

	public Long getUserId() {
		return userId;
	}

	@XmlElement
	public Collection<AccountType> getTypes() {
		return types;
	}

	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@XmlElement
	public int getLimit() {
		return limit;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}

	@XmlElement
	public AccountsOrder getSortOrder() {
		return sortOrder;
	}

	@XmlElement
	public boolean isAscending() {
		return ascending;
	}
	
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public void setTypes(Collection<AccountType> types) {
		this.types = types;
	}

	public void setSortOrder(AccountsOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

}
