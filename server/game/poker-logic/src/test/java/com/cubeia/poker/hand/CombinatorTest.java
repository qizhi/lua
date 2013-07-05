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

import static org.junit.Assert.*;


public class CombinatorTest {

    @Test
    public void testCombinator_1() throws Exception {
        List<Card> set = new Hand("2s 3s 4s").getCards();
        Combinator<Card> combinator = new Combinator<Card>(set, 2);
        List<List<Card>> combinations = combinator.getAsList();

        assertEquals(3, combinations.size());

        assertTrue(findInCombination(combinations, new Hand("2s 3s")));
        assertTrue(findInCombination(combinations, new Hand("2s 4s")));
        assertTrue(findInCombination(combinations, new Hand("3s 4s")));
        assertFalse(findInCombination(combinations, new Hand("2s 2s")));
        assertFalse(findInCombination(combinations, new Hand("4s 3s")));
    }

    @Test
    public void testCombinator_2() throws Exception {
        List<Card> set = new Hand("2s 3s 4s 5s 6s 7s 8s").getCards();
        Combinator<Card> combinator = new Combinator<Card>(set, 5);
        List<List<Card>> combinations = combinator.getAsList();

        assertEquals(21, combinations.size());
        assertTrue(findInCombination(combinations, new Hand("2s 3s 6s 7s 8s")));
        assertTrue(findInCombination(combinations, new Hand("2s 3s 4s 7s 8s")));
    }

    private boolean findInCombination(List<List<Card>> combinations, Hand hand) {
        boolean equals = false;
        for (List<Card> cards : combinations) {
            equals |= Arrays.equals(cards.toArray(), hand.getCards().toArray());

        }
        return equals;
    }
}
