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

package com.cubeia.poker.hand;

import com.cubeia.poker.variant.texasholdem.TexasHoldemHandCalculator;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cubeia.poker.hand.HandType.*;
import static com.cubeia.poker.hand.Rank.*;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


public class HandStrengthComparatorTest {

    @Test
    public void testTrivial() throws Exception {
        List<HandStrength> list = new ArrayList<HandStrength>();

        HandStrength strength1 = new HandStrength(THREE_OF_A_KIND);
        HandStrength strength2 = new HandStrength(STRAIGHT);
        HandStrength strength3 = new HandStrength(FULL_HOUSE);
        HandStrength strength4 = new HandStrength(STRAIGHT_FLUSH);

        list.add(strength1);
        list.add(strength2);
        list.add(strength3);
        list.add(strength4);

        Collections.sort(list, new HandStrengthComparator());

        assertEquals(STRAIGHT_FLUSH, list.get(0).getHandType());
        assertEquals(FULL_HOUSE, list.get(1).getHandType());
        assertEquals(STRAIGHT, list.get(2).getHandType());
        assertEquals(THREE_OF_A_KIND, list.get(3).getHandType());
    }

    @Test
    public void testDifferentPairs() throws Exception {
        List<HandStrength> list = new ArrayList<HandStrength>();

        HandStrength strength1 = new HandStrength(PAIR);
        strength1.setHighestRank(TEN);

        HandStrength strength2 = new HandStrength(PAIR);
        strength2.setHighestRank(KING);

        HandStrength strength3 = new HandStrength(PAIR);
        strength3.setHighestRank(ACE);

        list.add(strength1);
        list.add(strength2);
        list.add(strength3);

        Collections.sort(list, new HandStrengthComparator());

        assertEquals(ACE, list.get(0).getHighestRank());
        assertEquals(KING, list.get(1).getHighestRank());
        assertEquals(TEN, list.get(2).getHighestRank());
    }

    @Test
    public void testDifferentTwoPairs() throws Exception {
        List<HandStrength> list = new ArrayList<HandStrength>();

        HandStrength strength1 = new HandStrength(TWO_PAIRS);
        strength1.setHighestRank(ACE);
        strength1.setSecondRank(TWO);

        HandStrength strength2 = new HandStrength(TWO_PAIRS);
        strength2.setHighestRank(ACE);
        strength2.setSecondRank(JACK);

        HandStrength strength3 = new HandStrength(TWO_PAIRS);
        strength3.setHighestRank(ACE);
        strength3.setSecondRank(KING);

        list.add(strength1);
        list.add(strength2);
        list.add(strength3);

        Collections.sort(list, new HandStrengthComparator());

        assertEquals(KING, list.get(0).getSecondRank());
        assertEquals(JACK, list.get(1).getSecondRank());
        assertEquals(TWO, list.get(2).getSecondRank());
    }

    @Test
    public void testTwoPairKicker() throws Exception {
        List<HandStrength> list = new ArrayList<HandStrength>();

        HandStrength strength1 = new HandStrength(TWO_PAIRS);
        strength1.setHighestRank(ACE);
        strength1.setSecondRank(JACK);
        List<Card> kickers = new ArrayList<Card>();
        kickers.add(new Card("4D"));
        strength1.setKickerCards(kickers);

        HandStrength strength2 = new HandStrength(TWO_PAIRS);
        strength2.setHighestRank(ACE);
        strength2.setSecondRank(JACK);
        kickers = new ArrayList<Card>();
        kickers.add(new Card("JH"));
        strength2.setKickerCards(kickers);

        HandStrength strength3 = new HandStrength(TWO_PAIRS);
        strength3.setHighestRank(ACE);
        strength3.setSecondRank(JACK);
        kickers = new ArrayList<Card>();
        kickers.add(new Card("TS"));
        strength3.setKickerCards(kickers);

        list.add(strength1);
        list.add(strength2);
        list.add(strength3);

        Collections.sort(list, new HandStrengthComparator());

        assertEquals(JACK, list.get(0).getKickerCards().get(0).getRank());
        assertEquals(TEN, list.get(1).getKickerCards().get(0).getRank());
        assertEquals(FOUR, list.get(2).getKickerCards().get(0).getRank());
    }

