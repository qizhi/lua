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
import com.cubeia.poker.variant.telesina.TelesinaCardComparator;

import java.io.Serializable;
import java.util.*;

import static com.cubeia.poker.hand.HandType.ROYAL_STRAIGHT_FLUSH;

/**
 * This class is a specialization of HandStrengthComparator for Telesina rules
 * which differs from the vanilla poker rules.
 */
public class TelesinaHandComparator implements Comparator<Hand>, Serializable {

    private TelesinaHandStrengthEvaluator evaluator;

    private final static Hand HIGHEST_ROYAL_STRAIGHT_FLUSH = new Hand("TH JH QH KH AH");

    private final int playersInPot;


    /**
     * Needed by JBoss serialization.
     */
    @SuppressWarnings("unused")
    private TelesinaHandComparator() {
        playersInPot = 0;
    }

    /**
     * Create a new telesina hand comparator. Package private as this comparator
     * must be created by the factory method: {@link TelesinaHandStrengthEvaluator#createHandComparator(int)}.
     *
     * @param evaluator
     * @param playersInPot
     */
    TelesinaHandComparator(TelesinaHandStrengthEvaluator evaluator, int playersInPot) {
        this.evaluator = evaluator;
        this.playersInPot = playersInPot;
    }

    public int compare(Hand h1, Hand h2) {
        HandStrength c1Strength = evaluator.getBestHandStrength(h1);
        HandStrength c2Strength = evaluator.getBestHandStrength(h2);

        if (playersInPot == 2 && checkForRoyals(c1Strength, c2Strength)) {
            List<Card> highestRoyal = HIGHEST_ROYAL_STRAIGHT_FLUSH.getCards();
            List<Card> lowestRoyal = evaluator.getLowestStraightFlushCards();

            if (h1.containsAllCardsRegardlessOfId(highestRoyal) && h2.containsAllCardsRegardlessOfId(lowestRoyal)) {
                return -1;
            } else if (h2.containsAllCardsRegardlessOfId(highestRoyal) && h1.containsAllCardsRegardlessOfId(lowestRoyal)) {
                return 1;
            }
        }

        if (c1Strength.getHandType() != c2Strength.getHandType()) {
            return c1Strength.getHandType().telesinaHandTypeValue - c2Strength.getHandType().telesinaHandTypeValue;
        }

        if (c1Strength.getHandType() == HandType.FLUSH) {
            Suit c1Suit = c1Strength.getGroup(0).get(0).getSuit();
            Suit c2Suit = c2Strength.getGroup(0).get(0).getSuit();

            if (c1Suit != c2Suit) {
                return c1Suit.telesinaSuitValue - c2Suit.telesinaSuitValue;
            }
        }

        if (c1Strength.getGroupSize() != c2Strength.getGroupSize()) {
            throw new IllegalStateException("Comparison groups in strength not of same size for two hands of type " + c1Strength.getHandType());
        }

        for (int i = 0; i < c1Strength.getGroupSize(); i++) {
            int compare = compareKickers(c1Strength.getGroup(i), c2Strength.getGroup(i));
            if (compare != 0) {
                return compare;
            }
        }

        return 0;
    }

    /**
     * Returns true if any of the given hand strengths contains a royal straight flush.
     */
    private boolean checkForRoyals(HandStrength c1Strength, HandStrength c2Strength) {
        return c1Strength.getHandType() == ROYAL_STRAIGHT_FLUSH || c2Strength.getHandType() == ROYAL_STRAIGHT_FLUSH;
    }

    private int compareKickers(List<Card> c1, List<Card> c2) {
        if (c1.size() != c2.size()) {
            throw new IllegalArgumentException("Only kicker lists of equal length may be compared");
        }

        List<Card> c1copy = new LinkedList<Card>(c1);
        List<Card> c2copy = new LinkedList<Card>(c2);

        Collections.sort(c1copy, TelesinaCardComparator.DESC);
        Collections.sort(c2copy, TelesinaCardComparator.DESC);

        Iterator<Card> c1iter = c1copy.iterator();
        Iterator<Card> c2iter = c2copy.iterator();

        while (c1iter.hasNext() && c2iter.hasNext()) {
            int cmp = TelesinaCardComparator.ASC.compare(c1iter.next(), c2iter.next());
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }
}
