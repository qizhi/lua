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

package com.cubeia.games.poker.tournament.configuration.payouts;

import com.cubeia.games.poker.common.money.Currency;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class PayoutsTest {

    private Payouts payouts;

    private Payouts sitAndGoPayouts;
    private Payouts payoutsWithRange;
    private Currency eur = new Currency("EUR",2);

    @Before
    public void setup() {
        PayoutStructure structure = PayoutStructureParserTest.createTestStructure();
        this.payouts = structure.getPayoutsForEntrantsAndPrizePool(56, bd(20000), eur);
        this.sitAndGoPayouts = structure.getPayoutsForEntrantsAndPrizePool(10, bd(1000),eur);
        this.payoutsWithRange = structure.getPayoutsForEntrantsAndPrizePool(235, bd(500000),eur);
    }

    @Test
    public void testPlayerNotInTheMoneyGetsZero() {
        assertThat(payouts.getPayoutsForPosition(56), is(bd("0")));
    }

    @Test
    public void testBubbleGetsBubble() {
        assertThat(sitAndGoPayouts.getPayoutList().size(), is(3));
        assertThat(sitAndGoPayouts.getPayoutsForPosition(3), not(bd("0")));
        assertThat(sitAndGoPayouts.getPayoutsForPosition(4), is(bd("0")));
    }

    @Test
    public void testRangedPayouts() {
        // 0.72% of $5000 = 36
        assertThat(payoutsWithRange.getPayoutsForPosition(36), is(bd(3600)));
    }
    private BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
    private BigDecimal bd(int i) {
        return new BigDecimal(i).setScale(2);
    }

}
