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

package com.cubeia.poker.tickets;

import com.cubeia.poker.AbstractTexasHandTester;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.TestUtils;
import com.cubeia.poker.action.PokerActionType;

/**
 * Test for Ticket 59
 */
public class Ticket59_TableFreeze extends AbstractTexasHandTester {

    /**
     * Mock Game is staked at 10/5'
     * Player default balance: 5000
     */
    public void testBothPlayerActs() {
        setAnteLevel(10);
        MockPlayer[] mp = TestUtils.createMockPlayers(4);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        assertEquals(1, mockServerAdapter.getTimeoutRequests());
        // Force start
        state.timeout();

        // Blinds
        act(p[1], PokerActionType.SMALL_BLIND);
        act(p[2], PokerActionType.BIG_BLIND);

        assertTrue(mp[3].isActionPossible(PokerActionType.CALL));
        assertEquals(bd(10), mockServerAdapter.getLastActionRequest().getOption(PokerActionType.CALL).getMinAmount());

    }

    /**
     * Mock Game is staked at 10/5'
     * Player default balance: 5000
     * <p/>
     * If big blind is declined (or timed out) the table can go to an infinite loop of asking
     * the next player to post big blind. If this player is timing out then it will never go further.
     */
    public void testBigBlindTimesOut() {
        setAnteLevel(10);
        MockPlayer[] mp = TestUtils.createMockPlayers(4);
        addPlayers(state, mp);

        assertEquals(1, mockServerAdapter.getTimeoutRequests());
        // Force start
        state.timeout();

        // Blinds
        state.timeout();
        state.timeout();

        assertTrue(mp[3].isActionPossible(PokerActionType.BIG_BLIND));
        state.timeout();

        assertTrue(mp[0].isActionPossible(PokerActionType.BIG_BLIND));
        state.timeout();

        assertTrue(state.isFinished());
        assertEquals("WaitingToStartState", state.getGameState().toString());

    }


}
