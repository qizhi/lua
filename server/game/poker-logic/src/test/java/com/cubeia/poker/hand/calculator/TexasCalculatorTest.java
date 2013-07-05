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

import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandStrength;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.hand.eval.HandTypeCheckCalculator;
import org.junit.Test;

import static com.cubeia.poker.hand.HandType.*;
import static com.cubeia.poker.hand.Rank.*;
import static org.junit.Assert.*;


public class TexasCalculatorTest {

    HandTypeCheckCalculator calc = new HandTypeCheckCalculator();

    @Test
    public void testIsFlush() throws Exception {
        Hand hand = new Hand("2C 3C 4C 5C AS");
        assertNull(calc.checkFlush(hand));

        hand = new Hand("2C 3C 4C 5C AC");
        assertNotNull(calc.checkFlush(hand));

        // This is not a proper flush...
        hand = new Hand("2C KC");
        assertNull(calc.checkFlush(hand));

        hand = new Hand("2C 3C 4C 5C AC TC");
        HandStrength strength = calc.checkFlush(hand);
        assertEquals(FLUSH, strength.getHandType());
        assertEquals(Rank.ACE, strength.getHighestRank());
        assertNull(strength.getSecondRank());

        hand = new Hand("2C 3C 4H");
        assertNull(calc.checkFlush(hand));

        hand = new Hand("2C 3C");
        assertNull(calc.checkFlush(hand, 3));
    }

    @Test
    public void testIsStraight() throws Exception {
        Hand hand = new Hand("2C 3C 4C 5C AS");
        assertNull(calc.checkStraight(hand));

        hand = new Hand("2C 3C 5C 4C 6H");
        HandStrength strength = calc.checkStraight(hand);
        assertEquals(STRAIGHT, strength.getHandType());
        assertEquals(Rank.SIX, strength.getHighestRank());
        assertNull(strength.getSecondRank());
        
        hand = new Hand("AH 2C 3C 5C 4C");
        strength = calc.checkStraight(hand, true);
        assertEquals(STRAIGHT, strength.getHandType());
        assertEquals(Rank.FIVE, strength.getHighestRank());
        assertNull(strength.getSecondRank());
        
        hand = new Hand("AH 2C 3C 5C 4C");
        strength = calc.checkStraight(hand, false);
        assertNull(calc.checkStraight(hand));
    }

    @Test
    public void testIsStraightFlush() throws Exception {
        Hand hand = new Hand("2C 3C 4C 5C AS");
        assertNull(calc.checkStraightFlush(hand));

        hand = new Hand("2C 3C 4C 5C 6C");
        HandStrength strength = calc.checkStraightFlush(hand);
        assertEquals(STRAIGHT_FLUSH, strength.getHandType());
        assertEquals(Rank.SIX, strength.getHighestRank());
        assertNull(strength.getSecondRank());
    }

    @Test
    public void testHasManyOfAKind() throws Exception {
        Hand hand = new Hand("2C 3C 4C 5C AS");
        assertNull(calc.checkManyOfAKind(hand, 3));

        hand = new Hand("2C 3C 2H 5C 2S");
        HandStrength strength = calc.checkManyOfAKind(hand, 3);
        assertEquals(THREE_OF_A_KIND, strength.getHandType());
        assertEquals(Rank.TWO, calc.checkManyOfAKind(hand, 3).getHighestRank());
        assertEquals(2, strength.getKickerCards().size());
        assertEquals(FIVE, strength.getKickerCards().get(0).getRank());
        assertEquals(THREE, strength.getKickerCards().get(1).getRank());
        assertEquals(5, strength.getCards().size());

        hand = new Hand("2C 2C 2H 5C 2S");
        assertEquals(THREE_OF_A_KIND, calc.checkManyOfAKind(hand, 3).getHandType());
        assertEquals(Rank.TWO, calc.checkManyOfAKind(hand, 3).getHighestRank());

        hand = new Hand("2C 2C 2H 5C 2S");
        assertEquals(THREE_OF_A_KIND, calc.checkManyOfAKind(hand, 3).getHandType());
        assertEquals(Rank.TWO, calc.checkManyOfAKind(hand, 3).getHighestRank());

        hand = new Hand("2C 2C 2H AC AS AH");
        assertEquals(THREE_OF_A_KIND, calc.checkManyOfAKind(hand, 3).getHandType());
        assertEquals(Rank.ACE, calc.checkManyOfAKind(hand, 3).getHighestRank());
    }

