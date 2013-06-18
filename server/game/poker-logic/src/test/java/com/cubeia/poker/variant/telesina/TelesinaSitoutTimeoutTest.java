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
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.util.SitoutCalculator;
import com.cubeia.poker.variant.PokerVariant;
import junit.framework.Assert;

import java.util.Collection;

import static com.cubeia.poker.action.PokerActionType.ANTE;
import static com.cubeia.poker.action.PokerActionType.DECLINE_ENTRY_BET;

public class TelesinaSitoutTimeoutTest extends AbstractTexasHandTester {

    @Override
    protected void setUp() throws Exception {
        variant = PokerVariant.TELESINA;
        rng = new NonRandomRNG();
        sitoutTimeLimitMilliseconds = 1;
        super.setUp();
        setAnteLevel(10);
    }

    public void testTimeoutCalculation() throws InterruptedException {
        MockPlayer[] mp = TestUtils.createMockPlayers(3, 100);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // ANTE
        act(p[1], ANTE);
        // Timeout player 2, this will start a scheduled action that should
        // remove him from the table
        act(p[2], DECLINE_ENTRY_BET);
        assertTrue(mp[2].isSittingOut());
        assertTrue(mp[2].getSitOutTimestamp() <= System.currentTimeMillis());
        // Assert that player 0 has received an action request
        act(p[0], ANTE);

        // Make sure we are timing out the sit out timeout
        Thread.sleep(100);
        assertTrue(mp[2].getSitOutTimestamp() + sitoutTimeLimitMilliseconds < System.currentTimeMillis());

        SitoutCalculator calculator = new SitoutCalculator();
        Collection<PokerPlayer> timeouts = calculator.checkTimeoutPlayers(state.getSeatedPlayers(), sitoutTimeLimitMilliseconds);
        Assert.assertEquals(1, timeouts.size());
        Assert.assertEquals(mp[2], timeouts.iterator().next());
    }


}