    @Test
    public void testSinglePairKicker() throws Exception {
        List<HandStrength> list = new ArrayList<HandStrength>();

        HandStrength strength1 = new HandStrength(PAIR);
        strength1.setHighestRank(ACE);
        List<Card> kickers = new ArrayList<Card>();
        kickers.add(new Card("AD"));
        kickers.add(new Card("QD"));
        kickers.add(new Card("2D"));
        strength1.setKickerCards(kickers);

        HandStrength strength2 = new HandStrength(PAIR);
        strength2.setHighestRank(ACE);
        kickers = new ArrayList<Card>();
        kickers.add(new Card("AH"));
        kickers.add(new Card("KH"));
        kickers.add(new Card("8H"));
        strength2.setKickerCards(kickers);

        HandStrength strength3 = new HandStrength(PAIR);
        strength3.setHighestRank(ACE);
        kickers = new ArrayList<Card>();
        kickers.add(new Card("AS"));
        kickers.add(new Card("KS"));
        kickers.add(new Card("TS"));
        strength3.setKickerCards(kickers);

        list.add(strength1);
        list.add(strength2);
        list.add(strength3);

        Collections.sort(list, new HandStrengthComparator());

        assertEquals(TEN, list.get(0).getKickerCards().get(2).getRank());
        assertEquals(EIGHT, list.get(1).getKickerCards().get(2).getRank());
        assertEquals(TWO, list.get(2).getKickerCards().get(2).getRank());

        assertEquals(strength3, list.get(0));
        assertEquals(strength2, list.get(1));
        assertEquals(strength1, list.get(2));
    }

    @Test
    public void testHighCard() throws Exception {
        List<HandStrength> list = new ArrayList<HandStrength>();

        HandStrength strength1 = new HandStrength(HIGH_CARD);
        strength1.setHighestRank(ACE);
        strength1.setSecondRank(KING);
        List<Card> kickers = new ArrayList<Card>();
        kickers.add(new Card("Qd"));
        kickers.add(new Card("JD"));
        kickers.add(new Card("2D"));
        kickers.add(new Card("Kh"));
        kickers.add(new Card("Ah"));
        strength1.setKickerCards(kickers);

        HandStrength strength2 = new HandStrength(HIGH_CARD);
        strength2.setHighestRank(ACE);
        strength2.setSecondRank(KING);
        kickers = new ArrayList<Card>();
        kickers.add(new Card("qs"));
        kickers.add(new Card("jH"));
        kickers.add(new Card("9H"));
        kickers.add(new Card("Kh"));
        kickers.add(new Card("Ah"));
        strength2.setKickerCards(kickers);

        HandStrength strength3 = new HandStrength(HIGH_CARD);
        strength3.setHighestRank(ACE);
        strength3.setSecondRank(KING);
        kickers = new ArrayList<Card>();
        kickers.add(new Card("Qh"));
        kickers.add(new Card("JS"));
        kickers.add(new Card("5S"));
        kickers.add(new Card("Kh"));
        kickers.add(new Card("Ah"));
        strength3.setKickerCards(kickers);

        list.add(strength1);
        list.add(strength2);
        list.add(strength3);

        Collections.sort(list, new HandStrengthComparator());

        strength1.toString();

        assertEquals(QUEEN, list.get(0).getKickerCards().get(0).getRank());

        assertEquals(strength2, list.get(0));
        assertEquals(strength3, list.get(1));
        assertEquals(strength1, list.get(2));
    }


    @Test
    public void testSameStrength() throws Exception {
        List<HandStrength> list = new ArrayList<HandStrength>();

        HandStrength strength1 = new HandStrength(TWO_PAIRS);
        strength1.setHighestRank(ACE);
        strength1.setSecondRank(JACK);
        List<Card> kickers = new ArrayList<Card>();
        kickers.add(new Card("4D"));
        strength1.setKickerCards(kickers);

        HandStrength strength2 = new HandStrength(TWO_PAIRS);
        strength2.setHighestRank(ACE);
        strength2.setSecondRank(JACK);
        kickers = new ArrayList<Card>();
        kickers.add(new Card("4H"));
        strength2.setKickerCards(kickers);

        HandStrength strength3 = new HandStrength(TWO_PAIRS);
        strength3.setHighestRank(ACE);
        strength3.setSecondRank(JACK);
        kickers = new ArrayList<Card>();
        kickers.add(new Card("4S"));
        strength3.setKickerCards(kickers);

        list.add(strength1);
        list.add(strength2);
        list.add(strength3);

        Collections.sort(list, new HandStrengthComparator());

        // Collection should not be touched since all are the same
        assertEquals(strength1, list.get(0));
        assertEquals(strength2, list.get(1));
        assertEquals(strength3, list.get(2));
    }


    @Test
    public void testStraightFlush() {
        TexasHoldemHandCalculator calculator = new TexasHoldemHandCalculator();
        Hand hand1 = new Hand("KC QC JC TC 9C");
        HandStrength handStrength1 = calculator.getHandStrength(hand1);

        Hand hand2 = new Hand("QC JC TC 9C 8C");
        HandStrength handStrength2 = calculator.getHandStrength(hand2);

        HandStrengthComparator hcp = new HandStrengthComparator();

        List<HandStrength> hands = new ArrayList<HandStrength>();
        hands.add(handStrength2);
        hands.add(handStrength1);

        Collections.sort(hands,new HandStrengthComparator());

        assertEquals(handStrength1, hands.get(0));
        assertEquals(handStrength2, hands.get(1));



    }

}
