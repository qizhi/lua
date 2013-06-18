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
import com.cubeia.poker.variant.PokerVariant;
import junit.framework.Assert;
import org.apache.log4j.Logger;

import static com.cubeia.poker.action.PokerActionType.ANTE;
import static com.cubeia.poker.action.PokerActionType.BET;
import static com.cubeia.poker.action.PokerActionType.CALL;
import static com.cubeia.poker.action.PokerActionType.CHECK;

public class TelesinaDisconnectTest extends AbstractTexasHandTester {

    Logger log = Logger.getLogger(this.getClass());

    @Override
    protected void setUp() throws Exception {
        variant = PokerVariant.TELESINA;
        rng = new NonRandomRNG();
        sitoutTimeLimitMilliseconds = 1;
        super.setUp();
        setAnteLevel(10);
    }

    public void testDisconnectBug() throws InterruptedException {
        MockPlayer[] mp = TestUtils.createMockPlayers(3, 100);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        //  --- ANTE ROUND ---
        act(p[1], ANTE);
        act(p[2], ANTE);
        act(p[0], ANTE);

        assertPlayersNumberOfCards(mp, 2, 2, 2);

        // --- NEW BETTING ROUND ---

        //timeout the DealInitialCardsRound
        state.timeout();

        // Player says he wants to sit out next hand and then leaves.
        state.playerSitsOutNextHand(p[1]);
        act(p[2], CHECK);
        act(p[0], CHECK);
        // And he times out (and checks) and set to away
        state.timeout();

        state.timeout(); // start next round
        assertPlayersNumberOfCards(mp, 3, 3, 3);

        // --- NEW BETTING ROUND ---
        act(p[0], BET);
        // player 1 will now be away and will be folded automatically
        act(p[2], CALL);
        Assert.assertTrue(mp[1].hasFolded());

        assertPlayersNumberOfCards(mp, 4, 3, 4);

        state.timeout();

        // Make sure mp[0] does not get any more cards
        assertPlayersNumberOfCards(mp, 4, 3, 4);
    }

    public void assertPlayersNumberOfCards(MockPlayer[] mp, int p0NumberOfCards, int p1NumberOfCards, int p2NumberOfCards) {
        assertEquals(p0NumberOfCards, mp[0].getPocketCards().getCards().size());
        assertEquals(p1NumberOfCards, mp[1].getPocketCards().getCards().size());
        assertEquals(p2NumberOfCards, mp[2].getPocketCards().getCards().size());
    }
}