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

package com.cubeia.poker.hand.eval;

import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandStrength;
import com.cubeia.poker.hand.HandType;
import org.junit.Test;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HandTypeCheckCalculatorTest {

    private HandTypeCheckCalculator calculator = new HandTypeCheckCalculator();

    @Test
    public void testCheckStraightFlush() throws Exception {
        Hand hand = new Hand("TH KH QH AH JH");
        Hand sortedHand = new Hand("AH KH QH JH TH");
        HandStrength handStrength = calculator.checkStraightFlush(hand);
        // Technically, this is a royal, but we can live with calling it a straight flush.
        assertThat(handStrength.getHandType(), is(HandType.STRAIGHT_FLUSH));
        assertThat(handStrength.getCards(), is(sortedHand.getCards()));
    }
}
