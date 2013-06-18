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

import java.io.Serializable;
import java.util.*;

import static java.util.Collections.reverseOrder;

/**
 * TODO: we should consider making hand immutable
 *
 * @author w
 */
public class Hand implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static int MAX_CARDS = 7;

    private List<Card> cards = new ArrayList<Card>();

    //private HandStrength handStrength = new HandStrength(HandType.NOT_RANKED);

    public Hand() {
    }

    public Hand(String cs) {
        StringTokenizer t = new StringTokenizer(cs, " -");
        while (t.hasMoreTokens()) {
            String s = t.nextToken();
            if (s.length() == 2) {
                Card c = new Card(s);
                addCard(c);
            }
        }
    }

    public Hand(Collection<Card> cards) {
        this.cards.addAll(cards);
    }

    /**
     * Copy constructor.
     *
     * @param otherHand
     */
    public Hand(Hand otherHand) {
        this.cards = new ArrayList<Card>(otherHand.getCards());
        //this.handStrength = new HandStrength(otherHand.handStrength);
    }

    public void removeCard(int cardNumber) {
        cards.remove(cardNumber);
    }

    @Override
    public String toString() {
        String s = "";
        for (Card card : cards) {
            s += card.toString() + " ";
        }
        return s;
    }

    /**
     * Will return a defensive copy of the cards.
     * Changes to the returned list are not reflected in the list
     * contained in this hand object.
     *
     * @return List of cards, never null.
     */
    public List<Card> getCards() {
        return new ArrayList<Card>(cards);
    }

    /**
     * Add a card to the hand.
     *
     * @param card card to add
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    public void addCards(Collection<Card> cardsToAdd) {
        cards.addAll(cardsToAdd);
    }

    /**
     * Sort all cards in an descending order.
     *
     * @return A new hand with cards sorted. Changes to this hand are not
     *         reflected in the supplied hand.
     */
    public Hand sort() {
        List<Card> sortedCards = new ArrayList<Card>(cards);
        Collections.sort(sortedCards, reverseOrder(new CardComparator()));
        return new Hand(sortedCards);
    }

    public Card getCardAt(int index) {
        return cards.get(index);
    }

    public void clear() {
        cards.clear();
    }

    public int getNumberOfCards() {
        return cards.size();
    }

    /**
     * Returns true if the given hand contains all cards in the given list regardless
     * of card id and order.
     *
     * @param cardsToCheck cards to check for inclusion in the hand
     * @return true if all cards contained, false otherwise
     */
    public boolean containsAllCardsRegardlessOfId(List<Card> cardsToCheck) {
        List<Card> handCardsWithoutIds = Card.makeCopyWithoutIds(cards);
        List<Card> givenCardsWithoutIds = Card.makeCopyWithoutIds(cardsToCheck);
        return handCardsWithoutIds.containsAll(givenCardsWithoutIds);
    }

}
