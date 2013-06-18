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

package com.cubeia.network.wallet.firebase.domain;

import java.math.BigDecimal;

public class ResultEntry {
	private long sessionId;
	private BigDecimal amount;
	private String currencyCode;
	private int fractionalDigits;
	
	public ResultEntry(long sessionId, BigDecimal amount, String currencyCode, int fractionalDigits) {
		super();
		this.sessionId = sessionId;
		this.amount = amount;
		this.currencyCode = currencyCode;
		this.fractionalDigits = fractionalDigits;
	}

	@Override
    public String toString() {
		return "sessionId["+sessionId+"] balance["+amount+"]";
    }
	
	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public String getCurrencyCode() {
        return currencyCode;
    }
	
	public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
	
	public int getFractionalDigits() {
        return fractionalDigits;
    }
	
	public void setFractionalDigits(int fractionalDigits) {
        this.fractionalDigits = fractionalDigits;
    }
	
}
