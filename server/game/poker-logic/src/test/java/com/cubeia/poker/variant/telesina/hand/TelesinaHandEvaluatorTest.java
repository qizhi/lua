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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TelesinaHandEvaluatorTest {

    @Test
    public void testEvaluatorIncludesHighCard() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("KS QD TD 9H 7D 8C"));

        assertEquals(HandType.HIGH_CARD, best.getHandType());
        assertEquals(5, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("KS QD TD 9H 8C")));
    }

    @Test
    public void testEvaluatorIncludesHandPair() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("AS QD TD 9H 7D 9C"));

        System.out.println("HandInfo: " + best);

        assertEquals(HandType.PAIR, best.getHandType());
        assertEquals(5, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("TD AS QD 9H 9C")));
    }

    @Test
    public void testEvaluatorIncludesHandTwoPair() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("AS QD QS 9H 7D 9C"));

        assertEquals(HandType.TWO_PAIRS, best.getHandType());
        assertEquals(5, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("AS QD QS 9H 9C")));
    }

    @Test
    public void testEvaluatorIncludesHandThreeOfAKind() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("7D 9C AS QD QS QC"));

        assertEquals(HandType.THREE_OF_A_KIND, best.getHandType());
        assertEquals(5, best.getCards().size());

        assertTrue(best.getCards().containsAll(Card.list("QD QS QC AS 9C")));
    }

    @Test
    public void testEvaluatorIncludesHandStraight1() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("TS AS KC JD 9H QS"));

        assertEquals(HandType.STRAIGHT, best.getHandType());
        assertEquals(5, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("AS KC QS JD TS")));
    }

    @Test
    public void testEvaluatorIncludesHandStraight2() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("7C KC QS JD TS 9H"));

        assertEquals(HandType.STRAIGHT, best.getHandType());
        assertEquals(5, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("KC QS JD TS 9H")));
    }

    @Test
    public void testEvaluatorIncludesHandFullHouse() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("QH QD QC TS JH JD"));

        assertEquals(HandType.FULL_HOUSE, best.getHandType());
        assertEquals(5, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("QH QD QC JD JH")));
    }

    @Test
    public void testEvaluatorIncludesHandFlush() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("8S 9S 9H 9C JS KS AS"));

        assertEquals(HandType.FLUSH, best.getHandType());
        assertEquals(5, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("8S 9S JS KS AS")));
    }

    @Test
    public void testEvaluatorIncludesHandFourOfAKind() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("8S 9S 9H 9C JS 9D AS"));

        assertEquals(HandType.FOUR_OF_A_KIND, best.getHandType());
        assertEquals(5, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("9S 9H 9C 9D AS")));
    }

    @Test
    public void testEvaluatorIncludesHandShortHandHighCard() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("JS KS AS"));

        assertEquals(HandType.HIGH_CARD, best.getHandType());
        assertEquals(3, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("AS KS JS")));
    }

    @Test
    public void testEvaluatorIncludesHandShortHandPair() {
        TelesinaHandStrengthEvaluator eval = new TelesinaHandStrengthEvaluator(Rank.SEVEN);

        HandInfo best = eval.getBestHandInfo(new Hand("JS KS KD"));

        assertEquals(HandType.PAIR, best.getHandType());
        assertEquals(3, best.getCards().size());
        assertTrue(best.getCards().containsAll(Card.list("JS KS KD")));
    }
}
