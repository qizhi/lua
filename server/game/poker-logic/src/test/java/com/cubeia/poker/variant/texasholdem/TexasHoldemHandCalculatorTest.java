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
package com.cubeia.poker.variant.texasholdem;

import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandInfo;
import com.cubeia.poker.hand.HandType;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TexasHoldemHandCalculatorTest {

    private TexasHoldemHandCalculator calculator;

    @Before
    public void setup() {
        calculator = new TexasHoldemHandCalculator();
    }

    @Test
    public void testGetBestHandInfoForPocketCards() throws Exception {
        HandInfo info = calculator.getBestHandInfo(new Hand("7S 8S"));
        assertEquals(HandType.HIGH_CARD, info.getHandType());
    }

    @Test
    public void testGetBestHandInfoForFullBoard() throws Exception {
        HandInfo info = calculator.getBestHandInfo(new Hand("7S 8S 2S JC QD 4S 5S"));
        assertEquals(HandType.FLUSH, info.getHandType());
    }

}
