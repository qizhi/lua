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

package com.cubeia.games.poker.tournament.state;

import org.junit.Before;
import org.junit.Test;

import static com.cubeia.games.poker.tournament.state.PendingBackendRequests.PendingRequestType.REBUY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PendingBackendRequestsTest {

    private PendingBackendRequests pendingRequests;

    @Before
    public void setup() {
        pendingRequests = new PendingBackendRequests();
    }

    @Test
    public void testGetAndClearPendingRequest() {
        pendingRequests.addPendingRequest(101, 2, REBUY);
        assertThat(pendingRequests.tableHasPendingRequests(2), is(true));
        pendingRequests.getAndClearPendingRequest(101, 2);
        assertThat(pendingRequests.tableHasPendingRequests(2), is(false));
    }
}
