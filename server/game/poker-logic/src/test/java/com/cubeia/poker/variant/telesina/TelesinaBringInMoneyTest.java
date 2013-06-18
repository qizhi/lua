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
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static com.cubeia.poker.action.PokerActionType.*;

public class TelesinaBringInMoneyTest extends AbstractTexasHandTester {

    @Override
    protected void setUp() throws Exception {
        variant = PokerVariant.TELESINA;
        rng = new NonRandomRNG();
        super.setUp();
        setAnteLevel(10);
    }

    @Test
    public void testBringInMoneyInHand() {
        setAnteLevel(10);
        MockPlayer[] mp = TestUtils.createMockPlayers(3, "100.00");
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();
        act(p[1], ANTE);
        act(p[2], ANTE);
        act(p[0], ANTE);

        // timeout the DealInitialCardsRound
        state.timeout();

        act(p[2], BET, "90.00");
        act(p[0], CALL);
        act(p[1], CALL);

        // Progress until hand is complete
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        bringInMoneyToMp2(mp, p);
        state.timeout();
        // End of hand

        assertEquals(bd(0), mp[0].getBalance());
        assertEquals(bd(300), mp[1].getBalance());
        assertEquals(bd(50), mp[2].getBalanceNotInHand());

        assertFalse(mp[0].isSittingOut());
        assertFalse(mp[2].isSittingOut());

        state.timeout();

        assertTrue(mp[0].isSittingOut());
        assertFalse(mp[2].isSittingOut());

        assertEquals(bd(50), mp[2].getBalance());
        act(p[2], ANTE);
        act(p[1], ANTE);

        // timeout the DealInitialCardsRound
        state.timeout();

        // Now game should progress to betting round or we have a bug!
        Assert.assertNotNull(mp[2].getActionRequest().getOption(PokerActionType.CHECK));

    }

    @Test
    public void testBringInMoneyBetweenHands() {
        setAnteLevel(10);
        MockPlayer[] mp = TestUtils.createMockPlayers(3, 100);
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();
        act(p[1], ANTE);
        act(p[2], ANTE);
        act(p[0], ANTE);

        // timeout the DealInitialCardsRound
        state.timeout();

        act(p[2], BET, 90);
        act(p[0], CALL);
        act(p[1], CALL);

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

        assertEquals(bd(0), mp[0].getBalance());
        assertEquals(bd(300), mp[1].getBalance());
        assertEquals(bd(0), mp[2].getBalance());

        assertFalse(mp[0].isSittingOut());
        assertFalse(mp[2].isSittingOut());

        bringInMoneyToMp2(mp, p);

        state.timeout();

        assertTrue(mp[0].isSittingOut());
        assertFalse(mp[2].isSittingOut());

        act(p[2], ANTE);
        act(p[1], ANTE);

        // timeout the DealInitialCardsRound
        state.timeout();

        // Now game should progress to betting round or we have a bug!
        Assert.assertNotNull(mp[2].getActionRequest().getOption(PokerActionType.CHECK));

    }


    @Test
    public void testSitInNextHand() {
        setAnteLevel(10);
        MockPlayer[] mp = TestUtils.createMockPlayers(3, "100.00");
        int[] p = TestUtils.createPlayerIdArray(mp);
        addPlayers(state, mp);

        // Force start
        state.timeout();
        act(p[1], ANTE);
        act(p[2], ANTE);
        act(p[0], ANTE);

        // timeout the DealInitialCardsRound
        state.timeout();

        act(p[2], BET, 90);
        act(p[0], CALL);
        act(p[1], CALL);

        // Progress until hand is complete
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        state.timeout();
        bringInMoneyToMp2(mp, p);

        assertEquals(bd(50), mp[2].getBalanceNotInHand());

        state.timeout();
        // End of hand

        assertFalse(mp[2].isSittingOut());
        assertEquals(bd(0), mp[0].getBalance());
        assertEquals(bd(300), mp[1].getBalance());
        assertEquals(bd(50), mp[2].getBalanceNotInHand());
        assertFalse(mp[0].isSittingOut());

        // Start new hand
        state.timeout();

        assertTrue(mp[0].isSittingOut());

        // PLayer 2 should now have the pending balance committed
        assertEquals(bd(50), mp[2].getBalance());

        act(p[2], ANTE);
        act(p[1], ANTE);

        // timeout the DealInitialCardsRound
        state.timeout();

        state.playerIsSittingIn(p[0]);

        act(p[2], CHECK);
        act(p[1], FOLD);

        assertTrue(state.isFinished());
    }

    private void bringInMoneyToMp2(MockPlayer[] mp, int[] p) {
        // Player 2 brings in more cash between hands
        // Mimic the logic executed in the back end handler, this is brittle - if the back end handler
        // implementation changes then that behavior will not be used here. Never the less...
        BigDecimal amountReserved = new BigDecimal("50.00");
        if (state.isPlayerInHand(p[2])) {
            System.out.println("player is in hand, adding reserved amount " + amountReserved + " as pending");
            mp[2].addNotInHandAmount(amountReserved);
        } else {
            System.out.println("player is not in hand, adding reserved amount " + amountReserved + " to balance");
            mp[2].addChips(amountReserved);
        }

        state.playerIsSittingIn(p[2]);
    }

}
