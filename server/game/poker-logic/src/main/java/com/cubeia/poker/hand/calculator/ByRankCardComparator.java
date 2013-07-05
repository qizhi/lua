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

package com.cubeia.poker.hand.calculator;

import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Rank;

import java.util.Collections;
import java.util.Comparator;

public class ByRankCardComparator implements Comparator<Card> {

    public static final Comparator<Card> ACES_HIGH_ASC = new ByRankCardComparator(true);
    public static final Comparator<Card> ACES_LOW_ASC = new ByRankCardComparator(false);

    public static final Comparator<Card> ACES_HIGH_DESC = Collections.reverseOrder(ACES_HIGH_ASC);
    public static final Comparator<Card> ACES_LOW_DESC = Collections.reverseOrder(ACES_LOW_ASC);

    private boolean acesHigh;

    public ByRankCardComparator(boolean acesHigh) {
        this.acesHigh = acesHigh;
    }

    @Override
    public int compare(Card c1, Card c2) {
        if (acesHigh) {
            return compareAcesHigh(c1, c2);
        } else {
            return compareAcesLow(c1, c2);
        }
    }

    private int compareAcesLow(Card c1, Card c2) {
        int c1Rank = c1.getRank().ordinal();
        if (c1Rank == Rank.ACE.ordinal()) {
            c1Rank = -1;
        }

        int c2Rank = c2.getRank().ordinal();
        if (c2Rank == Rank.ACE.ordinal()) {
            c2Rank = -1;
        }

        return c1Rank - c2Rank;
    }

    public int compareAcesHigh(Card c1, Card c2) {
        return c1.getRank().ordinal() - c2.getRank().ordinal();
    }
}
