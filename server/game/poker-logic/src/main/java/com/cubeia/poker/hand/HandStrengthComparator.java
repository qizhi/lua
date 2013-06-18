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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Compare to another hand strength with hand ranking in mind,
 * i.e. the strongest hand should come first (is greater than a lesser hand).</p>
 * <p/>
 * This ordering is contrary to the contract specified by Comparator so take care!
 */
public class HandStrengthComparator implements Comparator<HandStrength>, Serializable {

    @Override
    public int compare(HandStrength hs1, HandStrength hs2) {
        if (!hs2.getHandType().equals(hs1.getHandType())) {
            // Different hand types so only compare type
            return hs2.getHandType().ordinal() - hs1.getHandType().ordinal();

        } else {
            // Check highest rank etc.
            if (hs2.getHighestRank() != hs1.getHighestRank()) {
                return hs2.getHighestRank().ordinal() - hs1.getHighestRank().ordinal();

            } else if (hs2.getSecondRank() != hs1.getSecondRank()) {
                return hs2.getSecondRank().ordinal() - hs1.getSecondRank().ordinal();
            } else if (hs1.getHandType() == HandType.FLUSH) {
                for (int i = 0; i < hs1.getGroupSize(); i++) {
                    int compare = compareGroups(hs1.getGroup(i), hs2.getGroup(i));
                    if (compare != 0) {
                        return compare;
                    }
                }
            } else {
                // Check kickers in descending order
                for (int i = 0; i < hs1.getKickerCards().size(); i++) {
                    if (hs2.getKickerCards().get(i).getRank() != hs1.getKickerCards().get(i).getRank()) {
                        return hs2.getKickerCards().get(i).getRank().ordinal() - hs1.getKickerCards().get(i).getRank().ordinal();
                    }
                }
            }
        }

        // Same strength
        return 0;
    }

    private int compareGroups(List<Card> cardList1, List<Card> cardList2) {
        if (cardList1.size() != cardList2.size()) {
            throw new IllegalArgumentException("Only kicker lists of equal length may be compared");
        }

        List<Card> copy1 = new LinkedList<Card>(cardList1);
        List<Card> copy2 = new LinkedList<Card>(cardList2);

        Collections.sort(copy1, CardComparator.DESC);
        Collections.sort(copy2, CardComparator.DESC);

        Iterator<Card> c1iter = copy1.iterator();
        Iterator<Card> c2iter = copy2.iterator();

        while (c1iter.hasNext() && c2iter.hasNext()) {
            int cmp = CardComparator.DESC.compare(c1iter.next(), c2iter.next());
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

}