    @Test
    public void testHasManyOfAKindOrder() throws Exception {
        Hand hand = new Hand("2C 3C 2C JC AS");
        HandStrength strength = calc.checkManyOfAKind(hand, 2);
        assertEquals(PAIR, strength.getHandType());
        assertEquals(new Card("2C"), strength.getCards().get(0));
        assertEquals(new Card("2C"), strength.getCards().get(1));
        assertEquals(new Card("AS"), strength.getCards().get(2));
        assertEquals(new Card("JC"), strength.getCards().get(3));
        assertEquals(new Card("3C"), strength.getCards().get(4));
    }

    @Test
    public void testQuads() throws Exception {
        Hand hand = new Hand("2C 2H 5D 2H 2C");
        HandStrength strength = calc.checkManyOfAKind(hand, 4);
        assertEquals(FOUR_OF_A_KIND, strength.getHandType());
        assertEquals(TWO, strength.getHighestRank());
    }

    @Test
    public void testOnePair() throws Exception {
        Hand hand = new Hand("2C 2H 5D JH 4C");
        HandStrength strength = calc.checkManyOfAKind(hand, 2);
        assertEquals(PAIR, strength.getHandType());
        assertEquals(TWO, strength.getHighestRank());
        assertNull(strength.getSecondRank());

        // Verify kicker cards
        assertEquals(3, strength.getKickerCards().size());
        assertEquals(JACK, strength.getKickerCards().get(0).getRank());
        assertEquals(FIVE, strength.getKickerCards().get(1).getRank());
        assertEquals(FOUR, strength.getKickerCards().get(2).getRank());
    }

    @Test
    public void testTwoPairs() throws Exception {
        Hand hand = new Hand("JC JH 5D TH TC");
        HandStrength strength = calc.checkTwoPairs(hand);
        assertEquals(TWO_PAIRS, strength.getHandType());
        assertEquals(JACK, strength.getHighestRank());
        assertEquals(Rank.TEN, strength.getSecondRank());
        assertEquals(1, strength.getKickerCards().size());
        assertEquals(FIVE, strength.getKickerCards().get(0).getRank());
    }

    @Test
    public void testFullHouse() throws Exception {
        Hand hand = new Hand("JC JH JD TH TC");
        HandStrength strength = calc.checkFullHouse(hand);
        assertEquals(FULL_HOUSE, strength.getHandType());
        assertEquals(JACK, strength.getHighestRank());
        assertEquals(Rank.TEN, strength.getSecondRank());
    }

    @Test
    public void testFullHouseOrder() throws Exception {
        Hand hand = new Hand("JC JH TD TH TC");
        HandStrength strength = calc.checkFullHouse(hand);
        assertEquals(FULL_HOUSE, strength.getHandType());
        assertEquals(TEN, strength.getHighestRank());
        assertEquals(JACK, strength.getSecondRank());
        assertEquals(Rank.TEN, strength.getCards().get(0).getRank());
        assertEquals(Rank.TEN, strength.getCards().get(1).getRank());
        assertEquals(Rank.TEN, strength.getCards().get(2).getRank());
        assertEquals(Rank.JACK, strength.getCards().get(3).getRank());
        assertEquals(Rank.JACK, strength.getCards().get(4).getRank());
        assertEquals(Rank.TEN, strength.getGroup(0).get(0).getRank());
    }

    @Test
    public void testHighCard() throws Exception {
        Hand hand = new Hand("2C 4H JD TH AC");
        HandStrength strength = calc.checkHighCard(hand);
        assertEquals(HIGH_CARD, strength.getHandType());
        assertEquals(ACE, strength.getHighestRank());
        assertEquals(JACK, strength.getSecondRank());
        assertEquals(5, strength.getKickerCards().size());
        assertEquals(ACE, strength.getKickerCards().get(0).getRank());
        assertEquals(JACK, strength.getKickerCards().get(1).getRank());
        assertEquals(TEN, strength.getKickerCards().get(2).getRank());
        assertEquals(FOUR, strength.getKickerCards().get(3).getRank());
        assertEquals(TWO, strength.getKickerCards().get(4).getRank());
    }

    @SuppressWarnings("unused")
    @Test
    public void testHighCard2() throws Exception {
        Hand hand = new Hand("2C");
        HandStrength strength = calc.checkHighCard(hand);
        //assertEquals(HIGH_CARD, strength.getHandType());
    }

}
