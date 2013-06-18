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

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.games.poker.state.FirebaseState;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class LobbyUpdaterTest {

    @Mock
    private FirebaseState fbState;
    @Mock
    private Table table;
    @Mock
    private LobbyTableAttributeAccessor lobbyTableAttributeAccessor;

    @Test
    public void testUpdateLobby() {
        initMocks(this);
        when(table.getAttributeAccessor()).thenReturn(lobbyTableAttributeAccessor);
        int handCount = 234;
        when(fbState.getHandCount()).thenReturn(handCount);
        LobbyUpdater lobbyUpdater = new LobbyUpdater();

        lobbyUpdater.updateLobby(fbState, table);

        ArgumentCaptor<AttributeValue> attributeCaptor = ArgumentCaptor.forClass(AttributeValue.class);
        verify(lobbyTableAttributeAccessor).setAttribute(Mockito.eq("handcount"), attributeCaptor.capture());
        AttributeValue attributeValue = attributeCaptor.getValue();
        assertThat(attributeValue.getIntValue(), is(handCount));
    }

}
