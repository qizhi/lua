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
import com.cubeia.poker.settings.PokerSettings;
import com.cubeia.poker.variant.GameType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class NotStartedSTMTest {

    private NotStartedSTM state;

    @Mock
    private GameType gameType;
    @Mock
    private PokerContext context;
    @Mock
    private ServerAdapterHolder serverAdapterHolder;
    @Mock
    private StateChanger stateChanger;
    @Mock
    private ServerAdapter serverAdapter;
    @Mock
    private PokerSettings settings;

    @Before
    public void setup() {
        initMocks(this);
        when(serverAdapterHolder.get()).thenReturn(serverAdapter);
        state = new NotStartedSTM(gameType, context, serverAdapterHolder, stateChanger);
    }

    @Test
    public void shouldRequestBuyInWhenPlayerHasOpenedSession() {
        state.playerOpenedSession(1);

        verify(serverAdapter).notifyBuyInInfo(1, false);
    }
}
