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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>Cubeia Wallet Protocol Unit implementation.</p>
 * 
 * <p>This bean is mapped to the XML specification by using JAXB
 * annotation framework.</p> 
 * 
 * @author Tobias Westerblom, Cubeia Ltd
 */
@SuppressWarnings("serial")
@XmlRootElement(name="Currency")
public class Currency implements Serializable {
	
	private String code;
	private int fractionalDigits;
	
	public Currency() {}
	
    public Currency(String code, int fractionalDigits) {
        this.code = code;
        this.fractionalDigits = fractionalDigits;
    }

    public static String getDefaultCurrencyCode() {
        return "EUR";
    }

    public static int getDefaultFractionDigits() {
        return 2;
    }

    /**
	 * The ISO 4217 currency code.
	 * @return currency code
	 */
	@XmlAttribute(name="code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Number for fractional digits used in the currency.
	 * Most currencies use 2 fractional digits.
	 * @return fractional digits
	 */
	@XmlAttribute(name="fractionalDigits")
	public int getFractionalDigits() {
		return fractionalDigits;
	}

	public void setFractionalDigits(int fractionalDigits) {
		this.fractionalDigits = fractionalDigits;
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Currency other = (Currency) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
