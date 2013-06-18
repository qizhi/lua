/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.tournament.payouts;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.cubeia.games.poker.common.money.Currency;
import org.junit.Before;
import org.junit.Test;

import com.cubeia.games.poker.tournament.configuration.payouts.IntRange;
import com.cubeia.games.poker.tournament.configuration.payouts.Payout;
import com.cubeia.games.poker.tournament.configuration.payouts.Payouts;
import com.google.common.collect.ImmutableSet;

public class PayoutHandlerTest {

    private PayoutHandler payoutHandler;
    private Currency eur = new Currency("EUR",2);

    @Before
    public void setup() {
        // Total prize pool is $200.
        Payouts payouts = new Payouts(new IntRange(20, 25), list(45.45, 22.73, 15.15, 9.09, 7.58, 6.12), bd(200),eur);
        payoutHandler = new PayoutHandler(payouts);
    }

    @Test
    public void testSimpleCase() {
        // Simple case, player finishes in place 4.
        // 9.09% of $200 is $18.18
        Map<Integer, BigDecimal> balanceAtStart = of(7, bd(70));
        List<ConcretePayout> payouts = payoutHandler.calculatePayouts(ImmutableSet.<Integer>of(7), balanceAtStart, 4);
        assertThat(payouts.get(0).getPayout(), is(bd("18.18")));
    }

    private BigDecimal bd(int i) {
        return  new BigDecimal(i).setScale(2);
    }

    @Test
    public void testTwoPlayersOut() {
        // Two players are out and they had different amounts of chips when the hand started.
        // 15.15% of $200 is $30.30
        Map<Integer, BigDecimal> balanceAtStart = of(7, bd(70), 8, bd(80));
        List<ConcretePayout> payouts = payoutHandler.calculatePayouts(ImmutableSet.<Integer>of(7, 8), balanceAtStart, 4);
        assertThat(payouts.get(0).getPayout(), is(bd("18.18")));
        assertThat(payouts.get(1).getPayout(), is(bd("30.30")));
    }

    private BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    @Test
    public void testThreePlayersOutWithSameStartChips() {
        // Two players are out and they had different amounts of chips when the hand started.
        // 7.58% of $200 is $15.16. 15.16 + 18.18 + 30.30 = 63.64
        // 63.64 / 3 = 21.21
        // 21.21 * 3 = 63.63 => 1 cent remainder
        Map<Integer, BigDecimal> balanceAtStart = of(7, bd(70), 8, bd(70), 9, bd(70));
        List<ConcretePayout> payouts = payoutHandler.calculatePayouts(ImmutableSet.<Integer>of(7, 8, 9), balanceAtStart, 5);

        assertThat(payouts.get(0).getPayout(), is(bd("21.22")));
        assertThat(payouts.get(1).getPayout(), is(bd("21.21")));
        assertThat(payouts.get(2).getPayout(), is(bd("21.21")));
    }

    @Test
    public void testFourPlayersTwoOfWhichHadTheSameStartingChips() {
        // Two players are out and they had different amounts of chips when the hand started.
        // 15.16 + 18.18 = 33.34
        // 33.34 / 2 = 16.67
        // 6.12% of $200 = 12.24
        Map<Integer, BigDecimal> balanceAtStart = of(7, bd(70), 8, bd(70), 9, bd(80), 10, bd(20));
        List<ConcretePayout> payouts = payoutHandler.calculatePayouts(ImmutableSet.<Integer>of(7, 8, 10), balanceAtStart, 6);

        assertThat(payouts.get(0).getPayout(), is(bd("12.24")));
        assertThat(payouts.get(1).getPayout(), is(bd("16.67")));
        assertThat(payouts.get(2).getPayout(), is(bd("16.67")));
        assertThat(payouts.get(3).getPayout(), is(bd("30.30")));
    }

    private List<Payout> list(double ... percentages) {
        List<Payout> payouts = newArrayList();
        int position = 1;
        for (double percentage : percentages) {
            payouts.add(new Payout(new IntRange(position, position), BigDecimal.valueOf(percentage)));
            position++;
        }
        return payouts;
    }

}
