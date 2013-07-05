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

import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableScheduler;
import com.cubeia.games.poker.logic.TimeoutCache;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.PokerState;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


public class DisconnectHandlerTest {

    @Mock
    TimeoutCache timeoutCache;

    @Mock
    Table table;

    @Mock
    TableScheduler scheduler;

    @Mock
    PokerState state;

    @Mock
    FirebaseServerAdapter adapter;

    DisconnectHandler handler = new DisconnectHandler();

    /**
     * Player id waiting to act: 1
     * Table id: 66
     */
    @Before
    public void setup() {
        initMocks(this);

        handler.state = state;
        handler.adapter = adapter;

        when(table.getScheduler()).thenReturn(scheduler);
        when(table.getId()).thenReturn(66);

        when(state.getPokerPlayer(1)).thenReturn(new MockPlayer(66));
    }

    @Ignore // Ignoring this test. Do we really need to tell the world that a player is having issues with his connection?
            // Specifically, if I know the player after me is disconnected, I might bet quickly just so that he will auto fold.
    @Test
    public void testWaitRejoin() {
        handler.checkDisconnectTime(table, 1, PlayerStatus.WAITING_REJOIN);
        //Mockito.verify(timeoutCache).removeTimeout(table.getTableId(), 1, table.getScheduler());
        Mockito.verify(adapter).notifyDisconnected(1);
    }

    @Test
    public void testWaitRejoinOtherPlayer() {
        handler.checkDisconnectTime(table, 2, PlayerStatus.WAITING_REJOIN);
        Mockito.verifyZeroInteractions(timeoutCache);
    }

    @Test
    public void testReconnect() {
        handler.checkDisconnectTime(table, 1, PlayerStatus.CONNECTED);

    }
}
