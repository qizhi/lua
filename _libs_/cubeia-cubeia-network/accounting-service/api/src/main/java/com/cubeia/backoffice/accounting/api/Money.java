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

import static java.math.RoundingMode.UNNECESSARY;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Immutable domain object representing money. Money is an amount and a currency. 
 * The currency is represented by an currency code and the number of fractional digits of the currency. 
 * The currency code need not be a valid ISO 4217 code.
 * @author w
 */
@XmlRootElement(name="money")
public final class Money implements Serializable {
	private static final long serialVersionUID = 6524466586945917257L;

	private String currencyCode;
	private int fractionalDigits;
	private BigDecimal amount;
	
	public Money() { }
	
	/**
	 * @see Money#Money(String, int, BigDecimal)
	 * @param currency
	 * @param amount
	 */
	public Money(Currency currency, BigDecimal amount) {
        if (currency == null) {
            throw new IllegalArgumentException("currency cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }
		
		this.currencyCode = currency.getCurrencyCode();
		this.fractionalDigits = currency.getDefaultFractionDigits();
        setAmount(amount, fractionalDigits);
	}
	
	/**
	 * Constructor for the cases where the currency is not on ISO4217 currency.
	 * The given amount will be normalized to have the scale equal to {@link #fractionalDigits}. I
	 * @param currencyCode currency code
	 * @param fractionalDigits number of fractional digits of the currency (most have 2)
	 * @param amount amount
     * @throws IllegalArgumentException if the fractional digits are < 0
     * @throws IllegalArgumentException if currency code or amount is null
     * @throws ArithmeticException if the amount has a scale that is bigger then the number of fractional digits
     *   and it is impossible to correct the scale without losing digits
	 */
	public Money(String currencyCode, int fractionalDigits, BigDecimal amount) {
        if (currencyCode == null) {
            throw new IllegalArgumentException("currency cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount cannot be null");
        }
        if (fractionalDigits < 0) {
            throw new IllegalArgumentException("fractional digits cannot be negative");
        }
        
		this.currencyCode = currencyCode;
		this.fractionalDigits = fractionalDigits;
		setAmount(amount, fractionalDigits);
	}

	private void setAmount(BigDecimal amount, int fractionalDigits) {
        try {
            this.amount = amount.setScale(fractionalDigits, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new ArithmeticException("impossible to scale down amount without losing digits");
        }
	}
	
	/**
	 * Copy constructor
	 * @param m money to copy
	 */
	public Money(Money m) {
	    this(m.getCurrencyCode(), m.getFractionalDigits(), m.getAmount());
    }
	
    @XmlElement(name = "amount", required = true)
	public BigDecimal getAmount() {
		return amount;
	}
	
    public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setFractionalDigits(int fractionalDigits) {
		this.fractionalDigits = fractionalDigits;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@XmlElement(name = "currency", required = true)
	public String getCurrencyCode() {
		return currencyCode;
	}
	
    @XmlElement(name = "fractionalDigits", required = true)
    public int getFractionalDigits() {
        return fractionalDigits;
    }
    
    /**
     * Multiplies with the given scalar and returns a new money instance with the same currency.
     * @param scalar scalar to multiply with
     * @return the result
     */
    public Money multiply(long scalar) {
        return new Money(getCurrencyCode(), getFractionalDigits(), getAmount().multiply(new BigDecimal(scalar)));
    }
    
    /**
     * Multiplies with the given multiplicand and returns a new money instance with the same currency.
     * @param multiplicand number to multiply with
     * @return the result
     * @throws ArithmeticException if the result will lose precision with the currencies number of
     *   fractional digits (scale).
     */
    public Money multiply(BigDecimal multiplicand) {
        BigDecimal newAmount = getAmount().multiply(multiplicand).setScale(getFractionalDigits(), UNNECESSARY);
        return new Money(getCurrencyCode(), getFractionalDigits(), newAmount);
    }
    
    /**
     * Adds the given money to this money and returns a new money object with the result.
     * @param m money to add
     * @return the result
     * @throws IllegalArgumentException if the currencies are not the same
     */
    public Money add(Money m) {
        if (!getCurrencyCode().equals(m.getCurrencyCode())  ||  getFractionalDigits() != m.getFractionalDigits()) {
            throw new IllegalArgumentException("cannot add money with different currencies");
        }
        return new Money(getCurrencyCode(), getFractionalDigits(), getAmount().add(m.getAmount()));
    }
    
    /**
     * Returns a new negated money instance.
     * This is equivalent to multiply(-1).
     * @return a negated instance
     */
    public Money negate() {
        return multiply(-1);
    }
    
    /**
     * Subtracts the given money from this and returns the result as a new instance. 
     * This is equivalent to this.add(other.negate().
     * @param m money to subtract
     * @return the result
     */
    public Money subtract(Money m) {
        return add(m.negate());
    }
    
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((currencyCode == null) ? 0 : currencyCode.hashCode());
        result = prime * result + fractionalDigits;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Money other = (Money) obj;
        if (amount == null) {
            if (other.amount != null)
                return false;
        } else if (!amount.equals(other.amount))
            return false;
        if (currencyCode == null) {
            if (other.currencyCode != null)
                return false;
        } else if (!currencyCode.equals(other.currencyCode))
            return false;
        if (fractionalDigits != other.fractionalDigits)
            return false;
        return true;
    }

    /**
     * Creates a string representation of this money object as:
     * currencyCode + " " + amount 
     */
    @Override
	public String toString() {
		return getCurrencyCode() + " " + getAmount();
	}

}
