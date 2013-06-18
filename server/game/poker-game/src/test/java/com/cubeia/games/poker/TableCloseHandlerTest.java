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

import com.cubeia.firebase.api.action.AbstractGameAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.GameNotifier;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableWatcherSet;
import com.cubeia.firebase.api.util.UnmodifiableSet;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.games.poker.adapter.FirebaseServerAdapter;
import com.cubeia.games.poker.cache.ActionCache;
import com.cubeia.games.poker.handler.BackendPlayerSessionHandler;
import com.cubeia.games.poker.io.protocol.Enums.ErrorCode;
import com.cubeia.games.poker.io.protocol.ErrorPacket;
import com.cubeia.games.poker.io.protocol.ProtocolObjectFactory;
import com.cubeia.games.poker.common.lobby.PokerLobbyAttributes;
import com.cubeia.games.poker.state.FirebaseState;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.player.PokerPlayer;
import mock.UnmongofiableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TableCloseHandlerTest {

    private StyxSerializer serializer = new StyxSerializer(new ProtocolObjectFactory());

    @Mock
    private PokerState state;
    @Mock
    private FirebaseState fbState;
    @Mock
    private ActionCache actionCache;
    @Mock
    private Table table;
    @Mock
    private LobbyTableAttributeAccessor attributeAccessor;
    @Mock
    private BackendPlayerSessionHandler backendPlayerSessionHandler;
    @Mock
    private FirebaseServerAdapter serverAdapter;
    private TableCloseHandlerImpl tableCrashHandler;
    private int tableId = 1343;
    private String handId = "4435";

    @Before
    public void setup() {
        initMocks(this);
        tableCrashHandler = new TableCloseHandlerImpl(state, actionCache, backendPlayerSessionHandler, serverAdapter);
        when(table.getAttributeAccessor()).thenReturn(attributeAccessor);
        when(table.getId()).thenReturn(tableId);
        when(state.getAdapterState()).thenReturn(fbState);
    }

    @Test
    public void testCloseAborted() throws IOException {
        setupCloseTableScenario();
        tableCrashHandler.closeTable(table, false);
        verify(state, times(0)).shutdown();
    }

    @Test
    public void testCloseForced() throws IOException {
        setupCloseTableScenario();
        when(serverAdapter.getIntegrationHandId()).thenReturn(handId);
        tableCrashHandler.closeTable(table, true);
        verify(state, times(1)).shutdown();
        ArgumentCaptor<GameDataAction> actionCaptor = ArgumentCaptor.forClass(GameDataAction.class);
        verify(table.getNotifier(), times(4)).notifyPlayer(Mockito.anyInt(), actionCaptor.capture());
        GameDataAction errorMessageAction = actionCaptor.getValue();
        ErrorPacket errorPacket = (ErrorPacket) serializer.unpack(errorMessageAction.getData());
        assertThat(errorPacket.code, is(ErrorCode.TABLE_CLOSING));
        assertThat(errorPacket.referenceId, is(handId));
    }

    @Test
    public void testCloseWhenNooneIsSeated() throws Exception {
        setupCloseTableScenario();

        // remove all seated players
        TablePlayerSet tablePlayerSet = mock(TablePlayerSet.class);
        when(table.getPlayerSet()).thenReturn(tablePlayerSet);
        UnmodifiableSet<GenericPlayer> playerSet = new UnmongofiableSet<GenericPlayer>(Arrays.<GenericPlayer>asList());
        when(tablePlayerSet.getPlayers()).thenReturn(playerSet);
        when(tablePlayerSet.getPlayerCount()).thenReturn(0);

        when(serverAdapter.getIntegrationHandId()).thenReturn(handId);
        tableCrashHandler.closeTable(table, false);
        verify(state, times(1)).shutdown();

        verify(table.getNotifier(), times(2)).notifyPlayer(Mockito.anyInt(), Mockito.any(GameDataAction.class));
    }

    protected void setupCloseTableScenario() {
        TablePlayerSet tablePlayerSet = mock(TablePlayerSet.class);
        when(table.getPlayerSet()).thenReturn(tablePlayerSet);

        TableWatcherSet tableWatcherSet = mock(TableWatcherSet.class);
        when(table.getWatcherSet()).thenReturn(tableWatcherSet);

        GameNotifier gameNotifier = mock(GameNotifier.class);
        when(table.getNotifier()).thenReturn(gameNotifier);

        final GenericPlayer gp1 = mock(GenericPlayer.class);
        final GenericPlayer gp2 = mock(GenericPlayer.class);
        int player1Id = 1003;
        int player2Id = 4001;
        when(gp1.getPlayerId()).thenReturn(player1Id);
        when(gp2.getPlayerId()).thenReturn(player2Id);

        UnmodifiableSet<GenericPlayer> playerSet = new UnmongofiableSet<GenericPlayer>(asList(gp1, gp2));
        UnmodifiableSet<Integer> watcherSet = new UnmongofiableSet<Integer>(Arrays.<Integer>asList(1337, 1338));

        when(tablePlayerSet.getPlayers()).thenReturn(playerSet);
        when(tablePlayerSet.getPlayerCount()).thenReturn(2);

        when(tableWatcherSet.getWatchers()).thenReturn(watcherSet);
        when(tableWatcherSet.getCountWatchers()).thenReturn(2);

        PokerPlayer pokerPlayer1 = mock(PokerPlayer.class);
        PokerPlayer pokerPlayer2 = mock(PokerPlayer.class);
        when(state.getPokerPlayer(player1Id)).thenReturn(pokerPlayer1);
        when(state.getPokerPlayer(player2Id)).thenReturn(pokerPlayer2);
        when(serverAdapter.getIntegrationHandId()).thenReturn(handId);
    }

    @Test
    public void testHandleCrashOnTable() throws IOException {
        AbstractGameAction action = new GameObjectAction(tableId);
        TablePlayerSet tablePlayerSet = mock(TablePlayerSet.class);
        when(table.getPlayerSet()).thenReturn(tablePlayerSet);
        GameNotifier gameNotifier = mock(GameNotifier.class);
        when(table.getNotifier()).thenReturn(gameNotifier);
        final GenericPlayer gp1 = mock(GenericPlayer.class);
        final GenericPlayer gp2 = mock(GenericPlayer.class);
        int player1Id = 1003;
        int player2Id = 4001;
        when(gp1.getPlayerId()).thenReturn(player1Id);
        when(gp2.getPlayerId()).thenReturn(player2Id);
        UnmodifiableSet<GenericPlayer> playerSet = new UnmongofiableSet<GenericPlayer>(asList(gp1, gp2));
        when(tablePlayerSet.getPlayers()).thenReturn(playerSet);
        PokerPlayer pokerPlayer1 = mock(PokerPlayer.class);
        PokerPlayer pokerPlayer2 = mock(PokerPlayer.class);
        when(state.getPokerPlayer(player1Id)).thenReturn(pokerPlayer1);
        when(state.getPokerPlayer(player2Id)).thenReturn(pokerPlayer2);
        String handId = "4435";
        when(serverAdapter.getIntegrationHandId()).thenReturn(handId);

        int watcher0id = 1337;
        int watcher1id = 1338;
        UnmodifiableSet<Integer> watcherSet = new UnmongofiableSet<Integer>(Arrays.<Integer>asList(watcher0id, watcher1id));
        TableWatcherSet tableWatcherSet = mock(TableWatcherSet.class);
        when(table.getWatcherSet()).thenReturn(tableWatcherSet);
        when(tableWatcherSet.getWatchers()).thenReturn(watcherSet);
        when(tableWatcherSet.getCountWatchers()).thenReturn(2);


        tableCrashHandler.handleUnexpectedExceptionOnTable(action, table, new RuntimeException("shit happens"));

        verify(attributeAccessor).setIntAttribute(PokerLobbyAttributes.VISIBLE_IN_LOBBY.name(), 0);
        verify(state).shutdown();

        verify(tablePlayerSet).removePlayer(player1Id);
        verify(tablePlayerSet).removePlayer(player2Id);
        verify(backendPlayerSessionHandler).endPlayerSessionInBackend(table, pokerPlayer1, 0, state);
        verify(backendPlayerSessionHandler).endPlayerSessionInBackend(table, pokerPlayer2, 0, state);
        verify(attributeAccessor).setAttribute(PokerLobbyAttributes.TABLE_READY_FOR_CLOSE.name(), new AttributeValue(1));
        verify(gameNotifier).notifyPlayer(Mockito.eq(player1Id), Mockito.any(GameDataAction.class));

        verify(gameNotifier).notifyPlayer(Mockito.eq(watcher0id), Mockito.any(GameDataAction.class));
        verify(gameNotifier).notifyPlayer(Mockito.eq(watcher1id), Mockito.any(GameDataAction.class));

        ArgumentCaptor<GameDataAction> actionCaptor = ArgumentCaptor.forClass(GameDataAction.class);
        verify(gameNotifier).notifyPlayer(Mockito.eq(player2Id), actionCaptor.capture());
        GameDataAction errorMessageAction = actionCaptor.getValue();
        ErrorPacket errorPacket = (ErrorPacket) serializer.unpack(errorMessageAction.getData());
        assertThat(errorPacket.code, is(ErrorCode.TABLE_CLOSING_FORCED));
        assertThat(errorPacket.referenceId, is("" + handId));
    }

    @Test
    public void testClosePlayerSessionsWontStopOnException() {
        PokerPlayer pokerPlayer1 = mock(PokerPlayer.class);
        PokerPlayer pokerPlayer2 = mock(PokerPlayer.class);
        doThrow(new RuntimeException("crash")).when(backendPlayerSessionHandler).endPlayerSessionInBackend(table, pokerPlayer1, -1, state);

        tableCrashHandler.closePlayerSessions(table, Arrays.asList(pokerPlayer1, pokerPlayer2));

        verify(backendPlayerSessionHandler).endPlayerSessionInBackend(table, pokerPlayer2, 0, state);
    }

}
