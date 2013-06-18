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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableMetaData;
import com.cubeia.firebase.api.service.clientregistry.PublicClientRegistryService;
import com.cubeia.games.poker.handler.BackendPlayerSessionHandler;
import com.cubeia.games.poker.model.PokerPlayerImpl;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.blinds.MissedBlindsStatus;
import com.cubeia.poker.player.PokerPlayer;

public class PokerTableListenerTest {

	PokerTableListener ptl;
	
	@Mock PublicClientRegistryService clientRegistry;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(clientRegistry.getOperatorId(Mockito.anyInt())).thenReturn(0);
		
		ptl = new PokerTableListener();
        ptl.clientRegistry = clientRegistry;
	}
	
    @Test
    public void addPlayer() throws IOException {
        int tableId = 234;
        int playerId = 1337;

        ptl.state = mock(PokerState.class);
        ptl.gameStateSender = mock(GameStateSender.class);
        ptl.backendPlayerSessionHandler = mock(BackendPlayerSessionHandler.class);

        FirebaseState fst = mock(FirebaseState.class);
        when(ptl.state.getAdapterState()).thenReturn(fst);

        Table table = mock(Table.class);
        when(table.getId()).thenReturn(tableId);
        TableMetaData tableMetaData = mock(TableMetaData.class);
        when(table.getMetaData()).thenReturn(tableMetaData);
        GameNotifier gameNotifier = mock(GameNotifier.class);
        when(table.getNotifier()).thenReturn(gameNotifier);
        GenericPlayer player = new GenericPlayer(playerId, "player");
        int balance = 40000;
        when(ptl.state.getBalance(playerId)).thenReturn(new BigDecimal(balance));

        PokerPlayer pokerPlayer = ptl.addPlayer(table, player, false);

        assertThat(pokerPlayer.getId(), is(playerId));
        assertThat(((PokerPlayerImpl) pokerPlayer).getPlayerSessionId(), nullValue());
        verify(ptl.gameStateSender).sendGameState(table, playerId);
        verify(ptl.state).addPlayer(pokerPlayer);
        verify(ptl.backendPlayerSessionHandler).startWalletSession(ptl.state, table, playerId);
        verify(ptl.state, never()).getBalance(playerId);

        assertThat(pokerPlayer.isSittingOut(), is(false));
        assertThat(pokerPlayer.getMissedBlindsStatus(), is(MissedBlindsStatus.NOT_ENTERED_YET));
    }

}
