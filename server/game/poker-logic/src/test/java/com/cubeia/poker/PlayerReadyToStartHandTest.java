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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.cubeia.poker.adapter.ServerAdapterHolder;
import com.cubeia.poker.context.PokerContext;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.states.StateChanger;
import com.cubeia.poker.states.WaitingToStartSTM;
import com.cubeia.poker.variant.GameType;

public class PlayerReadyToStartHandTest {

    @Mock
    private GameType gameType;

    @Mock
    private PokerPlayer pokerPlayer;

    @Mock
    private PokerSettings settings;

    @Mock
    private PokerContext pokerContext;

    @Mock
    private ServerAdapterHolder adapterHolder;

    @Mock
    private StateChanger stateChanger;

    WaitingToStartSTM state;

    @Before
    public void setup() {
        initMocks(this);
        when(pokerContext.getSettings()).thenReturn(settings);
        state = new WaitingToStartSTM(gameType, pokerContext, adapterHolder, stateChanger);
    }

    @Test
    public void testPlayerReadyToStartHand() {
        when(pokerPlayer.isSittingOut()).thenReturn(false);
        when(pokerPlayer.isBuyInRequestActive()).thenReturn(false);
        when(gameType.canPlayerAffordEntryBet(pokerPlayer, settings, false)).thenReturn(true);

        assertThat(state.playerReadyToStartHand(pokerPlayer), is(true));
    }

    @Test
    public void testPlayerNotReadyToStartHandWhenSittingOut() {
        when(pokerPlayer.isSittingOut()).thenReturn(true);
        when(pokerPlayer.isBuyInRequestActive()).thenReturn(false);
        when(gameType.canPlayerAffordEntryBet(pokerPlayer, settings, false)).thenReturn(true);

        assertThat(state.playerReadyToStartHand(pokerPlayer), is(false));
    }

    @Test
    public void testPlayerNotReadyToStartHandWhileBackendRequestActive() {
        when(pokerPlayer.isSittingOut()).thenReturn(false);
        when(pokerPlayer.isBuyInRequestActive()).thenReturn(true);
        when(gameType.canPlayerAffordEntryBet(pokerPlayer, settings, false)).thenReturn(true);

        assertThat(state.playerReadyToStartHand(pokerPlayer), is(false));
    }

    @Test
    public void testPlayerNotReadyToStartHandIfNoCashForEntryBet() {
        when(pokerPlayer.isSittingOut()).thenReturn(false);
        when(pokerPlayer.isBuyInRequestActive()).thenReturn(false);
        when(gameType.canPlayerAffordEntryBet(pokerPlayer, settings, false)).thenReturn(false);

        assertThat(state.playerReadyToStartHand(pokerPlayer), is(false));
    }

}
