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
import com.cubeia.poker.action.PokerAction;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.variant.PokerVariant;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TelesinaRoundTimeoutTest extends AbstractTexasHandTester {

    @Override
    protected void setUp() throws Exception {
        variant = PokerVariant.TELESINA;
        rng = new NonRandomRNG();
        super.setUp();
        setAnteLevel(20);
    }


    @Test
    public void testTimeoutFlagWhenAnteRoundEndsWithoutAnyPlayerActing() {

        MockPlayer[] mp = TestUtils.createMockPlayers(2, 100);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        assertThat(mp[1].isActionPossible(PokerActionType.ANTE), is(true));
        assertThat(mp[0].isActionPossible(PokerActionType.ANTE), is(true));

        act(p[1], PokerActionType.ANTE);

        state.timeout();

        PokerAction action0 = mockServerAdapter.getNthAction(0);
        PokerAction action1 = mockServerAdapter.getNthAction(1);

        assertThat(action0.getActionType(), is(PokerActionType.ANTE));
        assertThat(action1.getActionType(), is(PokerActionType.DECLINE_ENTRY_BET));

        assertThat(action0.isTimeout(), is(false));
        assertThat(action1.isTimeout(), is(true));

    }

    @Test
    public void testTimeoutFlagWhenAnteRoundForBothPlayersWhenFirstPlayerTimesOut() {

        MockPlayer[] mp = TestUtils.createMockPlayers(2, 100);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        assertThat(mp[1].isActionPossible(PokerActionType.ANTE), is(true));
        assertThat(mp[0].isActionPossible(PokerActionType.ANTE), is(true));

        state.timeout();

        PokerAction action0 = mockServerAdapter.getNthAction(0);
        PokerAction action1 = mockServerAdapter.getNthAction(1);

        assertThat(action0.isTimeout(), is(true));
        assertThat(action1.isTimeout(), is(true));

    }

    @Test
    public void testTimeoutFlagWhenActing() {

        MockPlayer[] mp = TestUtils.createMockPlayers(3, 100);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        assertThat(mp[1].isActionPossible(PokerActionType.ANTE), is(true));
        assertThat(mp[0].isActionPossible(PokerActionType.ANTE), is(true));
        assertThat(mp[2].isActionPossible(PokerActionType.ANTE), is(true));

        // Pay ante
        act(p[1], PokerActionType.ANTE);
        act(p[0], PokerActionType.ANTE);
        act(p[2], PokerActionType.ANTE);

        PokerAction action0 = mockServerAdapter.getNthAction(0);
        PokerAction action1 = mockServerAdapter.getNthAction(1);
        PokerAction action2 = mockServerAdapter.getNthAction(2);

        assertThat(action0.isTimeout(), is(false));
        assertThat(action0.getActionType(), is(PokerActionType.ANTE));

        assertThat(action1.isTimeout(), is(false));
        assertThat(action1.getActionType(), is(PokerActionType.ANTE));

        assertThat(action2.isTimeout(), is(false));
        assertThat(action2.getActionType(), is(PokerActionType.ANTE));

        // NEXT ROUND
        // make deal initial pocket cards round end
        state.timeout();

        assertThat(mp[2].isActionPossible(PokerActionType.BET), is(true));
        act(p[2], PokerActionType.BET);

        assertThat(mp[0].isActionPossible(PokerActionType.CALL), is(true));
        act(p[0], PokerActionType.CALL);

        assertThat(mp[1].isActionPossible(PokerActionType.CALL), is(true));
        act(p[1], PokerActionType.CALL);


        action0 = mockServerAdapter.getNthAction(3);
        action1 = mockServerAdapter.getNthAction(4);
        action2 = mockServerAdapter.getNthAction(5);

        assertThat(action0.isTimeout(), is(false));
        assertThat(action0.getActionType(), is(PokerActionType.BET));
        assertThat(action1.isTimeout(), is(false));
        assertThat(action1.getActionType(), is(PokerActionType.CALL));
        assertThat(action2.isTimeout(), is(false));
        assertThat(action2.getActionType(), is(PokerActionType.CALL));

        // NEXT ROUND
        state.timeout();

        assertThat(mp[0].isActionPossible(PokerActionType.CHECK), is(true));
        act(p[0], PokerActionType.CHECK);

        assertThat(mp[1].isActionPossible(PokerActionType.CHECK), is(true));
        act(p[1], PokerActionType.CHECK);

        assertThat(mp[2].isActionPossible(PokerActionType.CHECK), is(true));
        act(p[2], PokerActionType.CHECK);

        action0 = mockServerAdapter.getNthAction(6);
        action1 = mockServerAdapter.getNthAction(7);
        action2 = mockServerAdapter.getNthAction(8);

        assertThat(action0.isTimeout(), is(false));
        assertThat(action0.getActionType(), is(PokerActionType.CHECK));
        assertThat(action1.isTimeout(), is(false));
        assertThat(action1.getActionType(), is(PokerActionType.CHECK));
        assertThat(action2.isTimeout(), is(false));
        assertThat(action2.getActionType(), is(PokerActionType.CHECK));


        // NEXT ROUND, CHECK TIMEOUT
        state.timeout();

        assertThat(mp[1].isActionPossible(PokerActionType.BET), is(true));
        act(p[1], PokerActionType.BET);

        assertThat(mp[2].isActionPossible(PokerActionType.CALL), is(true));
        state.timeout(); // Dont act timeout

        assertThat(mp[0].isActionPossible(PokerActionType.CALL), is(true));
        act(p[0], PokerActionType.CALL);


        action0 = mockServerAdapter.getNthAction(9);
        action1 = mockServerAdapter.getNthAction(10);
        action2 = mockServerAdapter.getNthAction(11);

        assertThat(action0.isTimeout(), is(false));
        assertThat(action0.getActionType(), is(PokerActionType.BET));
        assertThat(action1.isTimeout(), is(true));
        assertThat(action1.getActionType(), is(PokerActionType.FOLD));
        assertThat(action2.isTimeout(), is(false));
        assertThat(action2.getActionType(), is(PokerActionType.CALL));


    }


}
