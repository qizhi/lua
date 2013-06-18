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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class HandTest {

    @Test
    public void testHandFromString() {
        Hand hand = new Hand("AS 5c kh");
        List<Card> cards = hand.getCards();
        Assert.assertEquals(3, cards.size());
        Assert.assertEquals("AS 5C KH ", hand.toString());
    }

    @Test
    public void testSortCards() throws Exception {
        Hand hand = new Hand("5C 3C 6C 2C 4C");
        hand = hand.sort();
        Assert.assertEquals("6C 5C 4C 3C 2C ", hand.toString());

        hand = new Hand("2C 2H 2D 2S 3C 3H");
        hand = hand.sort();
        Assert.assertEquals("3H 3C 2S 2H 2D 2C ", hand.toString());

        hand = new Hand("KH KC");
        hand = hand.sort();
        Assert.assertEquals("KH KC ", hand.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandFailFromString1() {
        new Hand("BS 5c kh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandFailFromString2() {
        new Hand("AX 5c kh");
    }

    @Test
    public void testContainsAllCardsRegardlessOfId() {
        Hand hand = new Hand(Arrays.asList(
                new Card(1, "5C"),
                new Card(1, "3C"),
                new Card(1, "6C"),
                new Card(1, "2C"),
                new Card(1, "4C")));

        assertThat(hand.containsAllCardsRegardlessOfId(new Hand("6C").getCards()), is(true));
        assertThat(hand.containsAllCardsRegardlessOfId(new Hand("2C 5C").getCards()), is(true));
        assertThat(hand.containsAllCardsRegardlessOfId(new Hand("6C 2C 5C").getCards()), is(true));
        assertThat(hand.containsAllCardsRegardlessOfId(asList(new Card(34, "6C"), new Card(10, "2C"))), is(true));

        assertThat(hand.containsAllCardsRegardlessOfId(new Hand("6D 2C 5C").getCards()), is(false));
    }


}
