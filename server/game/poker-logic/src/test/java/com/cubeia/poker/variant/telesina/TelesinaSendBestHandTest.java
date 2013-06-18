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

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandStrength;
import com.cubeia.poker.hand.HandType;
import com.cubeia.poker.player.DefaultPokerPlayer;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.pot.PotHolder;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import com.cubeia.poker.variant.telesina.hand.TelesinaHandStrengthEvaluator;

public class TelesinaSendBestHandTest {

    @Mock
    private PotHolder potHolder;
    @Mock
    private ServerAdapter serverAdapter;
    @Mock
    private TelesinaDeckFactory deckFactory;
    @Mock
    private TelesinaDeck deck;
    @Mock
    private TelesinaRoundFactory roundFactory;
    @Mock
    private TelesinaDealerButtonCalculator dealerButtonCalculator;
    @Mock
    private PokerContext context;
    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    private PokerPlayer player1 = new DefaultPokerPlayer(1001);

    private SortedMap<Integer, PokerPlayer> seatingMap;
    private Telesina telesina;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        seatingMap = new TreeMap<Integer, PokerPlayer>();
        seatingMap.put(0, player1);
        when(context.getCurrentHandSeatingMap()).thenReturn(seatingMap);

        Map<Integer, PokerPlayer> playerMap = new HashMap<Integer, PokerPlayer>();
        playerMap.put(player1.getId(), player1);
        when(context.getCurrentHandPlayerMap()).thenReturn(playerMap);

        when(context.getTimingProfile()).thenReturn(new DefaultTimingProfile());
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(context.getPotHolder()).thenReturn(potHolder);
        when(deckFactory.createNewDeck(Mockito.any(Random.class), Mockito.anyInt())).thenReturn(deck);
        when(serverAdapter.getSystemRNG()).thenReturn(new Random());

        telesina = new Telesina(deckFactory, roundFactory, dealerButtonCalculator);
        telesina.setPokerContextAndServerAdapter(context, serverAdapterHolder);
    }

    @Test
    public void testCalculateAndSendBestHandToPlayer() {
        TelesinaHandStrengthEvaluator evaluator = Mockito.mock(TelesinaHandStrengthEvaluator.class);
        Hand hand = mock(Hand.class);
        PokerPlayer player = mock(PokerPlayer.class);
        when(player.getPocketCards()).thenReturn(hand);
        when(player.isExposingPocketCards()).thenReturn(false);
        Card pocketCard1 = new Card("AS");
        Card pocketCard2 = new Card("5C");
        when(hand.getCards()).thenReturn(asList(pocketCard1, pocketCard2));
        Card velaCard = new Card("2H");
        when(context.getCommunityCards()).thenReturn(asList(velaCard));
        HandStrength handStrength = mock(HandStrength.class);
        when(handStrength.getCards()).thenReturn(Arrays.asList(pocketCard1));
        when(handStrength.getHandType()).thenReturn(HandType.FOUR_OF_A_KIND);

        when(evaluator.getBestHandStrength(Mockito.any(Hand.class))).thenReturn(handStrength);

        telesina.calculateAndSendBestHandToPlayer(evaluator, player);
        verify(serverAdapter).notifyBestHand(player.getId(), HandType.FOUR_OF_A_KIND, asList(pocketCard1), false);
    }

    @Test
    public void testCalculateAndSendBestHandToPlayersWhenExposingHand() {
        TelesinaHandStrengthEvaluator evaluator = Mockito.mock(TelesinaHandStrengthEvaluator.class);
        Hand hand = mock(Hand.class);
        PokerPlayer player = mock(PokerPlayer.class);
        when(player.getPocketCards()).thenReturn(hand);
        when(player.isExposingPocketCards()).thenReturn(true);
        Card pocketCard1 = new Card("AS");
        Card pocketCard2 = new Card("5C");
        when(hand.getCards()).thenReturn(asList(pocketCard1, pocketCard2));
        Card velaCard = new Card("2H");
        when(context.getCommunityCards()).thenReturn(asList(velaCard));
        HandStrength handStrength = mock(HandStrength.class);
        when(handStrength.getCards()).thenReturn(Arrays.asList(pocketCard1));
        when(handStrength.getHandType()).thenReturn(HandType.FOUR_OF_A_KIND);

        when(evaluator.getBestHandStrength(Mockito.any(Hand.class))).thenReturn(handStrength);

        telesina.calculateAndSendBestHandToPlayer(evaluator, player);
        verify(serverAdapter).notifyBestHand(player.getId(), HandType.FOUR_OF_A_KIND, asList(pocketCard1), true);
    }

    @Test
    public void testCalculateAndSendBestHandShouldKeepQuietWhenFolded() {
        TelesinaHandStrengthEvaluator evaluator = Mockito.mock(TelesinaHandStrengthEvaluator.class);
        Hand hand = mock(Hand.class);
        PokerPlayer player = mock(PokerPlayer.class);
        when(player.getPocketCards()).thenReturn(hand);
        when(player.isExposingPocketCards()).thenReturn(true);
        when(player.hasFolded()).thenReturn(true);
        Card pocketCard1 = new Card("AS");
        Card pocketCard2 = new Card("5C");
        when(hand.getCards()).thenReturn(asList(pocketCard1, pocketCard2));
        Card velaCard = new Card("2H");
        when(context.getCommunityCards()).thenReturn(asList(velaCard));
        HandStrength handStrength = mock(HandStrength.class);
        when(handStrength.getCards()).thenReturn(Arrays.asList(pocketCard1));
        when(handStrength.getHandType()).thenReturn(HandType.FOUR_OF_A_KIND);

        when(evaluator.getBestHandStrength(Mockito.any(Hand.class))).thenReturn(handStrength);

        telesina.calculateAndSendBestHandToPlayer(evaluator, player);
        verify(serverAdapter).notifyBestHand(player.getId(), HandType.FOUR_OF_A_KIND, asList(pocketCard1), false);
    }

}
