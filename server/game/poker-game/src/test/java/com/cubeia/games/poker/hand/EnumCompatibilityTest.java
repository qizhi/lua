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

package com.cubeia.games.poker.hand;

import com.cubeia.games.poker.io.protocol.Enums;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.hand.Suit;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EnumCompatibilityTest {
    @Test
    public void testSuitEnumCompatibility() {
        String msg = "Suit enum ordinal diff between protocol and domain";
        assertThat(msg, Enums.Suit.CLUBS.ordinal(), is(Suit.CLUBS.ordinal()));
        assertThat(msg, Enums.Suit.DIAMONDS.ordinal(), is(Suit.DIAMONDS.ordinal()));
        assertThat(msg, Enums.Suit.HEARTS.ordinal(), is(Suit.HEARTS.ordinal()));
        assertThat(msg, Enums.Suit.SPADES.ordinal(), is(Suit.SPADES.ordinal()));
    }

    @Test
    public void testRankEnumCompatibility() {
        String msg = "Suit enum ordinal diff between protocol and domain";
        assertThat(msg, Enums.Rank.ACE.ordinal(), is(Rank.ACE.ordinal()));
        assertThat(msg, Enums.Rank.KING.ordinal(), is(Rank.KING.ordinal()));
        assertThat(msg, Enums.Rank.QUEEN.ordinal(), is(Rank.QUEEN.ordinal()));
        assertThat(msg, Enums.Rank.JACK.ordinal(), is(Rank.JACK.ordinal()));
        assertThat(msg, Enums.Rank.TEN.ordinal(), is(Rank.TEN.ordinal()));
        assertThat(msg, Enums.Rank.NINE.ordinal(), is(Rank.NINE.ordinal()));
        assertThat(msg, Enums.Rank.EIGHT.ordinal(), is(Rank.EIGHT.ordinal()));
        assertThat(msg, Enums.Rank.SEVEN.ordinal(), is(Rank.SEVEN.ordinal()));
        assertThat(msg, Enums.Rank.SIX.ordinal(), is(Rank.SIX.ordinal()));
        assertThat(msg, Enums.Rank.FIVE.ordinal(), is(Rank.FIVE.ordinal()));
        assertThat(msg, Enums.Rank.FOUR.ordinal(), is(Rank.FOUR.ordinal()));
        assertThat(msg, Enums.Rank.THREE.ordinal(), is(Rank.THREE.ordinal()));
        assertThat(msg, Enums.Rank.TWO.ordinal(), is(Rank.TWO.ordinal()));
    }
}
