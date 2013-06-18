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

package com.cubeia.games.poker.adapter;

import com.cubeia.games.poker.adapter.BuyInCalculator.MinAndMaxBuyInResult;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BuyInCalculatorTest {

    @Test
    public void testCalculateBelowMax() {
        BigDecimal tableMinBuyIn = new BigDecimal(100);
        BigDecimal tableMaxBuyIn = new BigDecimal(20000);
        BigDecimal anteLevel = new BigDecimal(20);
        BuyInCalculator blc = new BuyInCalculator();

        MinAndMaxBuyInResult result;

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, BigDecimal.ZERO);
        assertThat(result.getMinBuyIn(), is(tableMinBuyIn));
        assertThat(result.getMaxBuyIn(), is(tableMaxBuyIn));
        assertThat(result.isBuyInPossible(), is(true));

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, bd(70));
        assertThat(result.getMinBuyIn(), is(tableMinBuyIn.subtract(bd(70))));
        assertThat(result.getMaxBuyIn(), is(tableMaxBuyIn.subtract(bd(70))));
        assertThat(result.isBuyInPossible(), is(true));

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, bd(99));
        assertThat(result.getMinBuyIn(), is(anteLevel));
        assertThat(result.getMaxBuyIn(), is(tableMaxBuyIn.subtract(bd(99))));
        assertThat(result.isBuyInPossible(), is(true));

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, tableMinBuyIn);
        assertThat(result.getMinBuyIn(), is(anteLevel));
        assertThat(result.getMaxBuyIn(), is(tableMaxBuyIn.subtract(tableMinBuyIn)));
        assertThat(result.isBuyInPossible(), is(true));

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, bd(5000));
        assertThat(result.getMinBuyIn(), is(anteLevel));
        assertThat(result.getMaxBuyIn(), is(tableMaxBuyIn.subtract(bd(5000))));
        assertThat(result.isBuyInPossible(), is(true));
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

    @Test
    public void testCalculateBalanceNearMax() {
        BigDecimal tableMinBuyIn = bd(100);
        BigDecimal tableMaxBuyIn = bd(20000);
        BigDecimal anteLevel = bd(20);
        BuyInCalculator blc = new BuyInCalculator();

        MinAndMaxBuyInResult result;

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, tableMaxBuyIn.subtract(anteLevel));
        assertThat(result.getMinBuyIn(), is(anteLevel));
        assertThat(result.getMaxBuyIn(), is(anteLevel));
        assertThat(result.isBuyInPossible(), is(true));

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, tableMaxBuyIn.subtract(anteLevel.divide(bd(2))));
        assertThat(result.getMinBuyIn(), is(anteLevel.divide(bd(2))));
        assertThat(result.getMaxBuyIn(), is(anteLevel.divide(bd(2))));
        assertThat(result.isBuyInPossible(), is(true));

    }

    @Test
    public void testCalculateBalanceAboveMax() {
        BigDecimal tableMinBuyIn = bd(100);
        BigDecimal tableMaxBuyIn = bd(20000);
        BigDecimal anteLevel = bd(20);
        BuyInCalculator blc = new BuyInCalculator();

        MinAndMaxBuyInResult result;

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, tableMaxBuyIn);
        assertThat(result.getMinBuyIn(), is(bd(0)));
        assertThat(result.getMaxBuyIn(), is(bd(0)));
        assertThat(result.isBuyInPossible(), is(false));

        result = blc.calculateBuyInLimits(tableMinBuyIn, tableMaxBuyIn, anteLevel, tableMaxBuyIn.add(BigDecimal.ONE));
        assertThat(result.getMinBuyIn(), is(bd(0)));
        assertThat(result.getMaxBuyIn(), is(bd(0)));
        assertThat(result.isBuyInPossible(), is(false));
    }

    @Test
    public void testCalculateReserveAmount() {
        BigDecimal tableMaxBuyIn = new BigDecimal(20000);
        BuyInCalculator blc = new BuyInCalculator();

        assertThat(blc.calculateAmountToReserve(tableMaxBuyIn, bd(5000), bd(20000)), is(bd(20000 - 5000)));
        assertThat(blc.calculateAmountToReserve(tableMaxBuyIn, bd(5000), bd(2000)), is(bd(2000)));
        assertThat(blc.calculateAmountToReserve(tableMaxBuyIn, bd(0), bd(20000)), is(bd(20000)));
        assertThat(blc.calculateAmountToReserve(tableMaxBuyIn, bd(20000), bd(20000)), is(bd(0)));
    }

}
