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

package com.cubeia.poker.hand.calculator;

import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.variant.texasholdem.TexasHoldemHandCalculator;
import org.junit.Test;


public class HandCalculatorSpeedTest {

    HandCalculator calc = new TexasHoldemHandCalculator();

    @Test
    public void testRankHand_1() throws Exception {
        Hand hand = new Hand("2S 3H TD JD TH");
        int iterations = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            calc.getHandStrength(hand);
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(iterations + " iterations for getHandStrength (static hand ) took: " + elapsed + "ms.");
        // 2011-08-19 : ca 8ms   - No checkHighCard
        // 2011-08-19 : ca 15ms  - Added checkHighCard
    }


}
