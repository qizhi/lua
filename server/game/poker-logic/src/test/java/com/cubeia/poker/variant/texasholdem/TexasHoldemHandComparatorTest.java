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

import com.cubeia.poker.hand.Hand;
import org.junit.Before;
import org.junit.Test;

import static com.cubeia.poker.util.TestHelpers.isLessThan;
import static org.junit.Assert.assertThat;

public class TexasHoldemHandComparatorTest {

    private TexasHoldemHandComparator comparator;

    @Before
    public void setup() {
        comparator = new TexasHoldemHandComparator();
    }

    @Test
    public void testTwoFlushesDifferentSecondHighCard() {
        Hand winner = new Hand("6C 8C 6D 9C AC 5C");
        Hand loser = new Hand("3C 8C 6D 9C AC 5C");

        // Should really be >= 1, but someone botched the compare semantics.
        assertThat(comparator.compare(winner, loser), isLessThan(0));
    }

}
