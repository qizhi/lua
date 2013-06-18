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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = {
@UniqueConstraint(columnNames={"currencyCode"})})
public class SupportedCurrency {

//	private Long id;
	private String currencyCode;
	private int fractionalDigits;
	private boolean removed = false;
	
	public SupportedCurrency() {}

    public SupportedCurrency(String currencyCode, int fractionalDigits) {
        this.currencyCode = currencyCode;
        this.fractionalDigits = fractionalDigits;
    }

	@Id
	@Column(nullable=false, unique = true)	
	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	
	@Column(nullable=false)
	public int getFractionalDigits() {
		return fractionalDigits;
	}
	
	public void setFractionalDigits(int fractionalDigits) {
		this.fractionalDigits = fractionalDigits;
	}

    /**
	 * Returns true if the currency is removed.
	 * @return true if removed
	 */
	public boolean isRemoved() {
		return removed;
	}
	
	public void setRemoved(boolean removed) {
		this.removed = removed;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((currencyCode == null) ? 0 : currencyCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		SupportedCurrency other = (SupportedCurrency) obj;
		if (currencyCode == null) {
			if (other.currencyCode != null)
				return false;
		} else if (!currencyCode.equals(other.currencyCode))
			return false;
		return true;
	}
}