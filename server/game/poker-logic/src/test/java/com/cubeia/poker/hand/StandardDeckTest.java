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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.cubeia.poker.hand.Rank.KING;
import static com.cubeia.poker.hand.Rank.QUEEN;
import static com.cubeia.poker.hand.Suit.CLUBS;
import static com.cubeia.poker.hand.Suit.DIAMONDS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class StandardDeckTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testConstruction() {
        Shuffler<Card> shuffler = mock(Shuffler.class);
        CardIdGenerator idGenerator = mock(CardIdGenerator.class);
        List<Card> shuffledList = new ArrayList<Card>();
        when(shuffler.shuffle(Mockito.anyList())).thenReturn(shuffledList);
        new StandardDeck(shuffler, idGenerator);

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(shuffler).shuffle(listCaptor.capture());
        assertThat(listCaptor.getValue().size(), is(52));
        verify(idGenerator).copyAndAssignIds(shuffledList);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateDeck() {
        Shuffler<Card> shuffler = mock(Shuffler.class);
        CardIdGenerator idGenerator = mock(CardIdGenerator.class);
        StandardDeck deck = new StandardDeck(shuffler, idGenerator);

        List<Card> cards = deck.createDeck();
        assertThat(cards, notNullValue());
        assertThat(cards.size(), is(52));
        assertThat(new HashSet<Card>(cards).size(), is(52));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void dealCard() {
        Shuffler<Card> shuffler = mock(Shuffler.class);
        CardIdGenerator idGenerator = mock(CardIdGenerator.class);
        Card card1 = new Card(KING, CLUBS);
        Card card2 = new Card(QUEEN, DIAMONDS);
        when(idGenerator.copyAndAssignIds(Mockito.anyList())).thenReturn(asList(card1, card2));

        StandardDeck deck = new StandardDeck(shuffler, idGenerator);
        verify(shuffler).shuffle(Mockito.anyList());
        assertThat(deck.isEmpty(), is(false));
        assertThat(deck.deal(), is(card1));
        assertThat(deck.isEmpty(), is(false));
        assertThat(deck.deal(), is(card2));
        assertThat(deck.isEmpty(), is(true));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalStateException.class)
    public void dealCardExceptionIfEmpty() {
        Shuffler<Card> shuffler = mock(Shuffler.class);
        CardIdGenerator idGenerator = mock(CardIdGenerator.class);
        when(idGenerator.copyAndAssignIds(Mockito.anyList())).thenReturn(Collections.<Card>emptyList());

        StandardDeck deck = new StandardDeck(shuffler, idGenerator);
        assertThat(deck.isEmpty(), is(true));
        deck.deal();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getAllCards() {
        Shuffler<Card> shuffler = mock(Shuffler.class);
        CardIdGenerator idGenerator = mock(CardIdGenerator.class);
        Card card1 = new Card(KING, CLUBS);
        Card card2 = new Card(QUEEN, DIAMONDS);
        when(idGenerator.copyAndAssignIds(Mockito.anyList())).thenReturn(asList(card1, card2));
        StandardDeck deck = new StandardDeck(shuffler, idGenerator);

        assertThat(deck.getAllCards().size(), is(2));
        deck.deal();
        assertThat(deck.getAllCards().size(), is(2));
    }

}
