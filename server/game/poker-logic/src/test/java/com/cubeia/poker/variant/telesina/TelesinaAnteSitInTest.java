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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.cubeia.poker.AbstractTexasHandTester;
import com.cubeia.poker.MockPlayer;
import com.cubeia.poker.NonRandomRNG;
import com.cubeia.poker.TestUtils;
import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.variant.PokerVariant;

import java.math.BigDecimal;

public class TelesinaAnteSitInTest extends AbstractTexasHandTester {

    @Override
    protected void setUp() throws Exception {
        variant = PokerVariant.TELESINA;
        rng = new NonRandomRNG();
        super.setUp();
        setAnteLevel(20);
    }


    /**
     * Mock Game is staked at 20/10'
     */
    @Test
    public void testAnteSitIns() {
        MockPlayer[] mp = TestUtils.createMockPlayers(6, 100);
        MockPlayer[] startingPlayers = new MockPlayer[]{mp[0], mp[1]};
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, startingPlayers);

        // Force start
        state.timeout();

        // Blinds
        assertThat(mp[1].isActionPossible(PokerActionType.ANTE), is(true));
        assertThat(mp[0].isActionPossible(PokerActionType.ANTE), is(true));
        act(p[1], PokerActionType.ANTE);

        assertEquals(2, state.getSeatedPlayers().size());

        state.addPlayer(mp[3]);

        assertEquals(3, state.getSeatedPlayers().size());

        act(p[0], PokerActionType.ANTE);

        // timeout the DealInitialCardsRound
        state.timeout();

        act(p[1], PokerActionType.CHECK);
        act(p[0], PokerActionType.CHECK);

        state.timeout();
        act(p[1], PokerActionType.CHECK);
        act(p[0], PokerActionType.CHECK);

    }

    @Test
    public void testDoubleAnte() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2, 100);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();
        // Same player posts ante twice.
        act(p[1], PokerActionType.ANTE);
        BigDecimal before = mp[1].getBalance();
        act(p[1], PokerActionType.ANTE);
        assertEquals(before, mp[1].getBalance());
    }

    @Test
    public void testAnteSitOutThenSitIt() {
        MockPlayer[] mp = TestUtils.createMockPlayers(3, 100);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Blinds
        act(p[1], PokerActionType.ANTE);
        act(p[2], PokerActionType.DECLINE_ENTRY_BET);

        // Assert that player 2 not in the current players
        assertNull(state.getPlayerInCurrentHand(p[2]));

        act(p[0], PokerActionType.ANTE);

        // timeout the DealInitialCardsRound
        state.timeout();

        // Now player 2 should not be in the hard nor be awarded cards
        assertEquals(2, state.getPlayerInCurrentHand(p[1]).getPocketCards().getCards().size());
        assertEquals(2, state.getPlayerInCurrentHand(p[0]).getPocketCards().getCards().size());


        act(p[1], PokerActionType.CHECK);
        act(p[0], PokerActionType.CHECK);
    }

}
