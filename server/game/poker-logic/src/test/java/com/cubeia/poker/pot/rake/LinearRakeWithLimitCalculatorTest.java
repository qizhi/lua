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

package com.cubeia.poker.pot.rake;

import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.pot.RakeInfoContainer;
import com.cubeia.poker.rake.LinearRakeWithLimitCalculator;
import com.cubeia.poker.settings.RakeSettings;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.Pot;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LinearRakeWithLimitCalculatorTest {
    Currency eur = new Currency("EUR", 2);

    @Test
    public void testCalculateRakeNoLimit() {
        BigDecimal rakeFraction = new BigDecimal("0.10");

        LinearRakeWithLimitCalculator rc = new LinearRakeWithLimitCalculator(
                RakeSettings.createDefaultRakeSettings(rakeFraction),
                eur);

        PokerPlayer player1 = Mockito.mock(PokerPlayer.class);
        PokerPlayer player2 = Mockito.mock(PokerPlayer.class);
        Pot pot1 = new Pot(0);
        Pot pot2 = new Pot(1);
        Pot pot3 = new Pot(2);

        pot1.bet(player1, bd(10000));
        pot1.bet(player2, bd(10000L));
        pot2.bet(player1, bd(3000L));
        pot2.bet(player2, bd(2000L));
        pot3.bet(player1, bd(1000L));

        RakeInfoContainer rakeInfoContainer = rc.calculateRakes(asList(pot1, pot2, pot3), true);
        assertThat(rakeInfoContainer.getTotalPot(), is(bd(26000)));
        assertThat(rakeInfoContainer.getTotalRake(), is(bd(2600)));

        Map<Pot, BigDecimal> rakes = rakeInfoContainer.getPotRakes();

        assertThat(rakes.get(pot1), is(rakeFraction.multiply(BigDecimal.valueOf(10000 + 10000))));
        assertThat(rakes.get(pot2), is(rakeFraction.multiply(BigDecimal.valueOf(3000 + 2000))));
        assertThat(rakes.get(pot3), is(rakeFraction.multiply(BigDecimal.valueOf(1000))));
    }

    private BigDecimal bd(long l) {
        return new BigDecimal(l).setScale(2);
    }

    @Test
    public void testCalculateRakeWithLimit() {
        BigDecimal rakeFraction = new BigDecimal("0.1");
        LinearRakeWithLimitCalculator rc = new LinearRakeWithLimitCalculator(new RakeSettings(rakeFraction, bd(4000), bd(1000)),eur);

        PokerPlayer player1 = Mockito.mock(PokerPlayer.class);
        PokerPlayer player2 = Mockito.mock(PokerPlayer.class);
        PokerPlayer player3 = Mockito.mock(PokerPlayer.class);
        Pot pot1 = new Pot(0);
        Pot pot2 = new Pot(1);
        Pot pot3 = new Pot(2);
        Pot pot4 = new Pot(3);

        pot1.bet(player1, bd(7000L));
        pot1.bet(player2, bd(7000L));
        pot1.bet(player3, bd(7000L));

        pot2.bet(player1, bd(10000L));
        pot2.bet(player2, bd(5000L));
        pot2.bet(player3, bd(3000L));

        pot3.bet(player1, bd(10000L));
        pot4.bet(player2, bd(10000L));

        Collection<Pot> pots = Arrays.asList(pot1, pot2, pot3, pot4);

        RakeInfoContainer rakeInfoContainer = rc.calculateRakes(pots, true);
        assertThat(rakeInfoContainer.getTotalPot(), is(bd(7000L * 3 + 10000 + 5000 + 3000 + 10000 + 10000)));
        assertThat(rakeInfoContainer.getTotalRake(), is(bd(4000)));

        Map<Pot, BigDecimal> rakes = rakeInfoContainer.getPotRakes();

        assertThat(rakes.get(pot1), is(rakeFraction.multiply(BigDecimal.valueOf(7000 * 3)).setScale(2)));            // 2100
        assertThat(rakes.get(pot2), is(rakeFraction.multiply(BigDecimal.valueOf(10000 + 5000 + 3000)).setScale(2))); // 1800
        assertThat(rakes.get(pot3), is(bd(100))); // 100 (limited)
        assertThat(rakes.get(pot4), is(bd(0)));   // 0 (over limit)
    }

    @Test
    public void testCalculateRakeWithLimitHeadsUp() {
        BigDecimal rakeFraction = new BigDecimal("0.10");
        LinearRakeWithLimitCalculator rc = new LinearRakeWithLimitCalculator(new RakeSettings(rakeFraction, bd(4000), bd(150)),eur);

        PokerPlayer player1 = Mockito.mock(PokerPlayer.class);
        PokerPlayer player2 = Mockito.mock(PokerPlayer.class);
        Pot pot1 = new Pot(0);
        Pot pot2 = new Pot(1);

        pot1.bet(player1, bd(500));
        pot1.bet(player2, bd(500));
        pot2.bet(player1, bd(50000));
        pot2.bet(player2, bd(50000));

        Collection<Pot> pots = asList(pot1, pot2);

        RakeInfoContainer rakeInfoContainer = rc.calculateRakes(pots, true);
        assertThat(rakeInfoContainer.getTotalPot(), is(bd((50000L + 500) * 2)));
        assertThat(rakeInfoContainer.getTotalRake(), is(bd(150)));

        Map<Pot, BigDecimal> rakes = rakeInfoContainer.getPotRakes();

        assertThat(rakes.get(pot1), is(rakeFraction.multiply(new BigDecimal("1000.00")).setScale(2)));
        assertThat(rakes.get(pot2), is(bd(50)));
    }

    @Test
    public void testCalculateRakeWithLimitHeadsUpAndPlayersThatContributedZero() {
        BigDecimal rakeFraction = new BigDecimal("0.6");
        RakeSettings rakeSettings = new RakeSettings(rakeFraction, bd(400), bd(150));
        LinearRakeWithLimitCalculator rc = new LinearRakeWithLimitCalculator(rakeSettings,eur);

        PokerPlayer player1 = Mockito.mock(PokerPlayer.class);
        PokerPlayer player2 = Mockito.mock(PokerPlayer.class);

        Pot mainpot = new Pot(0);
        Pot betstackpot = new Pot(Integer.MAX_VALUE);

        mainpot.bet(player1, bd(5000L));
        mainpot.bet(player2, bd(5000L));

        betstackpot.bet(player1, bd(100L));
        betstackpot.bet(player2, bd(100L));


        Collection<Pot> pots = asList(mainpot, betstackpot);

        RakeInfoContainer rakeInfoContainer = rc.calculateRakes(pots, true);
        assertThat(rakeInfoContainer.getTotalPot(), is(bd(10200)));
        assertThat(rakeInfoContainer.getTotalRake(), is(bd(150)));

    }

    @Test
    public void testCalculateRakeNoRakeBeforeFirstCall() {
        BigDecimal rakeFraction = new BigDecimal("0.10");
        LinearRakeWithLimitCalculator rc = new LinearRakeWithLimitCalculator(RakeSettings.createDefaultRakeSettings(rakeFraction),eur);

        PokerPlayer player1 = Mockito.mock(PokerPlayer.class);
        PokerPlayer player2 = Mockito.mock(PokerPlayer.class);
        Pot pot1 = new Pot(0);

        pot1.bet(player1, bd(10000L));
        pot1.bet(player2, bd(10000L));

        RakeInfoContainer rakeInfoContainer = rc.calculateRakes(asList(pot1), false);
        assertThat(rakeInfoContainer.getTotalPot(), is(bd(20000)));
        assertThat(rakeInfoContainer.getTotalRake(), is(bd(0)));

        Map<Pot, BigDecimal> rakes = rakeInfoContainer.getPotRakes();
        assertThat(rakes.get(pot1), is(bd(0)));
    }

}
