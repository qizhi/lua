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

import com.cubeia.poker.variant.telesina.TelesinaDeck;
import com.cubeia.poker.variant.telesina.TelesinaDeckUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import static com.cubeia.poker.hand.Rank.KING;
import static com.cubeia.poker.hand.Rank.QUEEN;
import static com.cubeia.poker.hand.Suit.CLUBS;
import static com.cubeia.poker.hand.Suit.DIAMONDS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TelesinaDeckTest {

    @Mock
    private Random rng;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testConstruction() {
        TelesinaDeck deck = new TelesinaDeck(new TelesinaDeckUtil(), rng, 6);
        assertThat(deck.isEmpty(), is(false));
        assertThat(deck.getAllCards().size(), is(40));
        assertThat(deck.getDeckLowestRank(), is(Rank.FIVE));
    }

    @Test
    public void calculateRanksToUse() {
        TelesinaDeckUtil telesinaDeckUtil = new TelesinaDeckUtil();
        TelesinaDeck deck = new TelesinaDeck(telesinaDeckUtil, rng, 2);
        assertThat(deck.getDeckLowestRank(), is(Rank.NINE));
        assertThat(deck.getAllCards().size(), is(6 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 3);
        assertThat(deck.getDeckLowestRank(), is(Rank.EIGHT));
        assertThat(deck.getAllCards().size(), is(7 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 4);
        assertThat(deck.getDeckLowestRank(), is(Rank.SEVEN));
        assertThat(deck.getAllCards().size(), is(8 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 5);
        assertThat(deck.getDeckLowestRank(), is(Rank.SIX));
        assertThat(deck.getAllCards().size(), is(9 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 6);
        assertThat(deck.getDeckLowestRank(), is(Rank.FIVE));
        assertThat(deck.getAllCards().size(), is(10 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 7);
        assertThat(deck.getDeckLowestRank(), is(Rank.FOUR));
        assertThat(deck.getAllCards().size(), is(11 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 9);
        assertThat(deck.getDeckLowestRank(), is(Rank.TWO));
        assertThat(deck.getAllCards().size(), is(Rank.values().length * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 10);
        assertThat(deck.getDeckLowestRank(), is(Rank.TWO));
        assertThat(deck.getAllCards().size(), is(Rank.values().length * 4));
    }


    @Test
    public void testGetLowestRank() {
        TelesinaDeckUtil telesinaDeckUtil = new TelesinaDeckUtil();
        TelesinaDeck deck = new TelesinaDeck(telesinaDeckUtil, rng, 2);
        assertThat(deck.getDeckLowestRank(), is(Rank.NINE));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 4);
        assertThat(deck.getDeckLowestRank(), is(Rank.SEVEN));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 8);
        assertThat(deck.getDeckLowestRank(), is(Rank.THREE));
    }

    @Test
    public void testGetTotalNumberOfCardsInDeck() {
        TelesinaDeckUtil telesinaDeckUtil = new TelesinaDeckUtil();
        TelesinaDeck deck = new TelesinaDeck(telesinaDeckUtil, rng, 2);
        assertThat(deck.getTotalNumberOfCardsInDeck(), is(6 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 4);
        assertThat(deck.getTotalNumberOfCardsInDeck(), is(8 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 9);
        assertThat(deck.getTotalNumberOfCardsInDeck(), is(13 * 4));

        deck = new TelesinaDeck(telesinaDeckUtil, rng, 10);
        assertThat(deck.getTotalNumberOfCardsInDeck(), is(13 * 4));
    }

    @Test
    public void dealCard() {
        Card card1 = new Card(KING, CLUBS);
        Card card2 = new Card(QUEEN, DIAMONDS);
        Card card3 = new Card(QUEEN, DIAMONDS);
        TelesinaDeckUtil telesinaDeckUtil = mock(TelesinaDeckUtil.class);
        when(telesinaDeckUtil.createDeckCards(4)).thenReturn(asList(card1, card2, card3));

        TelesinaDeck deck = new TelesinaDeck(telesinaDeckUtil, rng, 4);

        assertThat(deck.isEmpty(), is(false));

        Card card = deck.deal();
        assertThat(card.getId(), is(0));
        assertThat(card.makeCopyWithoutId(), is(card1));
        verify(rng).nextInt(3);
        assertThat(deck.isEmpty(), is(false));

        card = deck.deal();
        assertThat(card.getId(), is(1));
        assertThat(card.makeCopyWithoutId(), is(card2));
        verify(rng).nextInt(2);
        assertThat(deck.isEmpty(), is(false));

        card = deck.deal();
        assertThat(card.getId(), is(2));
        assertThat(card.makeCopyWithoutId(), is(card3));
        verify(rng).nextInt(1);
        assertThat(deck.isEmpty(), is(true));
    }

    @Test
    public void dealShufflingDifferendForDifferentSeeds() {
        SecureRandom rng1 = new SecureRandom(new byte[]{0});
        SecureRandom rng2 = new SecureRandom(new byte[]{1});

        TelesinaDeckUtil telesinaDeckUtil = new TelesinaDeckUtil();
        TelesinaDeck deck1 = new TelesinaDeck(telesinaDeckUtil, rng1, 10);
        TelesinaDeck deck2 = new TelesinaDeck(telesinaDeckUtil, rng2, 10);

        ArrayList<Card> cards1 = new ArrayList<Card>();
        ArrayList<Card> cards2 = new ArrayList<Card>();

        while (!deck1.isEmpty()) {
            cards1.add(deck1.deal());
        }
        while (!deck2.isEmpty()) {
            cards1.add(deck2.deal());
        }

        assertThat(cards1, is(not(cards2)));
    }

    @Test(expected = IllegalStateException.class)
    public void dealCardExceptionIfEmpty() {
        TelesinaDeckUtil telesinaDeckUtil = mock(TelesinaDeckUtil.class);
        when(telesinaDeckUtil.createDeckCards(4)).thenReturn(new ArrayList<Card>());
        TelesinaDeck deck = new TelesinaDeck(telesinaDeckUtil, rng, 4);
        assertThat(deck.isEmpty(), is(true));
        deck.deal();
    }

    @Test
    public void getAllCards() {
        Card card1 = new Card(KING, CLUBS);
        Card card2 = new Card(QUEEN, DIAMONDS);
        TelesinaDeckUtil telesinaDeckUtil = mock(TelesinaDeckUtil.class);
        when(telesinaDeckUtil.createDeckCards(4)).thenReturn(asList(card1, card2));

        TelesinaDeck deck = new TelesinaDeck(telesinaDeckUtil, rng, 4);

        assertThat(deck.getAllCards().size(), is(2));
        deck.deal();
        assertThat(deck.getAllCards().size(), is(2));
    }

}
