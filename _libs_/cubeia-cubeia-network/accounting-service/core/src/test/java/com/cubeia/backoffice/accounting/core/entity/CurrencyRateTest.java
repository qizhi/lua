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

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

public class CurrencyRateTest {

    @Test
    public void testInvert() {
        CurrencyRate r = new CurrencyRate("EUR", "SEK", new BigDecimal("10"), new Date(0));
        CurrencyRate ir = r.invert();
        assertThat(ir.getSourceCurrencyCode(), is(r.getTargetCurrencyCode()));
        assertThat(ir.getTargetCurrencyCode(), is(r.getSourceCurrencyCode()));
        assertThat(ir.getTimestamp(), is(r.getTimestamp()));
        assertThat(ir.getRate().doubleValue(), closeTo(0.1d, 0.001));
    }
    
    @Test
    public void testCombine() {
        CurrencyRate r0 = new CurrencyRate("EUR", "SEK", new BigDecimal("10.07"), new Date(0));
        CurrencyRate r1 = new CurrencyRate("SEK", "GBP", new BigDecimal("0.09"), new Date(0));
        
        CurrencyRate cr = r0.combine(r1);
        
        assertThat(cr.getSourceCurrencyCode(), is(r0.getSourceCurrencyCode()));
        assertThat(cr.getTargetCurrencyCode(), is(r1.getTargetCurrencyCode()));
        assertThat(cr.getTimestamp(), is(r0.getTimestamp()));
        assertThat(cr.getRate().doubleValue(), closeTo(10.07 * 0.09, 0.00005));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCombineBadCurrencies() {
        CurrencyRate r0 = new CurrencyRate("EUR", "SEK", new BigDecimal("1"), new Date(0));
        CurrencyRate r1 = new CurrencyRate("USD", "GBP", new BigDecimal("2"), new Date(0));
        r0.combine(r1);
    }
}
