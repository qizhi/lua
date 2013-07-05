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
import com.cubeia.poker.hand.HandStrength;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.variant.texasholdem.TexasHoldemHandCalculator;
import org.junit.Test;

import static com.cubeia.poker.hand.HandType.*;
import static com.cubeia.poker.hand.Rank.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class HandCalculatorHandStrengthTest {

    HandCalculator calc = new TexasHoldemHandCalculator();

    @Test
    public void testHandStrength_1() throws Exception {
        Hand hand = new Hand("2C 3C 4C 5C 6C");
        HandStrength strength = calc.getHandStrength(hand);
        assertEquals(STRAIGHT_FLUSH, strength.getHandType());
        assertEquals(SIX, strength.getHighestRank());
    }

    @Test
    public void testHandStrength_2() throws Exception {
        Hand hand = new Hand("2C 2H 4C 5C 2S");
        HandStrength strength = calc.getHandStrength(hand);
        assertEquals(THREE_OF_A_KIND, strength.getHandType());
        assertEquals(TWO, strength.getHighestRank());
        assertNull(strength.getSecondRank());
        assertEquals(2, strength.getKickerCards().size());
        assertEquals(FIVE, strength.getKickerCards().get(0).getRank());
        assertEquals(FOUR, strength.getKickerCards().get(1).getRank());
    }

    @Test
    public void testHandStrength_3() throws Exception {
        Hand hand = new Hand("2C KC 4C 5C JC");
        HandStrength strength = calc.getHandStrength(hand);
        assertEquals(FLUSH, strength.getHandType());
        assertEquals(KING, strength.getHighestRank());
    }

    @Test
    public void testHandStrength_4() throws Exception {
        Hand hand = new Hand("2C 2H JC 2D JC");
        HandStrength strength = calc.getHandStrength(hand);
        assertEquals(FULL_HOUSE, strength.getHandType());
        assertEquals(TWO, strength.getHighestRank());
        assertEquals(JACK, strength.getSecondRank());
    }

    @Test
    public void testHandStrength_5() throws Exception {
        Hand hand = new Hand("8C QH 8D QC KS");
        HandStrength strength = calc.getHandStrength(hand);
        assertEquals(TWO_PAIRS, strength.getHandType());
        assertEquals(Rank.QUEEN, strength.getHighestRank());
        assertEquals(Rank.EIGHT, strength.getSecondRank());
        assertEquals(1, strength.getKickerCards().size());
        assertEquals(KING, strength.getKickerCards().get(0).getRank());
    }

    @Test
    public void testHandStrength_6() throws Exception {
        Hand hand = new Hand("2C 4H 6D 8C KS");
        HandStrength strength = calc.getHandStrength(hand);
        assertEquals(HIGH_CARD, strength.getHandType());
        assertEquals(Rank.KING, strength.getHighestRank());
        assertEquals(Rank.EIGHT, strength.getSecondRank());
        assertEquals(5, strength.getKickerCards().size());
        assertEquals(KING, strength.getKickerCards().get(0).getRank());
    }
    
    @Test
    public void testHandStrength_wheel() throws Exception {
        Hand hand = new Hand("2C 4H 3D AC 5S");
        HandStrength strength = calc.getHandStrength(hand);
        assertEquals(STRAIGHT, strength.getHandType());
        assertEquals(Rank.FIVE, strength.getHighestRank());
    }

    @Test
    public void testHandStrengthStraight() throws Exception {
        Hand hand = new Hand("AS KD QC JH TD");
        HandStrength strength = calc.getHandStrength(hand);
        assertEquals(STRAIGHT, strength.getHandType());
        assertEquals(Rank.ACE, strength.getHighestRank());
    }
}
