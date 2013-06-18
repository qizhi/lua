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

import java.math.BigDecimal;

/**
 * Test for Ticket 44:
 * When all players are all-in pre-flop it seems that (sometimes) there is one too many timeouts scheduled.
 * This causes the river and turn to come directly and the next hand to start right away without any pause.
 */
public class Ticket44_TooManyTimeouts extends AbstractTexasHandTester {

    /**
     * Mock Game is staked at 10/5'
     * Player default balance: 5000
     */
    public void testAllInHoldemHand() {
        setAnteLevel(10);
        MockPlayer[] mp = TestUtils.createMockPlayers(4);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);
        mp[0].setBalance(new BigDecimal("9000.00"));

        // Force start
        state.timeout();
        // Blinds
        act(p[1], PokerActionType.SMALL_BLIND);
        act(p[2], PokerActionType.BIG_BLIND);
        act(p[3], PokerActionType.RAISE, 5000);    // ALL IN
        act(p[0], PokerActionType.CALL);        // 4000 remaining
        act(p[1], PokerActionType.CALL);        // ALL IN
        mockServerAdapter.clear();
        act(p[2], PokerActionType.CALL);        // ALL IN

        assertEquals(bd(4000), mp[0].getBalance());
        assertEquals(1, mockServerAdapter.getTimeoutRequests());

        // Trigger deal community cards
        state.timeout();

        // FLOP
        // THIS IS THE BUG. The assert below should not be true since mp[0] is not all-in
        // assertTrue(mp[0].isActionPossible(PokerActionType.CHECK));
        assertFalse(mp[0].isActionPossible(PokerActionType.CHECK));
        assertFalse(mp[3].isActionPossible(PokerActionType.CHECK));
        assertEquals(3, state.getCommunityCards().size());

        // Trigger all-in FLOP round timeout
        state.timeout();
        // Trigger deal community cards
        state.timeout();

        // TURN
        assertFalse(mp[0].isActionPossible(PokerActionType.CHECK));
        assertFalse(mp[3].isActionPossible(PokerActionType.CHECK));
        assertEquals(4, state.getCommunityCards().size());

    }

}
