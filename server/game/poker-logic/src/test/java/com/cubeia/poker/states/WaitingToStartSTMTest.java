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

package com.cubeia.poker.states;

import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.SitOutStatus;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.util.SitoutCalculator;
import com.cubeia.poker.variant.GameType;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class WaitingToStartSTMTest {

    @Mock
    private PokerContext context;
    
    @Mock
    private ServerAdapterHolder serverAdapterHolder;
    
    @Mock
    private StateChanger stateChanger;

    @Mock
    private ServerAdapter serverAdapter;

    @Mock
    private GameType gameType;

    @Mock
    private PokerSettings settings;

    private WaitingToStartSTM stateUnderTest;
    private PokerPlayer player1;
    private PokerPlayer player2;

    @Before
    public void setup() {
        initMocks(this);
        stateUnderTest = new WaitingToStartSTM(gameType, context, serverAdapterHolder, stateChanger);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(context.isTournamentTable()).thenReturn(false);
        when(context.getSettings()).thenReturn(settings);
        player1 = mock(PokerPlayer.class);
        player2 = mock(PokerPlayer.class);
    }
    
    @Test
    public void testTimeout() {
        List<PokerPlayer> players = asList(player1, player2);
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(players);
        when(context.getSeatedPlayers()).thenReturn(players);

        stateUnderTest.timeout();

        verify(serverAdapter).performPendingBuyIns(players);
        verify(context).setHandFinished(false);
        verify(context).commitPendingBalances(any(BigDecimal.class));
        verify(gameType).startHand();
        verify(serverAdapter).cleanupPlayers(Matchers.<SitoutCalculator>any());
    }

    @Test
    public void testTimeoutTooFewPlayers() {
        PokerPlayer player = mock(PokerPlayer.class);
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(asList(player));
        ArrayList<PokerPlayer> seatedPlayers = new ArrayList<PokerPlayer>();
        when(context.getSeatedPlayers()).thenReturn(seatedPlayers);

        stateUnderTest.timeout();

        verify(serverAdapter).performPendingBuyIns(seatedPlayers);
        verify(context).commitPendingBalances(any(BigDecimal.class));
        verify(context).setHandFinished(true);
        verify(stateChanger).changeState(isA(NotStartedSTM.class));
        verify(serverAdapter).cleanupPlayers(Matchers.<SitoutCalculator>any());

        verify(gameType, never()).startHand();
    }

    @Test
    public void testNotifyBalanceAtStartOfHand() {
        List<PokerPlayer> players = asList(player1, player2);
        when(context.getPlayersReadyToStartHand(Matchers.<Predicate<PokerPlayer>>any())).thenReturn(players);
        when(context.getSeatedPlayers()).thenReturn(players);

        stateUnderTest.startHand();

        verify(serverAdapter).notifyPlayerBalance(player1);
        verify(serverAdapter).notifyPlayerBalance(player2);
    }

    @Test
    public void testSetPlayersWithoutMoneyAsSittingOut() {
        PokerPlayer p1 = mock(PokerPlayer.class);
        PokerPlayer p2 = mock(PokerPlayer.class);
        PokerPlayer p3 = mock(PokerPlayer.class);

        Map<Integer, PokerPlayer> map = createPlayerMap(p1, p2, p3);
        when(gameType.canPlayerAffordEntryBet(p1, settings, true)).thenReturn(true);
        when(gameType.canPlayerAffordEntryBet(p2, settings, true)).thenReturn(false);
        when(gameType.canPlayerAffordEntryBet(p3, settings, true)).thenReturn(false);
        when(context.getPlayerMap()).thenReturn(map);

        stateUnderTest.setPlayersWithoutMoneyAsSittingOut();

        verify(p1, never()).setSitOutStatus(Mockito.any(SitOutStatus.class));
        verify(p2).setSitOutStatus(SitOutStatus.SITTING_OUT);
        verify(p3).setSitOutStatus(SitOutStatus.SITTING_OUT);
    }

    private Map<Integer, PokerPlayer> createPlayerMap(PokerPlayer ... players) {
        Map<Integer, PokerPlayer> map = Maps.newHashMap();
        for (int i = 0; i < players.length; i++) {
            PokerPlayer player = players[i];
            int playerId = i + 1;
            map.put(playerId, player);
            when(player.getId()).thenReturn(playerId);
        }
        return map;
    }

}
