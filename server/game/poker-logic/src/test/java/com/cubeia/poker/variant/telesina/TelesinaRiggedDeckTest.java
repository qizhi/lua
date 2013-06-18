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
import com.cubeia.poker.hand.Rank;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TelesinaRiggedDeckTest {

    @Test
    public void testTelesinaRiggedDeckConstruction() {
        TelesinaDeckUtil telesinaDeckUtil = mock(TelesinaDeckUtil.class);
        String riggedDeckString = "7C7D7H";
        int participants = 4;
        Card card1 = new Card("7C");
        Card card2 = new Card("7D");
        Card card3 = new Card("7H");
        when(telesinaDeckUtil.createRiggedDeck(participants, riggedDeckString)).thenReturn(Arrays.asList(card1, card2, card3));
        when(telesinaDeckUtil.calculateLowestRank(participants)).thenReturn(Rank.SEVEN);
        List<Card> vanillaCards = Arrays.asList(new Card("2H"), new Card("3H"), new Card("4H"));
        when(telesinaDeckUtil.createDeckCards(participants)).thenReturn(vanillaCards);

        TelesinaRiggedDeck riggedDeck = new TelesinaRiggedDeck(telesinaDeckUtil, participants, riggedDeckString);

        assertThat(riggedDeck.getAllCards().size(), is(3));
        assertThat(riggedDeck.isEmpty(), is(false));
        assertThat(riggedDeck.deal().makeCopyWithoutId(), is(card1));
        assertThat(riggedDeck.deal().makeCopyWithoutId(), is(card2));
        assertThat(riggedDeck.deal().makeCopyWithoutId(), is(card3));
        assertThat(riggedDeck.isEmpty(), is(true));
    }

}
