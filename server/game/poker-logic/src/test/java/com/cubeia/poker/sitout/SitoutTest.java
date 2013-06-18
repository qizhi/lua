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

package com.cubeia.poker.sitout;

import com.cubeia.poker.GuiceTest;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.PokerState;
import com.cubeia.poker.TestUtils;
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.player.PokerPlayerStatus;

import java.math.BigDecimal;

/**
 * Integration test for poker logic.
 */
public class SitoutTest extends GuiceTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDeclinedPlayerSitsOut() {
        mockServerAdapter.clear();
        MockPlayer[] mp = TestUtils.createMockPlayers(3);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        act(p[1], PokerActionType.DECLINE_ENTRY_BET);
        assertTrue(mp[1].isSittingOut());
        assertEquals(PokerPlayerStatus.SITOUT, mockServerAdapter.getPokerPlayerStatus(p[1]));

        act(p[2], PokerActionType.DECLINE_ENTRY_BET);
        assertTrue(mp[2].isSittingOut());
        assertEquals(PokerPlayerStatus.SITOUT, mockServerAdapter.getPokerPlayerStatus(p[2]));
    }

    public void testBlindsTimeout() {
        mockServerAdapter.clear();
        MockPlayer[] mp = TestUtils.createMockPlayers(3);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        state.timeout();
        assertTrue(mp[1].isSittingOut());
        assertEquals(PokerPlayerStatus.SITOUT, mockServerAdapter.getPokerPlayerStatus(p[1]));

        System.out.println("Next request: " + mockServerAdapter.getLastActionRequest());

        state.timeout();
        assertTrue(mp[2].isSittingOut());
        assertEquals(PokerPlayerStatus.SITOUT, mockServerAdapter.getPokerPlayerStatus(p[2]));
    }

    private void act(int playerId, PokerActionType actionType) {
        act(playerId, actionType, mockServerAdapter.getLastActionRequest().getOption(actionType).getMinAmount());
    }

    private void act(int playerId, PokerActionType actionType, BigDecimal amount) {
        PokerAction action = new PokerAction(playerId, actionType);
        action.setBetAmount(amount);
        state.act(action);
    }

    private void addPlayers(PokerState game, PokerPlayer[] p) {
        for (PokerPlayer pl : p) {
            game.addPlayer(pl);
        }
    }

}
