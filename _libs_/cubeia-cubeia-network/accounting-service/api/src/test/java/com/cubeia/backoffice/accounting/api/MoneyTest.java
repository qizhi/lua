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

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Test;

public class MoneyTest {

    @Test(expected = ArithmeticException.class)
    public void testMoneyCurrencyBigDecimalBadScale() {
        new Money(Currency.getInstance("EUR"), new BigDecimal("123.34343"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMoneyCurrencyBigDecimalNullCurrency() {
        new Money(null, new BigDecimal("12.21"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoneyCurrencyBigDecimalNullAmount() {
        new Money(Currency.getInstance("EUR"), null);
    }
 
    @Test
    public void testMoneyCurrencyBigDecimal() {
        Money m = new Money(Currency.getInstance("EUR"), new BigDecimal("123.34"));
        assertThat(m.getAmount(), is(new BigDecimal("123.34")));
        assertThat(m.getFractionalDigits(), is(Currency.getInstance("EUR").getDefaultFractionDigits()));
        assertThat(m.getCurrencyCode(), is("EUR"));
        
        m = new Money(Currency.getInstance("EUR"), new BigDecimal("22.3"));
        assertThat(m.getAmount(), is(new BigDecimal("22.30")));
        assertThat(m.getAmount().scale(), is(Currency.getInstance("EUR").getDefaultFractionDigits()));
        assertThat(m.getFractionalDigits(), is(Currency.getInstance("EUR").getDefaultFractionDigits()));
        assertThat(m.getCurrencyCode(), is("EUR"));
    }

    @Test(expected = ArithmeticException.class)
    public void testMoneyStringIntBigDecimalBadScale() {
        new Money("EUR", 2, new BigDecimal("123.3433"));
    }
    
    @Test
    public void testMoneyStringIntBigDecimalScaleDown() {
        Money m = new Money("EUR", 2, new BigDecimal("123.3400"));
        assertThat(m.getAmount(), is(new BigDecimal("123.34")));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMoneyStringIntBigDecimalNegativeFractionalDigits() {
        new Money("EUR", -2, new BigDecimal("123.34"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMoneyStringIntBigDecimalNullCurrencyCode() {
        new Money(null, 2, new BigDecimal("123.34"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMoneyStringIntBigDecimalNullAmount() {
        new Money("EUR", 2, null);
    }
    
    @Test
    public void testMoneyStringIntBigDecimal() {
        Money m = new Money("EUR", 2, new BigDecimal("123.34"));
        assertThat(m.getAmount(), is(new BigDecimal("123.34")));
        assertThat(m.getFractionalDigits(), is(Currency.getInstance("EUR").getDefaultFractionDigits()));
        assertThat(m.getCurrencyCode(), is("EUR"));
        
        m = new Money("EUR", 2, new BigDecimal("22.3"));
        assertThat(m.getAmount(), is(new BigDecimal("22.30")));
        assertThat(m.getAmount().scale(), is(Currency.getInstance("EUR").getDefaultFractionDigits()));
        assertThat(m.getFractionalDigits(), is(Currency.getInstance("EUR").getDefaultFractionDigits()));
        assertThat(m.getCurrencyCode(), is("EUR"));
    }

    @Test
    public void testMoneyMoney() {
        Money m = new Money("EUR", 2, new BigDecimal("123.34"));
        Money m2 = new Money(m);
        assertThat(m2.getAmount(), is(m.getAmount()));
        assertThat(m2.getFractionalDigits(), is(m.getFractionalDigits()));
        assertThat(m2.getCurrencyCode(), is(m.getCurrencyCode()));
        assertThat(m2, is(m));
    }

    @Test
    public void testEquals() {
        Money m = new Money("EUR", 2, new BigDecimal("123.34"));
        Money m1 = new Money("EUR", 2, new BigDecimal("123.34"));
        Money m2 = new Money("SEK", 2, new BigDecimal("123.34"));
        Money m3 = new Money("EUR", 3, new BigDecimal("123.34"));
        Money m4 = new Money("EUR", 2, new BigDecimal("34.30"));

        assertThat(m, is(m1));
        assertThat(m1, is(m));
        assertThat(m, not(m2));
        assertThat(m2, not(m));
        assertThat(m, not(m3));
        assertThat(m, not(m4));
        assertThat(m2, not(m1));
        assertThat(m2, not(m3));
        assertThat(m2, not(m4));
    }
    
    @Test
    public void testMultiplyByScalar() {
        Money m = new Money("EUR", 2, new BigDecimal("10.01"));
        
        Money m2 = m.multiply(2);
        assertThat(m2.getAmount(), is(new BigDecimal("20.02")));
        assertThat(m2.getFractionalDigits(), is(2));
        assertThat(m2.getCurrencyCode(), is("EUR"));
        
        m2 = m.multiply(0);
        assertThat(m2.getAmount(), is(new BigDecimal("0.00")));
        
        m2 = m.multiply(10000);
        assertThat(m2.getAmount(), is(new BigDecimal("100100.00")));
        
        m2 = m.multiply(-1);
        assertThat(m2.getAmount(), is(new BigDecimal("-10.01")));
    }
    
    @Test
    public void testMultiplyByBigDecimal() {
        Money m = new Money("EUR", 2, new BigDecimal("10.01"));
        
        Money m2 = m.multiply(new BigDecimal("2"));
        assertThat(m2.getAmount(), is(new BigDecimal("20.02")));
        assertThat(m2.getFractionalDigits(), is(2));
        assertThat(m2.getCurrencyCode(), is("EUR"));
        
        m2 = m.multiply(ZERO);
        assertThat(m2.getAmount(), is(new BigDecimal("0.00")));
        
        m2 = m.multiply(new BigDecimal("10000"));
        assertThat(m2.getAmount(), is(new BigDecimal("100100.00")));
        
        m2 = m.multiply(ONE.negate());
        assertThat(m2.getAmount(), is(new BigDecimal("-10.01")));
    }
    
    @Test(expected = ArithmeticException.class)
    public void testMultiplyByBigDecimalBadScale() {
        Money m = new Money("EUR", 2, new BigDecimal("1.00"));
        m.multiply(new BigDecimal("0.001"));
    }

    @Test
    public void testAdd() {
        Money m = new Money("EUR", 2, new BigDecimal("1.23"));
        Money m2 = new Money("EUR", 2, new BigDecimal("-1.23"));
        Money r = m.add(m2);
        assertThat(r.getAmount(), is(new BigDecimal("0.00")));
        assertThat(r.getFractionalDigits(), is(2));
        assertThat(r.getCurrencyCode(), is("EUR"));
        
        Money m3 = new Money("EUR", 2, new BigDecimal("123.45"));
        r = m.add(m3);
        assertThat(r.getAmount(), is(new BigDecimal("124.68")));
    }
    
    @Test
    public void testSubtract() {
        Money m = new Money("EUR", 2, new BigDecimal("-1.23"));
        Money m2 = new Money("EUR", 2, new BigDecimal("-2.23"));
        Money r = m.subtract(m2);
        assertThat(r.getAmount(), is(new BigDecimal("1.00")));
        assertThat(r.getFractionalDigits(), is(2));
        assertThat(r.getCurrencyCode(), is("EUR"));
        
        assertThat(m.add(m2.negate()), is(m.subtract(m2)));
    }
    
    @Test
    public void testNegate() {
        Money m = new Money("EUR", 2, new BigDecimal("1.23"));
        Money r = m.negate();
        assertThat(r.getAmount(), is(new BigDecimal("-1.23")));
        assertThat(r.getFractionalDigits(), is(2));
        assertThat(r.getCurrencyCode(), is("EUR"));

        assertThat(m.negate().negate(), is(m));
    }
        
    @Test(expected = IllegalArgumentException.class)
    public void testAddBadCurrency() {
        Money m = new Money("EUR", 2, new BigDecimal("1.00"));
        Money m2 = new Money("SEK", 2, new BigDecimal("1.00"));
        m.add(m2);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSubtractBadCurrency() {
        Money m = new Money("EUR", 2, new BigDecimal("1.00"));
        Money m2 = new Money("SEK", 2, new BigDecimal("1.00"));
        m.subtract(m2);
    }
}
