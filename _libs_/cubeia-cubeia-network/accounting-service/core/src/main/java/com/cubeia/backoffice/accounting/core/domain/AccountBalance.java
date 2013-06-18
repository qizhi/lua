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

package com.cubeia.backoffice.accounting.core.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.accounting.core.util.Arguments;

public class AccountBalance implements Serializable {

	private static final long serialVersionUID = 714549974815057942L;

	private final Long accountId;
	private final Money balance;
	
	public AccountBalance(Long accountId, Money balance) {
		Arguments.notNull(accountId, "accountId");
		Arguments.notNull(balance, "balance");
		this.accountId = accountId;
		this.balance = balance;
	}
	
	public AccountBalance(Long accountId, Currency curr, BigDecimal amount) {
		this(accountId, new Money(curr, amount));
	}
	
	public AccountBalance(Long accountId, String currency, int fractionalDigits, BigDecimal amount) {
		this(accountId, new Money(currency, fractionalDigits, amount));
	}
	
	public Long getAccountId() {
		return accountId;
	}
	
	public Money getBalance() {
		return balance;
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
