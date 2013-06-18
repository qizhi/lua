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

package com.cubeia.poker.betlevel;

import com.cubeia.poker.AbstractTexasHandTester;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.TestUtils;
import com.cubeia.poker.action.PokerActionType;

/**
 * Integration test for poker logic.
 */
public class BlindsLevelTest extends AbstractTexasHandTester {

    public void testSimpleHoldemHand() {
        setAnteLevel(100);
        MockPlayer[] mp = TestUtils.createMockPlayers(4);
        int[] p = TestUtils.createPlayerIdArray(mp);
        assertEquals(4, p.length);
        addPlayers(state, mp);
        assertEquals(4, state.getSeatedPlayers().size());

        // Force start
        state.timeout();
        assertEquals(101, mockServerAdapter.getLastActionRequest().getPlayerId());
        assertEquals(bd("50.00"), mockServerAdapter.getLastActionRequest().getOption(PokerActionType.SMALL_BLIND).getMinAmount());
        assertEquals(bd("50.00"), mockServerAdapter.getLastActionRequest().getOption(PokerActionType.SMALL_BLIND).getMaxAmount());

        // Blinds
        act(p[1], PokerActionType.SMALL_BLIND);

        assertTrue(mp[2].isActionPossible(PokerActionType.BIG_BLIND));
        assertEquals(102, mockServerAdapter.getLastActionRequest().getPlayerId());
        assertEquals(bd("100.00"), mockServerAdapter.getLastActionRequest().getOption(PokerActionType.BIG_BLIND).getMinAmount());
        assertEquals(bd("100.00"), mockServerAdapter.getLastActionRequest().getOption(PokerActionType.BIG_BLIND).getMaxAmount());
        act(p[2], PokerActionType.BIG_BLIND);

    }

}
