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

package com.cubeia.games.poker.common.money;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Immutable domain object representing money.
 * <p/>
 * The value must be multiplied by 10^fractionalDigits. For example: $12.34
 * should be stored as Money(1234, "USD", 2).
 *
 * @author w
 */
public final class Money implements Serializable {

    private static final long serialVersionUID = 6524466586945917257L;



    private final Currency currency;
    private final BigDecimal amount;

    public Money(BigDecimal amount, Currency currency) {
        super();
        this.amount = amount;
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return currency.getCode();
    }

    public int getFractionalDigits() {
        return currency.getFractionalDigits();
    }

    public BigDecimal getAmount() {
        return amount; 
    }

    /**
     * Returns a new money object by adding the given money to this money.
     * The currencies must be the same.
     *
     * @param m money to add
     * @return the sum of this money and the given money
     * @throws IllegalArgumentException if the currencies are incompatible
     */
    public Money add(Money m) {
        if (getFractionalDigits() != m.getFractionalDigits() || !getCurrencyCode().equals(m.getCurrencyCode())) {
            throw new IllegalArgumentException("incompatible currencies: this = " +
                    getCurrencyCode() + "+" + getFractionalDigits() + ", other = " +
                    m.getCurrencyCode() + "+" + m.getFractionalDigits());
        }
        return new Money(getAmount().add(m.getAmount()),getCurrency());
    }

    /**
     * Subtract the given money from this money.
     * This is the same as doing this.add(that.negate()).
     *
     * @param m money to subtract
     * @return the result
     */
    public Money subtract(Money m) {
        return this.add(m.negate());
    }

    /**
     * Returns a new money object with the given scalar amount added.
     *
     * @param amount amount to add
     * @return new money with amount added
     */
    public Money add(BigDecimal amount) {
        return new Money(getAmount().add(amount),getCurrency());
    }

    public Money negate() {
        return new Money(getAmount().negate(),getCurrency());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Money money = (Money) o;

        if (amount != null ? !amount.equals(money.amount) : money.amount != null) return false;
        if (currency != null ? !currency.equals(money.currency) : money.currency != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = currency != null ? currency.hashCode() : 0;
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        BigDecimal amountBd = getAmount();
        return "" + amountBd.toPlainString() + " " + getCurrencyCode();
    }

    public Currency getCurrency() {
        return currency;
    }
}
