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

package com.cubeia.poker.variant.telesina;

import com.cubeia.poker.hand.Card;

import java.util.Collections;
import java.util.Comparator;

/**
 * Compares two cards telesina style. Note an ACE will always be better than any other
 * Rank card. So an ACE used as a low card in a straight will still compare as a high card
 * if compared to another card.
 * This comparator excludes the id of the card from the comparison.
 */
public class TelesinaCardComparator implements Comparator<Card> {
    public static final Comparator<Card> ASC = new TelesinaCardComparator();
    public static final Comparator<Card> DESC = Collections.reverseOrder(ASC);

    public TelesinaCardComparator() {
    }

    @Override
    public int compare(Card c1, Card c2) {
        if (c1.getRank() != c2.getRank()) {
            return c1.getRank().ordinal() - c2.getRank().ordinal();
        }

        return c1.getSuit().telesinaSuitValue - c2.getSuit().telesinaSuitValue;
    }
}