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
package com.cubeia.games.poker;

import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.player.PokerPlayer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AllowLeaveTest {

    private static final int playerId = 2343;
    @Mock
    private Table table;
    @Mock
    private PokerPlayer pokerPlayer;
    @Mock
    private PokerState state;
    @Mock
    private StateInjector stateInjector;
    private PokerTableInterceptor tableInterceptor;


    @Before
    public void setUp() throws Exception {
        initMocks(this);

        tableInterceptor = new PokerTableInterceptor();
        tableInterceptor.state = state;
        tableInterceptor.stateInjector = stateInjector;

        when(state.getPokerPlayer(playerId)).thenReturn(pokerPlayer);
    }

    @Test
    public void okIfPlayerHasNoRunningWalletRequestAndNotPlaying() {
        when(state.isPlaying()).thenReturn(false);
        when(pokerPlayer.isBuyInRequestActive()).thenReturn(false);

        InterceptionResponse response = tableInterceptor.allowLeave(table, playerId);
        assertThat(response.isAllowed(), is(true));
    }

    @Test
    public void okIfWaitingToStart() {
        when(state.isPlaying()).thenReturn(false);
        when(pokerPlayer.isBuyInRequestActive()).thenReturn(false);

        InterceptionResponse response = tableInterceptor.allowLeave(table, playerId);
        assertThat(response.isAllowed(), is(true));
    }

    @Test
    public void doNotAllowIfPlaying() {
        when(pokerPlayer.isBuyInRequestActive()).thenReturn(false);

        when(state.isPlaying()).thenReturn(true);
        InterceptionResponse response = tableInterceptor.allowLeave(table, playerId);
        assertThat(response.isAllowed(), is(false));
    }

    @Test
    public void doNotAllowIfWalletRequestActive() {
        when(state.isPlaying()).thenReturn(false);
        when(pokerPlayer.isBuyInRequestActive()).thenReturn(true);

        InterceptionResponse response = tableInterceptor.allowLeave(table, playerId);
        assertThat(response.isAllowed(), is(false));
    }

}
