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

package com.cubeia.backend.cashgame.dto;

import com.cubeia.games.poker.common.money.Currency;
import org.junit.Test;

import com.cubeia.games.poker.common.money.Money;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MoneyTest {

    private Currency sek3 = new Currency("SEK",3);
    private Currency sek = new Currency("SEK",2);
    private Currency eur = new Currency("EUR",2);
    private Currency eur3 = new Currency("EUR",2);

    @Test
    public void testCreation() {
        Money money = new Money(new BigDecimal(1234), sek);
        assertThat(money.getAmount(), is(new BigDecimal(1234L)));
        assertThat(money.getCurrencyCode(), is("SEK"));
        assertThat(money.getFractionalDigits(), is(2));
    }

    @Test
    public void testAdd() {
        Money m1 = new Money(new BigDecimal(1000), sek);
        Money m2 = new Money(new BigDecimal(-1000), sek);
        Money m3 = new Money(new BigDecimal(1234), sek);
        assertThat(m1.add(m2), is(new Money(BigDecimal.ZERO, sek)));
        assertThat(m1.add(m3), is(new Money(new BigDecimal(2234), sek)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFailOnIncompatibleCurrencies() {
        Money m1 = new Money(new BigDecimal(1000), sek);
        Money m2 = new Money(new BigDecimal(-1000), eur);
        m1.add(m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFailOnIncompatibleFractionalDigits() {
        Money m1 = new Money(new BigDecimal(1000), sek);
        Money m2 = new Money(new BigDecimal(-1000), sek3);
        m1.add(m2);
    }

    @Test
    public void testNegate() {
        Money m1 = new Money(new BigDecimal(1000), sek);
        Money m2 = new Money(new BigDecimal(-123), eur3);
        assertThat(m1.negate(), is(new Money(new BigDecimal(-1000), sek)));
        assertThat(m2.negate(), is(new Money(new BigDecimal(123), eur3)));
    }

    @Test
    public void testSubtract() {
        Money m1 = new Money(new BigDecimal(1000), sek);
        Money m2 = new Money(new BigDecimal(-123), sek);
        assertThat(m1.subtract(m2), is(new Money(new BigDecimal(1123), sek)));
        assertThat(m2.subtract(m1), is(new Money(new BigDecimal(-1123), sek)));
        assertThat(m1.subtract(m1), is(new Money(BigDecimal.ZERO, sek)));
    }

    @Test
    public void testAddScalar() {
        Money m1 = new Money(new BigDecimal(1000), sek);
        Money m2 = new Money(new BigDecimal(-1000), eur3);
        assertThat(m1.add(new BigDecimal(1234)), is(new Money(new BigDecimal(2234), sek)));
        assertThat(m2.add(new BigDecimal(1000)), is(new Money(new BigDecimal(0), eur3)));
    }

    @Test
    public void testToString() {
        Money money = new Money(new BigDecimal("12.34"), sek);
        assertThat(money.toString(), is("12.34 SEK"));
    }

}
