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

import java.util.Collections;
import java.util.Comparator;

/**
 * Comparator for cards that compares the rank and suit.
 * The order of the cards are compatible with the order of the
 * {@link Suit} and {@link Rank} enums.
 * An Ace is greater than a King etc.
 * The card id is not compared.
 *
 * @author w
 */
public class CardComparator implements Comparator<Card> {
    public static final Comparator<Card> ASC = new CardComparator();
    public static final Comparator<Card> DESC = Collections.reverseOrder(ASC);

    @Override
    public int compare(Card c1, Card c2) {
        int comp = c1.getRank().ordinal() * 1000 - c2.getRank().ordinal() * 1000;
        comp += c1.getSuit().ordinal() - c2.getSuit().ordinal();
        return comp;
    }

}
