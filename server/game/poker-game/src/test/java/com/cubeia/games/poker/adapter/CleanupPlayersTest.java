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

package com.cubeia.games.poker.adapter;

import static com.cubeia.firebase.api.game.player.PlayerStatus.CONNECTED;
import static com.cubeia.firebase.api.game.player.PlayerStatus.DISCONNECTED;
import static com.cubeia.firebase.api.game.player.PlayerStatus.LEAVING;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collection;

import mock.UnmongofiableSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableMetaData;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableType;
import com.cubeia.firebase.api.util.UnmodifiableSet;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.util.SitoutCalculator;

public class CleanupPlayersTest {

    @Mock
    private Table table;
    @Mock
    private TableMetaData tableMetaData;
    @Mock
    private TablePlayerSet tablePlayerSet;
    @Mock
    private PokerState state;
    @Mock
    private SitoutCalculator sitoutCalculator;
    @Mock
    private GenericPlayer genericPlayer1;
    @Mock
    private GenericPlayer genericPlayer2;
    @Mock
    private GenericPlayer genericPlayer3;
    @Mock
    private PokerPlayer pokerPlayer1;
    @Mock
    private PokerPlayer pokerPlayer2;
    @Mock
    private PokerPlayer pokerPlayer3;
    @Mock
    private FirebaseState firebaseState;
    @Mock
    private LobbyUpdater lobbyUpdater;
    @Mock
    private PlayerUnseater playerUnseater;

    private FirebaseServerAdapter firebaseServerAdapter = new FirebaseServerAdapter();
    private int player1Id = 1001;
    private int player2Id = 1002;
    private int player3Id = 1003;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        firebaseServerAdapter.table = table;
        firebaseServerAdapter.state = state;
        firebaseServerAdapter.playerUnseater = playerUnseater;
        firebaseServerAdapter.lobbyUpdater = lobbyUpdater;

        when(genericPlayer1.getPlayerId()).thenReturn(player1Id);
        when(genericPlayer2.getPlayerId()).thenReturn(player2Id);
        when(genericPlayer3.getPlayerId()).thenReturn(player3Id);

        when(state.getPokerPlayer(player1Id)).thenReturn(pokerPlayer1);
        when(state.getPokerPlayer(player2Id)).thenReturn(pokerPlayer2);
        when(state.getPokerPlayer(player3Id)).thenReturn(pokerPlayer3);

        when(state.getAdapterState()).thenReturn(firebaseState);
        PokerSettings pokerSettings = mock(PokerSettings.class);
        when(state.getSettings()).thenReturn(pokerSettings);
        when(table.getMetaData()).thenReturn(tableMetaData);

        when(tableMetaData.getType()).thenReturn(TableType.NORMAL); 
        when(table.getPlayerSet()).thenReturn(tablePlayerSet);
        UnmodifiableSet<GenericPlayer> players = new UnmongofiableSet<GenericPlayer>(
                asList(genericPlayer1, genericPlayer2, genericPlayer3));
        when(tablePlayerSet.getPlayers()).thenReturn(players);
    }

    @Test
    public void removeDisconnectedAndLeavingPlayers() {
        when(genericPlayer1.getStatus()).thenReturn(CONNECTED);
        when(genericPlayer2.getStatus()).thenReturn(DISCONNECTED);
        when(genericPlayer3.getStatus()).thenReturn(LEAVING);

        firebaseServerAdapter.cleanupPlayers(sitoutCalculator);

        verify(playerUnseater, never()).unseatPlayer(table, player1Id, false);
        verify(playerUnseater).unseatPlayer(table, player2Id, false);
        verify(playerUnseater).unseatPlayer(table, player3Id, false);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void removeTimedOutPlayers() {
        when(genericPlayer1.getStatus()).thenReturn(CONNECTED);
        when(genericPlayer2.getStatus()).thenReturn(CONNECTED);
        when(genericPlayer3.getStatus()).thenReturn(CONNECTED);
        PokerPlayer timedOutPokerPlayer = mock(PokerPlayer.class);
        when(timedOutPokerPlayer.getId()).thenReturn(player2Id);
        Collection<PokerPlayer> pokerPlayers = Arrays.asList(timedOutPokerPlayer);
        when(sitoutCalculator.checkTimeoutPlayers((Collection<PokerPlayer>) Mockito.any(), Mockito.anyLong())).thenReturn(pokerPlayers);

        firebaseServerAdapter.cleanupPlayers(sitoutCalculator);

        verify(playerUnseater, never()).unseatPlayer(Mockito.eq(table), Mockito.eq(player1Id), Mockito.anyBoolean());
        verify(playerUnseater).unseatPlayer(table, player2Id, true);
        verify(playerUnseater, never()).unseatPlayer(table, player3Id, true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void dontRemoveIfAsyncRequestIsActive() {
        when(genericPlayer1.getStatus()).thenReturn(CONNECTED);
        when(genericPlayer2.getStatus()).thenReturn(DISCONNECTED);
        when(genericPlayer3.getStatus()).thenReturn(LEAVING);
        when(pokerPlayer2.isBuyInRequestActive()).thenReturn(true);

        PokerPlayer timedOutPokerPlayer = mock(PokerPlayer.class);
        when(timedOutPokerPlayer.getId()).thenReturn(player2Id);
        Collection<PokerPlayer> pokerPlayers = Arrays.asList(timedOutPokerPlayer);
        when(sitoutCalculator.checkTimeoutPlayers((Collection<PokerPlayer>) Mockito.any(), Mockito.anyLong())).thenReturn(pokerPlayers);

        firebaseServerAdapter.cleanupPlayers(sitoutCalculator);

        verify(playerUnseater, never()).unseatPlayer(Mockito.eq(table), Mockito.eq(player1Id), Mockito.anyBoolean());
        verify(playerUnseater, never()).unseatPlayer(Mockito.eq(table), Mockito.eq(player2Id), Mockito.anyBoolean());
        verify(playerUnseater).unseatPlayer(table, player3Id, false);
    }

    @Test
    public void testUnseatPlayer() {
        firebaseServerAdapter.unseatPlayer(player1Id, false);
        verify(playerUnseater).unseatPlayer(table, player1Id, false);
    }

    @Test
    public void testUnseatPlayerRefuseIfActiveBackendRequest() {
        when(pokerPlayer1.isBuyInRequestActive()).thenReturn(true);
        firebaseServerAdapter.unseatPlayer(player1Id, false);
        verify(playerUnseater, never()).unseatPlayer(table, player1Id, false);
    }

    @Test
    public void testUnseatPlayerRefuseIfParticipatingInHand() {
        when(state.getPlayerInCurrentHand(player1Id)).thenReturn(pokerPlayer1);
        when(state.isPlaying()).thenReturn(true);
        firebaseServerAdapter.unseatPlayer(player1Id, false);
        verify(playerUnseater, never()).unseatPlayer(table, player1Id, false);
    }

}
