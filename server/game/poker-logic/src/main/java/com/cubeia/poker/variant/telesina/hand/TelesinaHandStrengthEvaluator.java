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
import com.cubeia.poker.hand.calculator.ByRankCardComparator;
import com.cubeia.poker.hand.eval.HandTypeCheckCalculator;

import java.io.Serializable;
import java.util.*;

import static com.cubeia.poker.hand.Suit.SPADES;

@SuppressWarnings("unchecked")
public class TelesinaHandStrengthEvaluator implements HandTypeEvaluator, Serializable {

    private static final long serialVersionUID = 1L;

    private HandTypeCheckCalculator typeCalculator;

    private final Rank deckLowestRank;

    /**
     * Create a hand strength evaluator fo a given telesina deck. A
     * lowest Rank of Rank.TWO corresponds to a full deck.
     *
     * @param deckLowestRank
     */
    public TelesinaHandStrengthEvaluator(Rank deckLowestRank) {
        this.deckLowestRank = deckLowestRank;
        typeCalculator = new HandTypeCheckCalculator(deckLowestRank);
    }

    @Override
    public HandInfo getBestHandInfo(Hand hand) {
        return getBestHandStrength(hand);
    }

    @Override
    public Comparator<Hand> createHandComparator(int playersInPot) {
        return new TelesinaHandComparator(this, playersInPot);
    }

    private List<Card> findBestHand(Hand hand) {
        TelesinaHandComparator comp = new TelesinaHandComparator(this, 1);
        Combinator<Card> comb = new Combinator<Card>(hand.getCards(), 5);
        List<Card> best = comb.next();

        while (comb.hasNext()) {
            List<Card> candidate = comb.next();
            if (comp.compare(new Hand(candidate), new Hand(best)) > 0) {
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Returns a list of cards (without id:s) that forms the
     * lowest possible straight flush for the given deck size.
     * The cards are all spades. For a deck with lowest rank = 3 this is:
     * AS 3S 4S 5S 6S
     *
     * @return lowest possible straight flush
     */
    public List<Card> getLowestStraightFlushCards() {
        int lowestOrdinal = deckLowestRank.ordinal();

        Rank[] ranks = Rank.values();
        Card c0 = new Card(Rank.ACE, SPADES);
        Card c1 = new Card(ranks[lowestOrdinal + 0], SPADES);
        Card c2 = new Card(ranks[lowestOrdinal + 1], SPADES);
        Card c3 = new Card(ranks[lowestOrdinal + 2], SPADES);
        Card c4 = new Card(ranks[lowestOrdinal + 3], SPADES);

        return Arrays.asList(c0, c1, c2, c3, c4);
    }

    /**
     * Find the strength of the best hand that can be built using a list of cards.
     *
     * @param hand
     * @return
     */
    public HandStrength getBestHandStrength(Hand hand) {
        List<Card> cards = hand.getCards();

        if (cards.size() > 5) {
            cards = findBestHand(hand);
            hand = new Hand(cards);
        }

        HandStrength strength = null;

        // ROYAL_STRAIGHT_FLUSH
        if (strength == null) {
            strength = checkRoyalStraightFlush(hand, 5);
        }

        // STRAIGHT_FLUSH
        if (strength == null) {
            strength = checkStraightFlush(hand, 5);
        }

        // FOUR_OF_A_KIND
        if (strength == null) {
            strength = typeCalculator.checkManyOfAKind(hand, 4);
        }

        // FLUSH
        if (strength == null) {
            strength = typeCalculator.checkFlush(hand, 5);
        }

        // FULL_HOUSE
        if (strength == null) {
            strength = typeCalculator.checkFullHouse(hand);
        }

        // STRAIGHT
        if (strength == null) {
            strength = checkStraight(hand, 5);
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

    /**
     * Checks to see if ALL cards (any number) supplied form a straight, aces low allowed.
     * Deck may be stripped.
     *
     * @param hand         The cards to check
     * @param minimumLength The minimum length required for a set of card to count as a straight (NOTE a one card straight is never recognized)
     * @return
     */
    public HandStrength checkStraight(Hand hand, int minimumLength) {
        if (hand.getNumberOfCards() < minimumLength) {
            return null;
        }

        HandStrength checkStraightAcesHigh = typeCalculator.checkStraight(hand);
        if (checkStraightAcesHigh != null) {
            return checkStraightAcesHigh;
        }

        return typeCalculator.checkStraight(hand, true);
    }

    /**
     * Checks to see if ALL cards (any number) supplied form a straight flush.
     * Deck may be stripped.
     *
     * @param hand         The cards to check
     * @param minimumLength The minimum length required for a set of card to count as a straight
     * @return
     */
    public HandStrength checkStraightFlush(Hand hand, int minimumLength) {
        if (typeCalculator.checkFlush(hand, minimumLength) == null) {
            return null;
        }

        HandStrength checkStraight = checkStraight(hand, minimumLength);

        if (checkStraight == null) {
            return null;
        }

        List<Card> sorted = new ArrayList<Card>(hand.getCards());
        Collections.sort(sorted, ByRankCardComparator.ACES_LOW_ASC);
        return new HandStrength(HandType.STRAIGHT_FLUSH, sorted, hand.getCards());
    }

    /**
     * Checks for a Royal Straight Flush.
     *
     * @param hand         The cards to check
     * @param minimumLength The minimum length required for a set of card to count as a straight
     * @return hand strength, null if not a royal straight flush
     * @see #checkStraightFlush(Hand, int)
     */
    public HandStrength checkRoyalStraightFlush(Hand hand, int minimumLength) {
        HandStrength strength = checkStraightFlush(hand, minimumLength);

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
