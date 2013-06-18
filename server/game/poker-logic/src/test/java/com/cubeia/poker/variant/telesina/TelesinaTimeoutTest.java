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

package com.cubeia.poker.variant.telesina;

import com.cubeia.poker.AbstractTexasHandTester;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.NonRandomRNG;
import com.cubeia.poker.TestUtils;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.player.PokerPlayerStatus;
import com.cubeia.poker.variant.PokerVariant;
import org.junit.Test;

import java.math.BigDecimal;

import static com.cubeia.poker.action.PokerActionType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TelesinaTimeoutTest extends AbstractTexasHandTester {

    @Override
    protected void setUp() throws Exception {
        variant = PokerVariant.TELESINA;
        rng = new NonRandomRNG();
        super.setUp();
        setAnteLevel(10);
    }


    /**
     * Verify that table does not hang on a timeout during ante round
     */
    @Test
    public void testAnteTimeout() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        addPlayers(state, mp);

        // Set initial balances
        mp[0].setBalance(bd(100));
        mp[1].setBalance(bd(100));

        // Force start
        state.timeout();

        assertThat(mp[0].isActionPossible(ANTE), is(true));
        assertThat(mp[1].isActionPossible(ANTE), is(true));
    }


    /**
     * Verify timeout will exclude player from table
     */
    @Test
    public void testAnteTimeoutHand() {
        MockPlayer[] mp = TestUtils.createMockPlayers(3);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Set initial balances
        mp[0].setBalance(bd(100));
        mp[1].setBalance(bd(100));
        mp[2].setBalance(bd(100));

        // Force start
        state.timeout();

        // ANTE
        act(p[1], ANTE);
        // Timeout player 2
        state.timeout();
        act(p[0], ANTE);
    }

    @Test
    public void testAnteTimeoutHand2() {
        MockPlayer[] mp = TestUtils.createMockPlayers(3);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Set initial balances
        mp[0].setBalance(bd(100));
        mp[1].setBalance(bd(100));
        mp[2].setBalance(bd(100));

        // Force start
        state.timeout();

        // ANTE
        act(p[1], ANTE);
        act(p[2], ANTE);
        // Timeout player 0
        state.timeout();


        // make deal initial pocket cards round end
        state.timeout();

        assertEquals(PokerPlayerStatus.SITOUT, mockServerAdapter.getPokerPlayerStatus(p[0]));

        assertTrue(mp[2].isActionPossible(CHECK));
        act(p[2], PokerActionType.CHECK);
        act(p[1], PokerActionType.CHECK);

        assertNull(state.getPlayerInCurrentHand(p[0]));
    }

    @Test
    public void testPlayerOutOfMoney() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Set initial balances
        mp[0].setBalance(bd(100));
        mp[1].setBalance(bd(100));

        // Force start
        state.timeout();
        act(p[1], ANTE);
        act(p[0], ANTE);

        // make deal initial pocket cards round end
        state.timeout();

        act(p[1], BET, bd(90));
        act(p[0], CALL);
        // Progress until hand is complete
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        // End of hand

        assertFalse(mp[0].isSittingOut());

        // test start new hand
        state.timeout();

        assertTrue(mp[0].isSittingOut());
    }

    @Test
    public void testPlayerDeclinesAnte() {
        MockPlayer[] mp = TestUtils.createMockPlayers(3);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Set initial balances
        mp[0].setBalance(bd(100));
        mp[1].setBalance(bd(100));
        mp[2].setBalance(bd(100));

        // Force start
        state.timeout();

        // ANTE
        act(p[1], ANTE);
        act(p[2], DECLINE_ENTRY_BET);
        act(p[0], ANTE);

        // make deal initial pocket cards round end
        state.timeout();

        assertEquals(PokerPlayerStatus.SITOUT, mockServerAdapter.getPokerPlayerStatus(p[2]));

        assertTrue(mp[1].isActionPossible(CHECK));
        act(p[1], PokerActionType.CHECK);
        act(p[0], PokerActionType.CHECK);

        assertNull(state.getPlayerInCurrentHand(p[2]));
    }
}
