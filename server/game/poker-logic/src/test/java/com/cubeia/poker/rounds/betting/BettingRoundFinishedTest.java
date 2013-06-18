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

import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.timing.impl.DefaultTimingProfile;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.texasholdem.TexasHoldemFutureActionsCalculator;
import com.google.common.base.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BettingRoundFinishedTest {

    @Mock
    private GameType telesina;
    @Mock
    private PokerContext context;
    @Mock
    private PlayerToActCalculator playerToActCalculator;
    @Mock
    private PokerPlayer player1;
    @Mock
    private PokerPlayer player2;
    @Mock
    private PokerPlayer player3;
    @Mock
    private ServerAdapterHolder serverAdapterHolder;
    @Mock
    private ServerAdapter serverAdapter;

    private BettingRound round;
    
    @Before
    public void setUp() throws Exception {
        initMocks(this);

        SortedMap<Integer, PokerPlayer> seatingMap = new TreeMap<Integer, PokerPlayer>();
        seatingMap.put(0, player1);
        seatingMap.put(1, player2);
        seatingMap.put(2, player3);
        when(player1.getBetStack()).thenReturn(BigDecimal.ZERO);
        when(player2.getBetStack()).thenReturn(BigDecimal.ZERO);
        when(player3.getBetStack()).thenReturn(BigDecimal.ZERO);

        when(context.getPlayersInHand()).thenReturn(seatingMap.values());
        when(context.getTimingProfile()).thenReturn(new DefaultTimingProfile());
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);

        NoLimitBetStrategy betStrategy = new NoLimitBetStrategy(BigDecimal.ZERO);
        ActionRequestFactory actionRequestFactory = new ActionRequestFactory(betStrategy);
        TexasHoldemFutureActionsCalculator futureActionsCalculator = new TexasHoldemFutureActionsCalculator(BetStrategyType.FIXED_LIMIT);
        round = new BettingRound(context, serverAdapterHolder, playerToActCalculator, actionRequestFactory, futureActionsCalculator, betStrategy);
    }

    @Test
    public void testNotFinishedWhenNoOneHasActed() {
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(asList(player1, player2, player3));
        when(context.countNonFoldedPlayers(anyCollectionOf(PokerPlayer.class))).thenReturn(3);
        when(player1.hasActed()).thenReturn(false);
        when(player2.hasActed()).thenReturn(false);
        when(player3.hasActed()).thenReturn(false);
        assertThat(round.calculateIfRoundFinished(), is(false));
    }

    @Test
    public void testFinishedWhenEverybodyHasActed() {
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(asList(player1, player2, player3));
        when(context.countNonFoldedPlayers(anyCollectionOf(PokerPlayer.class))).thenReturn(3);
        when(player1.hasActed()).thenReturn(true);
        when(player2.hasActed()).thenReturn(true);
        when(player3.hasActed()).thenReturn(true);
        assertThat(round.calculateIfRoundFinished(), is(true));
    }

    @Test
    public void testFinishedWhenAllButOneFolded() {
        when(context.countNonFoldedPlayers(anyCollectionOf(PokerPlayer.class))).thenReturn(1);

        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(asList(player1, player2, player3));
        assertThat(round.calculateIfRoundFinished(), is(true));
    }

    @Test
    public void testFinishedWhenAllButOneSittingOut() {
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(asList(player3));
        when(context.countNonFoldedPlayers(anyCollectionOf(PokerPlayer.class))).thenReturn(3);
        when(player1.isSittingOut()).thenReturn(true);
        when(player2.isSittingOut()).thenReturn(true);
        when(player3.isSittingOut()).thenReturn(false);
        assertThat(round.calculateIfRoundFinished(), is(false));
    }

    @Test
    public void testNotFinishedWhenAllSittingOut() {
        when(context.isEveryoneSittingOut()).thenReturn(true);
        when(context.countNonFoldedPlayers(anyCollectionOf(PokerPlayer.class))).thenReturn(3);
        when(player1.isSittingOut()).thenReturn(true);
        when(player2.isSittingOut()).thenReturn(true);
        when(player3.isSittingOut()).thenReturn(true);
        assertThat(round.calculateIfRoundFinished(), is(false));
    }

    @Test
    public void testFinished3PlayersAllInAndFoldedCombo() {
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(asList(player1, player2, player3));
        when(context.countNonFoldedPlayers(anyCollectionOf(PokerPlayer.class))).thenReturn(2);

        when(player1.isAllIn()).thenReturn(true);
        when(player2.hasFolded()).thenReturn(true);

        assertThat(round.calculateIfRoundFinished(), is(false));
    }

}
