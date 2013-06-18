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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.cubeia.backoffice.accounting.api.Money;

@SuppressWarnings("serial")
@XmlRootElement(name="accountBalanceResult")
public class AccountBalanceResult implements Serializable {
	
	private long accountId;
	private Money balance;
	
	public AccountBalanceResult() {
	}
	
	public AccountBalanceResult(long accountId, Money balance) {
        super();
        this.accountId = accountId;
        this.balance = balance;
    }

    /**
	 * @return String, a readable representation
	 */
	@Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
	
	@XmlElement(name="accountId")
	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	@XmlElement(name="balance")
	public Money getBalance() {
	    return balance;
	}
	
    public void setBalance(Money balance) {
        this.balance = balance;
    }
}
