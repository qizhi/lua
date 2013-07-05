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

import com.cubeia.poker.hand.*;
import com.cubeia.poker.hand.calculator.ByRankCardComparator;
import com.cubeia.poker.hand.calculator.HandCalculator;
import com.cubeia.poker.hand.eval.HandTypeCheckCalculator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.min;

/**
 * <p>Texas Holdem implementation of a Hand Calculator. This is probably
 * the common calculations for most poker games, but variations do exist.</p>
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class TexasHoldemHandCalculator implements HandCalculator, HandTypeEvaluator, Serializable {

    private HandTypeCheckCalculator typeCalculator = new HandTypeCheckCalculator();

    @Override
    public HandInfo getBestHandInfo(Hand hand) {
        int minCards = min(5, hand.getCards().size());
        return getBestCombinationHandStrength(hand, minCards);
    }

    @Override
    public Comparator<Hand> createHandComparator(int playersInPot) {
        return Collections.reverseOrder(new TexasHoldemHandComparator());
    }

    /**
     * Get all possible hand combinations and rank them.
     *
     * @param hand with more than 5 cards
     * @return the best HandStrength found.
     */
    protected HandStrength getBestCombinationHandStrength(Hand hand, int minElements) {
        if (hand == null || hand.getCards() == null || hand.getCards().isEmpty()) {
            return new HandStrength(HandType.NOT_RANKED);
        }
        List<HandStrength> allPossibleHands = new ArrayList<HandStrength>();
        Combinator<Card> combinator = new Combinator<Card>(hand.getCards(), minElements);
        for (List<Card> cards : combinator) {
            HandStrength handStrength = getHandStrength(new Hand(cards));
            allPossibleHands.add(handStrength);
        }

        if (allPossibleHands.isEmpty()) {
            throw new IllegalArgumentException("calculated 0 possible hands from cards: " + hand.toString());
        }

        Collections.sort(allPossibleHands, new HandStrengthComparator());
        return allPossibleHands.get(0);
    }

    /* ----------------------------------------------------
      *
      * 	PUBLIC METHODS
      *
      *  ---------------------------------------------------- */

    /* (non-Javadoc)
      * @see com.cubeia.poker.hand.calculator.HandCalculator#getHandStrength(com.cubeia.poker.hand.Hand)
      */
    @Override
    public HandStrength getHandStrength(Hand hand) {
        HandStrength strength = null;

        if(strength == null) {
            strength = checkRoyalStraightFlush(hand);
        }

        // STRAIGHT_FLUSH
        if (strength == null) {
            strength = typeCalculator.checkStraightFlush(hand);
        }

        // FOUR_OF_A_KIND
        if (strength == null) {
            strength = typeCalculator.checkManyOfAKind(hand, 4);
        }

        // FULL_HOUSE
        if (strength == null) {
            strength = typeCalculator.checkFullHouse(hand);
        }

        // FLUSH
        if (strength == null) {
            strength = typeCalculator.checkFlush(hand);
        }

        // STRAIGHT
        if (strength == null) {
            strength = typeCalculator.checkAcesHighAndLowStraight(hand);
        }

        // THREE_OF_A_KIND
        if (strength == null) {
            strength = typeCalculator.checkManyOfAKind(hand, 3);
        }

        // TWO_PAIRS
        if (strength == null) {
            strength = typeCalculator.checkTwoPairs(hand);
        }

        // ONE_PAIR
        if (strength == null) {
            strength = typeCalculator.checkManyOfAKind(hand, 2);
        }

        // HIGH_CARD
        if (strength == null) {
            strength = typeCalculator.checkHighCard(hand);
        }

        if (strength == null) {
            strength = new HandStrength(HandType.NOT_RANKED);
        }

        return strength;
    }

    private HandStrength checkRoyalStraightFlush(Hand hand) {
        HandStrength strength = typeCalculator.checkStraightFlush(hand);

        if (strength == null) {
            return null;
        }

        List<Card> cards = strength.getCards();
        if (containsRank(cards, Rank.KING) && containsRank(cards, Rank.ACE)) {
            List<Card> sorted = new ArrayList<Card>(hand.getCards());
            Collections.sort(sorted, ByRankCardComparator.ACES_HIGH_ASC);
            return new HandStrength(HandType.ROYAL_STRAIGHT_FLUSH, sorted, hand.getCards());
        } else {
            return null;
        }
    }

    private boolean containsRank(List<Card> cards, Rank rank) {
        for (Card c : cards) {
            if (c.getRank() == rank) {
                return true;
            }
        }
        return false;
    }

}
