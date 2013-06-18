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
import com.cubeia.poker.variant.PokerVariant;
import org.junit.Test;

public class TelesinaAnteRageQuitTest extends AbstractTexasHandTester {

    @Override
    protected void setUp() throws Exception {
        variant = PokerVariant.TELESINA;
        rng = new NonRandomRNG();
        super.setUp();
        setAnteLevel(20);
    }

    @Test
    public void testAnteSitOutThenSitOut() {
        MockPlayer[] mp = TestUtils.createMockPlayers(2, 100);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();

        // Antes
        act(p[1], PokerActionType.ANTE);
        state.playerSitsOutNextHand(p[1]);
        act(p[0], PokerActionType.ANTE);

        state.timeout();

        // Verify that both players get 2 cards dealt (one hidden and one public)
        // The rage quit bug was that the player sitting out, p[1], did not get the hidden card.
        assertEquals(2, state.getPlayerInCurrentHand(p[1]).getPocketCards().getCards().size());
        assertEquals(1, state.getPlayerInCurrentHand(p[1]).getPrivatePocketCards().size());
        assertEquals(2, state.getPlayerInCurrentHand(p[0]).getPocketCards().getCards().size());
        assertEquals(1, state.getPlayerInCurrentHand(p[0]).getPrivatePocketCards().size());
    }

}
