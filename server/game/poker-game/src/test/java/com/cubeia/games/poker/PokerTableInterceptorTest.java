/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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
import com.cubeia.firebase.api.game.table.SeatRequest;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.poker.PokerState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PokerTableInterceptorTest {

    @Mock
    private Table table;

    @Mock
    PokerState state;

    @Mock
    StateInjector stateInjector;

    @InjectMocks
    private PokerTableInterceptor interceptor = new PokerTableInterceptor();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testAllowJoin() {
        // Given a table that is shut down.
        when(state.isShutDown()).thenReturn(true);

        // When we check if joining is allowed.
        SeatRequest request = new SeatRequest(0, 1, null);
        InterceptionResponse response = interceptor.allowJoin(table, request);

        // The response should be false.
        assertThat(response.isAllowed(), is(false));
    }
}
