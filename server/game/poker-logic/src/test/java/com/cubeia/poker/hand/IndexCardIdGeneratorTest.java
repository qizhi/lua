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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IndexCardIdGeneratorTest {

    @Test
    public void test() {
        Card card1 = new Card(Rank.ACE, Suit.HEARTS);
        Card card2 = new Card(34, Rank.NINE, Suit.SPADES);

        List<Card> idCards = new IndexCardIdGenerator().copyAndAssignIds(Arrays.asList(card1, card2));
        assertThat(idCards.size(), is(2));
        assertThat(idCards.get(0), is(new Card(0, Rank.ACE, Suit.HEARTS)));
        assertThat(idCards.get(1), is(new Card(1, Rank.NINE, Suit.SPADES)));
    }

}
