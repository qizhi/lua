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

import com.cubeia.games.poker.common.money.*;
import com.cubeia.games.poker.common.money.Currency;
import com.cubeia.poker.adapter.HandEndStatus;
import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.model.RatedPlayerHand;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.SitOutStatus;
import com.cubeia.poker.pot.PotTransition;
import com.cubeia.poker.result.HandResult;
import com.cubeia.poker.result.Result;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.timing.TimingProfile;
import com.cubeia.poker.variant.GameType;
import com.cubeia.poker.variant.telesina.Telesina;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PlayingSTMTest {
    
    @Mock
    private PokerSettings settings;

    @Mock
    private ServerAdapterHolder serverAdapterHolder;

    @Mock
    private ServerAdapter serverAdapter;

    @Mock
    private StateChanger stateChanger;
    
    @Mock
    private PokerContext context;

    @Mock
    private GameType gameType;

    int anteLevel;

    @Before
    public void setup() {
        initMocks(this);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        when(context.getSettings()).thenReturn(settings);
    }
    
    @Test
    public void testNotifyHandFinished() {
        TimingProfile timingProfile = mock(TimingProfile.class);
        when(settings.getTiming()).thenReturn(timingProfile);
        when(settings.getMaxBuyIn()).thenReturn(new BigDecimal(10000));

        PlayingSTM playing = new PlayingSTM();

        Telesina telesina = mock(Telesina.class);
        PokerContext context = new PokerContext(settings);
        
        playing.context = context;
        playing.gameType = telesina;
        playing.stateChanger = stateChanger;
        playing.serverAdapterHolder = serverAdapterHolder;

        Map<PokerPlayer, Result> results = new HashMap<PokerPlayer, Result>();

        PokerPlayer player1 = createMockPlayer(1337, anteLevel - 1);
        PokerPlayer player2 = createMockPlayer(666, anteLevel);
        PokerPlayer player3 = createMockPlayer(123, 0);
        when(player3.getBalanceNotInHand()).thenReturn(new BigDecimal(anteLevel));

        Result result1 = mock(Result.class);
        Result result2 = mock(Result.class);
        Result result3 = mock(Result.class);
        results.put(player1, result1);
        results.put(player2, result2);
        results.put(player3, result3);

        HandResult result = new HandResult(results, new ArrayList<RatedPlayerHand>(), Collections.<PotTransition>emptyList(), null, new ArrayList<Integer>(),new Currency("EUR",2));
        context.playerMap = new HashMap<Integer, PokerPlayer>();
        context.playerMap.put(player1.getId(), player1);
        context.playerMap.put(player2.getId(), player2);
        context.playerMap.put(player3.getId(), player3);

        context.seatingMap = new TreeMap<Integer, PokerPlayer>();
        context.seatingMap.put(0, player1);
        context.seatingMap.put(1, player2);
        context.seatingMap.put(2, player3);

        context.currentHandPlayerMap = new HashMap<Integer, PokerPlayer>();
        context.getCurrentHandPlayerMap().put(player1.getId(), player1);
        context.getCurrentHandPlayerMap().put(player2.getId(), player2);
        context.getCurrentHandPlayerMap().put(player3.getId(), player3);

        when(telesina.canPlayerAffordEntryBet(player1, settings, true)).thenReturn(false);
        when(telesina.canPlayerAffordEntryBet(player2, settings, true)).thenReturn(true);
        when(telesina.canPlayerAffordEntryBet(player3, settings, true)).thenReturn(true);

        BigDecimal winningsIncludingOwnBets = new BigDecimal(344);
        when(result1.getWinningsIncludingOwnBets()).thenReturn(winningsIncludingOwnBets);

        playing.handFinished(result, HandEndStatus.NORMAL);

        verify(player1).addChips(winningsIncludingOwnBets);

        InOrder inOrder = Mockito.inOrder(serverAdapter);
        inOrder.verify(serverAdapter).notifyHandEnd(result, HandEndStatus.NORMAL, context.isTournamentTable());
        inOrder.verify(serverAdapter).performPendingBuyIns(context.playerMap.values());

        assertThat(context.isFinished(), is(true));
        verify(stateChanger).changeState(isA(WaitingToStartSTM.class));
        verify(player3, Mockito.never()).setSitOutStatus(SitOutStatus.SITTING_OUT);

        verify(serverAdapter).notifyPlayerBalance(player1);
        verify(serverAdapter).notifyPlayerBalance(player2);
        verify(serverAdapter).notifyPlayerBalance(player3);

        verify(serverAdapter).notifyBuyInInfo(player1.getId(), true);
        verify(player2, Mockito.never()).setSitOutStatus(SitOutStatus.SITTING_OUT);
    }

    @Test
    public void testSendBuyInInfoToPlayersWithoutMoney() {
        PokerPlayer p1 = mock(PokerPlayer.class);
        PokerPlayer p2 = mock(PokerPlayer.class);
        PokerPlayer p3 = mock(PokerPlayer.class);
        PlayingSTM playing = new PlayingSTM();
        playing.context = context;

        playing.gameType = gameType;
        playing.stateChanger = stateChanger;
        playing.serverAdapterHolder = serverAdapterHolder;

        Map<Integer, PokerPlayer> map = createPlayerMap(p1, p2, p3);
        when(gameType.canPlayerAffordEntryBet(p1, settings, true)).thenReturn(true);
        when(gameType.canPlayerAffordEntryBet(p2, settings, true)).thenReturn(false);
        when(gameType.canPlayerAffordEntryBet(p3, settings, true)).thenReturn(false);
        when(context.getPlayerMap()).thenReturn(map);

        when(p3.isBuyInRequestActive()).thenReturn(true);

        playing.sendBuyinInfoToPlayersWithoutMoney();

        verify(serverAdapter, never()).notifyBuyInInfo(p1.getId(), true); // player affords buyin
        verify(serverAdapter).notifyBuyInInfo(p2.getId(), true);
        verify(serverAdapter, never()).notifyBuyInInfo(p3.getId(), true); // player has pending buyin, so we won't bother him
    }

    private PokerPlayer createMockPlayer(int playerId, int balance) {
        PokerPlayer player = mock(PokerPlayer.class);
        when(player.getBalance()).thenReturn(new BigDecimal(balance));
        when(player.getId()).thenReturn(playerId);
        return player;
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
