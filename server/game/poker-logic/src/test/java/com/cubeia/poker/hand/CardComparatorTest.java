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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CardComparatorTest {

    @Test
    public void testCompare() {
        Card c1 = new Card(Rank.ACE, Suit.SPADES);
        Card c2 = new Card(Rank.TWO, Suit.CLUBS);
        Card c3 = new Card(Rank.TEN, Suit.HEARTS);
        Card c4 = new Card(Rank.TWO, Suit.CLUBS);

        CardComparator cc = new CardComparator();

        assertThat(cc.compare(c1, c2) > 0, is(true));
        assertThat(cc.compare(c2, c1) < 0, is(true));
        assertThat(cc.compare(c1, c3) > 0, is(true));
        assertThat(cc.compare(c2, c4) == 0, is(true));
    }

    @Test
    public void testCompareIdsAreIgnored() {
        Card c1 = new Card(10, Rank.ACE, Suit.SPADES);
        Card c2 = new Card(100, Rank.TWO, Suit.CLUBS);

        CardComparator cc = new CardComparator();

        assertThat(cc.compare(c1, c2) > 0, is(true));
    }
}
