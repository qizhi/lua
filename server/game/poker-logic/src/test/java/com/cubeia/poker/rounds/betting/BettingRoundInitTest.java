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

package com.cubeia.poker.rounds.betting;

import com.cubeia.poker.action.ActionRequest;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.hand.Card;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import com.cubeia.poker.variant.texasholdem.TexasHoldemFutureActionsCalculator;
import com.google.common.base.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BettingRoundInitTest {

    @Mock
    private PokerPlayer player1;
    @Mock
    private PokerPlayer player2;
    @Mock
    private PokerPlayer player3;
    @Mock
    private PlayerToActCalculator playertoActCalculator;
    @Mock
    private ActionRequestFactory actionRequestFactory;
    @Mock
    private BetStrategy betStrategy;

    @Mock
    private PokerContext context;

    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    @Mock
    private ServerAdapter serverAdapter;

    private SortedMap<Integer, PokerPlayer> currentHandSeatingMap;

    private Integer player2Id = 1002;

    private BettingRound round;

    @Before
    public void setup() {
        initMocks(this);

        when(player2.getId()).thenReturn(player2Id);

        currentHandSeatingMap = new TreeMap<Integer, PokerPlayer>();
        currentHandSeatingMap.put(0, player1);
        currentHandSeatingMap.put(1, player2);
        currentHandSeatingMap.put(2, player3);
        when(player1.getBetStack()).thenReturn(BigDecimal.ZERO);
        when(player2.getBetStack()).thenReturn(BigDecimal.ZERO);
        when(player3.getBetStack()).thenReturn(BigDecimal.ZERO);

        when(context.getCurrentHandSeatingMap()).thenReturn(currentHandSeatingMap);
        when(context.getPlayersInHand()).thenReturn(currentHandSeatingMap.values());
        when(context.getTimingProfile()).thenReturn(new DefaultTimingProfile());
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        TexasHoldemFutureActionsCalculator futureActionsCalculator = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);
        round = new BettingRound(context, serverAdapterHolder, playertoActCalculator, actionRequestFactory, futureActionsCalculator, betStrategy);
    }

    @Test
    public void testNormalBettingRoundInit() {
        when(playertoActCalculator.getFirstPlayerToAct(eq(currentHandSeatingMap), Mockito.anyListOf(Card.class))).thenReturn(player2);
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(asList(player1, player2, player3));

        ActionRequest actionRequest = mock(ActionRequest.class);
        when(actionRequestFactory.createFoldCheckBetActionRequest(Mockito.any(BettingRound.class), eq(player2))).thenReturn(actionRequest);
        when(player2.getActionRequest()).thenReturn(actionRequest);

        round = new BettingRound(context, serverAdapterHolder, playertoActCalculator, actionRequestFactory,
                                 new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT), betStrategy);

        assertThat(round.playerToAct, is(player2Id));
        verify(player2).setActionRequest(actionRequest);
        verify(serverAdapter).requestAction(actionRequest);
    }

    @Test
    public void testRoundFinishedWhenAllInShowdown() {
        when(playertoActCalculator.getFirstPlayerToAct(eq(currentHandSeatingMap),
                Mockito.anyListOf(Card.class))).thenReturn(player2);
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(asList(player1, player2, player3));
        when(player1.isAllIn()).thenReturn(true);
        when(player3.isAllIn()).thenReturn(true);

        verify(serverAdapter).scheduleTimeout(anyLong());
        assertThat(round.isFinished(), is(true));
    }
}
