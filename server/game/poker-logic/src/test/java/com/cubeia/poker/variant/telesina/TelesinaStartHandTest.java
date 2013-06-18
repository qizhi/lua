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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.hand.Rank;
import com.cubeia.poker.model.BlindsInfo;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.Round;
import com.cubeia.poker.rounds.ante.AnteRound;

public class TelesinaStartHandTest {

    @Mock
    private PokerContext context;

    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    @Mock
    private BlindsInfo blindsInfo;

    @Mock
    private TelesinaDeckFactory deckFactory;

    @Mock
    private ServerAdapter serverAdapter;

    @Mock
    private Random rng;

    @Mock
    private TelesinaDeck deck;

    @Mock
    private TelesinaRoundFactory roundFactory;

    @Mock
    private AnteRound anteRound;

    @Mock
    private TelesinaDealerButtonCalculator dealerButtonCalculator;

    private Telesina telesina;

    @Before
    public void init() {
        initMocks(this);

        when(serverAdapter.getSystemRNG()).thenReturn(rng);
        when(context.getTableSize()).thenReturn(4);
        when(context.getAnteAmount()).thenReturn(new BigDecimal(1000));
        when(deck.getTotalNumberOfCardsInDeck()).thenReturn(40);
        when(deck.getDeckLowestRank()).thenReturn(Rank.FIVE);
        when(deckFactory.createNewDeck(rng, 4)).thenReturn(deck);
        when(roundFactory.createAnteRound(context, serverAdapterHolder)).thenReturn(anteRound);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(context.getBlindsInfo()).thenReturn(blindsInfo);

        telesina = new Telesina(deckFactory, roundFactory, dealerButtonCalculator);
        telesina.setPokerContextAndServerAdapter(context, serverAdapterHolder);
    }

    @Test
    public void testStartHand() {
        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);
        SortedMap<Integer, PokerPlayer> seatingMap = new TreeMap<Integer, PokerPlayer>();
        seatingMap.put(0, player1);
        seatingMap.put(1, player2);
        when(context.getCurrentHandSeatingMap()).thenReturn(seatingMap);
        when(context.getPlayersInHand()).thenReturn(seatingMap.values());

        telesina.startHand();

        assertThat(telesina.getCurrentRound(), is((Round) anteRound));
        assertThat(telesina.getBettingRoundId(), is(0));
        verify(deckFactory).createNewDeck(rng, 4);
//        verify(serverAdapter).notifyDeckInfo(40, Rank.FIVE);
    }

    @Test
    public void testThatNewDeckIsCreatedOnStartHand() {
        PokerPlayer player1 = mock(PokerPlayer.class);
        PokerPlayer player2 = mock(PokerPlayer.class);
        SortedMap<Integer, PokerPlayer> seatingMap = new TreeMap<Integer, PokerPlayer>();
        seatingMap.put(0, player1);
        seatingMap.put(1, player2);
        when(context.getCurrentHandSeatingMap()).thenReturn(seatingMap);

        context.setBlindsInfo(blindsInfo);

        telesina.startHand();
        verify(deckFactory, times(1)).createNewDeck(rng, 4);

        telesina.startHand();
        verify(deckFactory, times(2)).createNewDeck(rng, 4);

        telesina.startHand();
        verify(deckFactory, times(3)).createNewDeck(rng, 4);
    }
}
