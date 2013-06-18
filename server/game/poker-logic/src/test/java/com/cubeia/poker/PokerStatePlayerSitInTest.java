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

package com.cubeia.poker;

import com.cubeia.poker.adapter.ServerAdapter;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.PokerPlayerStatus;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.variant.GameType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PokerStatePlayerSitInTest {

    @Mock
    PokerState state;
    @Mock
    ServerAdapter serverAdapter;
    @Mock
    GameType gameType;

    @Before
    public void setup() {
        initMocks(this);

        state = new PokerState();
        state.init(gameType, null);
        state.setServerAdapter(serverAdapter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlayerIsSittingIn() {
        int playerId = 1337;
        state.pokerContext.playerMap = mock(Map.class);
        PokerPlayer player = mock(PokerPlayer.class);
        when(state.pokerContext.playerMap.get(playerId)).thenReturn(player);
        when(gameType.canPlayerAffordEntryBet(Mockito.eq(player), (PokerSettings) Mockito.any(), Mockito.eq(true))).thenReturn(true);

        when(player.isSittingOut()).thenReturn(true);
        state.playerIsSittingIn(playerId);

        verify(player).sitIn();
        verify(player).setSittingOutNextHand(false);
        verify(player).setSitInAfterSuccessfulBuyIn(false);
        verify(serverAdapter).notifyPlayerStatusChanged(playerId, PokerPlayerStatus.SITIN, false,false,false);
        verify(serverAdapter, never()).notifyBuyInInfo(playerId, true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlayerIsSittingInNoCashSendsBuyInInfo() {
        int playerId = 1337;
        state.pokerContext.playerMap = mock(Map.class);
        PokerPlayer player = mock(PokerPlayer.class);
        when(state.pokerContext.playerMap.get(playerId)).thenReturn(player);
        when(gameType.canPlayerAffordEntryBet(Mockito.eq(player), (PokerSettings) Mockito.any(), Mockito.eq(true))).thenReturn(false);
        when(player.getRequestedBuyInAmount()).thenReturn(BigDecimal.ZERO);
        when(player.isSittingOut()).thenReturn(true);
        state.playerIsSittingIn(playerId);

        verify(serverAdapter).notifyBuyInInfo(playerId, true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlayerIsSittingInDontSendBuyInInfoIfPendingRequest() {
        int playerId = 1337;
        state.pokerContext.playerMap = mock(Map.class);
        PokerPlayer player = mock(PokerPlayer.class);
        when(player.isBuyInRequestActive()).thenReturn(true);
        when(state.pokerContext.playerMap.get(playerId)).thenReturn(player);
        when(gameType.canPlayerAffordEntryBet(Mockito.eq(player), (PokerSettings) Mockito.any(), Mockito.eq(true))).thenReturn(false);

        state.playerIsSittingIn(playerId);

        verify(serverAdapter, never()).notifyBuyInInfo(playerId, true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPlayerIsSittingInWithInsufficientCashToBuyIn() {
        int playerId = 1337;
        state.pokerContext.playerMap = mock(Map.class);
        PokerPlayer player = mock(PokerPlayer.class);
        when(state.pokerContext.playerMap.get(playerId)).thenReturn(player);
        when(gameType.canPlayerAffordEntryBet(Mockito.eq(player), (PokerSettings) Mockito.any(), Mockito.eq(true))).thenReturn(false);
        when(player.getRequestedBuyInAmount()).thenReturn(new BigDecimal(0));
        when(player.isSittingOut()).thenReturn(true);
        state.playerIsSittingIn(playerId);

        verify(player, never()).sitIn();
        verify(serverAdapter, never()).notifyPlayerStatusChanged(playerId, PokerPlayerStatus.SITIN, false,player.isAway(),
                player.isSittingOutNextHand());
        verify(serverAdapter).notifyBuyInInfo(playerId, true);
    }

}
