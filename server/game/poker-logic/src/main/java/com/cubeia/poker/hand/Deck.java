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
import java.util.List;

/**
 * A deck of cards. A deck is stateful and remembers it's shuffled order of cards
 * as well as a which cards has been dealt.
 *
 * @author w
 */
public interface Deck extends Serializable {

    /**
     * Deals a card. Picks the next card in the deck and removes it.
     *
     * @return the dealt card, null if deck is empty
     */
    public Card deal();

    /**
     * Gets the lowest rank used in this deck.
     *
     * @return the lowest rank used in this deck
     */
    Rank getDeckLowestRank();

    /**
     * Gets the total number of cards in this deck.
     *
     * @return the total number of cards in this deck
     */
    int getTotalNumberOfCardsInDeck();

    /**
     * Returns true if all cards have been dealt.
     *
     * @return true if deck is empty
     */
    boolean isEmpty();

    /**
     * Returns a list (copy) of all cards in the deck including dealt cards.
     * The returned list won't reflect the shuffling of the deck.
     *
     * @return list of cards
     */
    List<Card> getAllCards();

}
