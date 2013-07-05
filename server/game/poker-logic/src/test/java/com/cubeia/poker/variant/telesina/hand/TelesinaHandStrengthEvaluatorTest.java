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

package com.cubeia.poker.variant.telesina.hand;

import com.cubeia.poker.hand.*;
import com.cubeia.poker.hand.eval.HandTypeCheckCalculator;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class TelesinaHandStrengthEvaluatorTest {

    TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

    HandTypeCheckCalculator typeCalculator = new HandTypeCheckCalculator(Rank.SEVEN);

    @Test
    public void testCheckStraight() {
        Hand hand = new Hand("TH 7S 9D JS 8C");
        HandStrength straight = eval.checkStraight(hand, 5);
        assertThat(straight.getHandType(), is(HandType.STRAIGHT));
        assertThat(straight.getCards().size(), is(5));

        assertThat(straight.getCards().get(0).getRank(), is(Rank.SEVEN));
        assertThat(straight.getCards().get(1).getRank(), is(Rank.EIGHT));
        assertThat(straight.getCards().get(2).getRank(), is(Rank.NINE));
        assertThat(straight.getCards().get(3).getRank(), is(Rank.TEN));
        assertThat(straight.getCards().get(4).getRank(), is(Rank.JACK));
    }

    @Test
    public void testCheckStraightAceLow() {
        Hand hand = new Hand("TH 7S 9D AC 8C");
        HandStrength straight = eval.checkStraight(hand, 5);
        assertThat(straight.getHandType(), is(HandType.STRAIGHT));
        assertThat(straight.getCards().size(), is(5));

        assertThat(straight.getCards().get(0).getRank(), is(Rank.ACE));
        assertThat(straight.getCards().get(1).getRank(), is(Rank.SEVEN));
        assertThat(straight.getCards().get(2).getRank(), is(Rank.EIGHT));
        assertThat(straight.getCards().get(3).getRank(), is(Rank.NINE));
        assertThat(straight.getCards().get(4).getRank(), is(Rank.TEN));
    }

    @Test
    public void testCheckStraightAceHigh() {
        Hand hand = new Hand("QH JS TD AC KC");
        HandStrength straight = eval.checkStraight(hand, 5);
        assertThat(straight.getHandType(), is(HandType.STRAIGHT));
        assertThat(straight.getCards().size(), is(5));

        assertThat(straight.getCards().get(0).getRank(), is(Rank.TEN));
        assertThat(straight.getCards().get(1).getRank(), is(Rank.JACK));
        assertThat(straight.getCards().get(2).getRank(), is(Rank.QUEEN));
        assertThat(straight.getCards().get(3).getRank(), is(Rank.KING));
        assertThat(straight.getCards().get(4).getRank(), is(Rank.ACE));
    }

    @Test
    public void testCheckStraightFlush() {
        Hand hand = new Hand("TS 7S 9S JS 8S");
        HandStrength straight = eval.checkStraightFlush(hand, 5);
        assertThat(straight.getHandType(), is(HandType.STRAIGHT_FLUSH));
        assertThat(straight.getCards().size(), is(5));

        assertThat(straight.getCards().get(0).getRank(), is(Rank.SEVEN));
        assertThat(straight.getCards().get(1).getRank(), is(Rank.EIGHT));
        assertThat(straight.getCards().get(2).getRank(), is(Rank.NINE));
        assertThat(straight.getCards().get(3).getRank(), is(Rank.TEN));
        assertThat(straight.getCards().get(4).getRank(), is(Rank.JACK));
    }

    @Test
    public void testCheckRoyalStraightFlush() {
        Hand hand = new Hand("TS JS QS KS AS");
        HandStrength straight = eval.checkRoyalStraightFlush(hand, 5);
        assertThat(straight.getHandType(), is(HandType.ROYAL_STRAIGHT_FLUSH));
        assertThat(straight.getCards().size(), is(5));

        assertThat(straight.getCards().get(0).getRank(), is(Rank.TEN));
        assertThat(straight.getCards().get(1).getRank(), is(Rank.JACK));
        assertThat(straight.getCards().get(2).getRank(), is(Rank.QUEEN));
        assertThat(straight.getCards().get(3).getRank(), is(Rank.KING));
        assertThat(straight.getCards().get(4).getRank(), is(Rank.ACE));

        hand = new Hand("QS TS JS QS KS");
        HandStrength gay = eval.checkRoyalStraightFlush(hand, 5);
        assertThat(gay, nullValue());
    }

    @Test
    public void testCheckFullHouse() {
        Hand hand = new Hand("7S 8S 8C 7D 8H");
        HandStrength strength = typeCalculator.checkFullHouse(hand);
        assertThat(strength.getHandType(), is(HandType.FULL_HOUSE));
        assertThat(strength.getCards().size(), is(5));

        assertThat(strength.getCards().get(0).getRank(), is(Rank.EIGHT));
        assertThat(strength.getCards().get(1).getRank(), is(Rank.EIGHT));
        assertThat(strength.getCards().get(2).getRank(), is(Rank.EIGHT));
        assertThat(strength.getCards().get(3).getRank(), is(Rank.SEVEN));
        assertThat(strength.getCards().get(4).getRank(), is(Rank.SEVEN));
    }


    @Test
    public void testFlush() {
        Hand hand = new Hand("7S KS 9S AS JS");
        HandStrength strength = typeCalculator.checkFlush(hand, 5);
        assertThat(strength.getHandType(), is(HandType.FLUSH));

        assertThat(strength.getCards().get(0).getRank(), is(Rank.ACE));
        assertThat(strength.getCards().get(1).getRank(), is(Rank.KING));
        assertThat(strength.getCards().get(2).getRank(), is(Rank.JACK));
        assertThat(strength.getCards().get(3).getRank(), is(Rank.NINE));
        assertThat(strength.getCards().get(4).getRank(), is(Rank.SEVEN));
    }

    @Test
    public void testGetLowestPossibleStraightFlush() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        List<Card> cards = eval.getLowestStraightFlushCards();
        assertThat(cards.size(), is(5));
        assertThat(cards, is(new Hand("AS 7S 8S 9S TS").getCards()));

        eval = new TelesinaHandStrengthEvaluator(Rank.TWO);
        assertThat(eval.getLowestStraightFlushCards(), is(new Hand("AS 2S 3S 4S 5S").getCards()));
        eval = new TelesinaHandStrengthEvaluator(Rank.THREE);
        assertThat(eval.getLowestStraightFlushCards(), is(new Hand("AS 3S 4S 5S 6S").getCards()));
        eval = new TelesinaHandStrengthEvaluator(Rank.FOUR);
        assertThat(eval.getLowestStraightFlushCards(), is(new Hand("AS 4S 5S 6S 7S").getCards()));
        eval = new TelesinaHandStrengthEvaluator(Rank.FIVE);
        assertThat(eval.getLowestStraightFlushCards(), is(new Hand("AS 5S 6S 7S 8S").getCards()));
        eval = new TelesinaHandStrengthEvaluator(Rank.SIX);
        assertThat(eval.getLowestStraightFlushCards(), is(new Hand("AS 6S 7S 8S 9S").getCards()));
        eval = new TelesinaHandStrengthEvaluator(Rank.NINE);
        assertThat(eval.getLowestStraightFlushCards(), is(new Hand("AS 9S TS JS QS").getCards()));
        eval = new TelesinaHandStrengthEvaluator(Rank.TEN);
        assertThat(eval.getLowestStraightFlushCards(), is(new Hand("AS TS JS QS KS").getCards()));
    }
}
