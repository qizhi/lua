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

package com.cubeia.backend.firebase;

import com.cubeia.backend.cashgame.PlayerSessionId;
import com.cubeia.backend.cashgame.TournamentId;
import com.cubeia.backend.cashgame.dto.CloseSessionRequest;
import com.cubeia.backend.cashgame.dto.OpenSessionResponse;
import com.cubeia.backend.cashgame.dto.OpenTournamentSessionRequest;
import org.junit.Test;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;

public class MockBackendAdapterTest {

    private MockBackendAdapter adapter = new MockBackendAdapter();

    @Test
    public void testSomething() {
        // Open two sessions
        OpenSessionResponse openSessionResponse = adapter.openSession(new OpenTournamentSessionRequest(-1, new TournamentId(UUID.randomUUID().toString(), 45), null));
        adapter.openSession(new OpenTournamentSessionRequest(-1, new TournamentId(UUID.randomUUID().toString(), 46), null));

        assertEquals(2, adapter.getSessionCount());

        // Close one of them.
        PlayerSessionId tournamentSessionId = openSessionResponse.getSessionId();
        adapter.closeSession(new CloseSessionRequest(tournamentSessionId));
        assertEquals(1, adapter.getSessionCount());
    }
}
