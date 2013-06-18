/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.poker.rounds.betting;

import com.cubeia.poker.player.PokerPlayer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FixedLimitBetStrategyTest {

    private FixedLimitBetStrategy strategy;

    @Mock
    private BettingRoundContext context;

    @Mock
    private PokerPlayer player;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        strategy = new FixedLimitBetStrategy(bd(10), false);
    }

    private BigDecimal bd(int i) {
        return new BigDecimal(i);
    }

    /**
     * Example 1: Player A bets $10, player B calls and player C raises to $20. Min raise-to for player A is now $30, meaning
     * he can pay $20 to raise the bet by $10, from $20 to $30.
     *
     *
     * If the player cannot afford to raise, 0 should be returned.
     *
     */
    @Test
    public void testGetMinRaiseToAmountExample1() {
        when(player.getBalance()).thenReturn(bd(100));
        when(player.getBetStack()).thenReturn(bd(0));
        when(context.getHighestBet()).thenReturn(bd(20));
        when(context.getHighestCompleteBet()).thenReturn(bd(20));

        assertThat(strategy.getMinRaiseToAmount(context, player), is(bd(30)));
    }

    /**
     * Example 2: Player A bets $10, player B calls and player C goes all-in to $14 (which is an incomplete bet both in no-limit and fixed limit).
     * Min raise-to for player A is now $20, because C's all-in counts as a call.
     */
    @Test
    public void testGetMinRaiseToAmountExample2() {
        when(player.getBalance()).thenReturn(bd(100));
        when(player.getBetStack()).thenReturn(bd(0));
        when(context.getHighestBet()).thenReturn(bd(10));
        when(context.getHighestCompleteBet()).thenReturn(bd(10));

        assertThat(strategy.getMinRaiseToAmount(context, player), is(bd(20)));
    }

    /**
     * If the player cannot afford to raise, 0 should be returned.
     *
     */
    @Test
    public void testGetMinRaiseToAmountExample3() {
        when(player.getBalance()).thenReturn(bd(9));
        when(player.getBetStack()).thenReturn(bd(0));
        when(context.getHighestBet()).thenReturn(bd(20));
        when(context.getHighestCompleteBet()).thenReturn(bd(20));

        assertThat(strategy.getMinRaiseToAmount(context, player), is(bd(0)));
    }

    /**
     * If the player cannot afford a full raise, return the highest possible raise.
     *
     */
    @Test
    public void testGetMinRaiseToAmountExample4() {
        when(player.getBetStack()).thenReturn(bd(10));
        when(player.getBalance()).thenReturn(bd(12));
        when(context.getHighestBet()).thenReturn(bd(20));
        when(context.getHighestCompleteBet()).thenReturn(bd(20));

        assertThat(strategy.getMinRaiseToAmount(context, player), is(bd(22)));
    }

    @Test
    public void testGetMaxRaiseToAmount() {
        when(player.getBetStack()).thenReturn(bd(10));
        when(player.getBalance()).thenReturn(bd(12));
        when(context.getHighestBet()).thenReturn(bd(20));
        when(context.getHighestCompleteBet()).thenReturn(bd(20));

        assertThat(strategy.getMaxRaiseToAmount(context, player), is(bd(22)));
    }

    /**
     * Gets the minimum allowed bet amount.
     *
     */
    @Test
    public void testGetMinBetAmount() {
        when(player.getBalance()).thenReturn(bd(12));
        when(context.getHighestCompleteBet()).thenReturn(bd(0));

        assertThat(strategy.getMinBetAmount(context, player), is(bd(10)));
    }

    /**
     * If the player does not have enough money for a min bet,
     * the player's balance should be returned.
     */
    @Test
    public void testGetMinBetAmountWhenNotEnoughForCompleteBet() {
        when(player.getBalance()).thenReturn(bd(8));
        when(context.getHighestCompleteBet()).thenReturn(bd(0));

        assertThat(strategy.getMinBetAmount(context, player), is(bd(8)));
    }

    /**
     * Gets the maximum allowed bet amount.
     *
     */
    @Test
    public void testGetMaxBetAmount() {
        when(player.getBalance()).thenReturn(bd(8));
        when(context.getHighestCompleteBet()).thenReturn(bd(0));

        assertThat(strategy.getMaxBetAmount(context, player), is(bd(8)));
    }

    /**
     * Gets the amount need for the given player to call. If the player does not have enough money for a call,
     * the player's balance should be returned.
     *
     */
    @Test
    public void testGetCallAmount() {
        when(player.getBalance()).thenReturn(bd(100));
        when(player.getBetStack()).thenReturn(bd(10));
        when(context.getHighestBet()).thenReturn(bd(20));

        assertThat(strategy.getCallAmount(context, player), is(bd(10)));
    }

    @Test
    public void testGetCallAmountWhenThereIsAnIncompleteBet() {
        when(player.getBalance()).thenReturn(bd(100));
        when(player.getBetStack()).thenReturn(bd(10));
        when(context.getHighestBet()).thenReturn(bd(14));

        assertThat(strategy.getCallAmount(context, player), is(bd(4)));
    }

    @Test
    public void testGetCallAmountWhenPlayerCannotAffordToCall() {
        when(player.getBetStack()).thenReturn(bd(10));
        when(player.getBalance()).thenReturn(bd(6));
        when(context.getHighestBet()).thenReturn(bd(20));

        assertThat(strategy.getCallAmount(context, player), is(bd(6)));
    }

    /**
     * Fixed limit examples:
     * 1. A bets $10, B raises to $20. Next valid raise level = $30
     *
     */
    @Test
    public void testGetNextValidRaiseToLevelExample1() {
        when(context.getHighestCompleteBet()).thenReturn(bd(20));
        assertThat(strategy.getNextValidRaiseToLevel(context), is(bd(30)));
    }

    /**
     * Fixed limit examples:
     * 2. A bets $10, B goes all-in for $12, next valid raise level = $20. (Because $12 is not a complete bet)
     *
     */
    @Test
    public void testGetNextValidRaiseToLevelExample2() {
        when(context.getHighestCompleteBet()).thenReturn(bd(10));
        when(context.getHighestBet()).thenReturn(bd(12));
        assertThat(strategy.getNextValidRaiseToLevel(context), is(bd(20)));
    }

    /**
     * Fixed limit examples:
     * 3. A bets $10, A goes all-in for $15 (which is a complete bet). Next valid raise level = $30.
     *
     */
    @Test
    public void testGetNextValidRaiseToLevelExample3() {
        when(context.getHighestCompleteBet()).thenReturn(bd(20));
        when(context.getHighestBet()).thenReturn(bd(15));
        assertThat(strategy.getNextValidRaiseToLevel(context), is(bd(30)));
    }

    /**
     * Fixed limit:
     * If the current bet level is $10 and the next level is $20, then a raise to $15 or more is considered complete, anything below is incomplete.
     *
     */
    @Test
    public void testIsCompleteBetOrRaise() throws Exception {
        when(context.getHighestCompleteBet()).thenReturn(bd(10));

        assertThat(strategy.isCompleteBetOrRaise(context, bd(14)), is(false));
        assertThat(strategy.isCompleteBetOrRaise(context, bd(15)), is(true));
    }

    @Test
    public void testShouldBettingBeCapped() {
        assertThat(strategy.shouldBettingBeCapped(3, false), is(false));
        assertThat(strategy.shouldBettingBeCapped(4, false), is(true)); // We reached max bets, betting is capped.

        assertThat(strategy.shouldBettingBeCapped(3, true), is(false));
        assertThat(strategy.shouldBettingBeCapped(4, true), is(false)); // We are heads up, betting should not be capped.
        assertThat(strategy.shouldBettingBeCapped(4, true), is(false)); // We are heads up, betting should not be capped.
    }

}
