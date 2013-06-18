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

package com.cubeia.poker.rounds.betting;

import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.player.PokerPlayer;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Bet strategy for deciding what the min and max bets are, given the situation.
 * <p/>
 * For fixed limit, we need to know:
 * 1. Min bet (will vary between rounds)
 * 2. Player to act's current bet stack and total stack
 * 3. The number of bets and raises
 * 4. The max number of bets and raises allowed (usually 4)
 */
public interface BetStrategy extends Serializable {

    /**
     * Gets the type of bet strategy implemented by this class.
     * @return
     */
    BetStrategyType getType();

    /**
     * Gets the min raise TO amount, that is the lowest amount that a player can raise TO.
     *
     * Example 1: Player A bets $10, player B calls and player C raises to $20. Min raise-to for player A is now $30, meaning
     * he can pay $20 to raise the bet by $10, from $20 to $30.
     *
     * Example 2: Player A bets $10, player B calls and player C goes all-in to $14.
     * Fixed limit: Min raise-to for player A is now $20, because C's all-in counts as a call.
     * No limit: Min raise-to for player A is $24.
     *
     * If the player cannot afford to raise, 0 should be returned.
     * If the player can afford an incomplete raise, that raise-to amount should be returned.
     *
     * @return the min raise to amount, or 0 if the player cannot afford to raise.
     */
    public BigDecimal getMinRaiseToAmount(BettingRoundContext bettingRoundContext, PokerPlayer player);

    /**
     * Gets the max raise TO amount, that is the biggest amount that a player can raise TO.
     *
     * If the player cannot afford to raise, 0 should be returned.
     * If the player can afford an incomplete raise, that raise-to amount should be returned.
     *
     * @return the max raise to amount, or 0 if the player cannot afford to raise.
     */
    public BigDecimal getMaxRaiseToAmount(BettingRoundContext bettingRoundContext, PokerPlayer player);

    /**
     * Gets the minimum allowed bet amount. If the player does not have enough money for a min bet,
     * the player's balance should be returned.
     *
     */
    public BigDecimal getMinBetAmount(BettingRoundContext bettingRoundContext, PokerPlayer player);

    /**
     * Gets the maximum allowed bet amount.
     *
     */
    public BigDecimal getMaxBetAmount(BettingRoundContext bettingRoundContext, PokerPlayer player);

    /**
     * Gets the amount need for the given player to call. If the player does not have enough money for a call,
     * the player's balance should be returned.
     *
     */
    public BigDecimal getCallAmount(BettingRoundContext bettingRoundContext, PokerPlayer player);

    /**
     * Gets the next valid raise level.
     *
     * Fixed limit examples:
     * 1. A bets $10, B raises to $20. Next valid raise level = $30
     * 2. A bets $10, B goes all-in for $12, next valid raise level = $20. (Because $12 is not a complete bet)
     * 3. A bets $10, A goes all-in for $15 (which is a complete bet). Next valid raise level = $30.
     *
     * No limit examples:
     * 1. A bets $10, B raises to $25. Next valid raise level = $40. (size of the raise was $15, so next raise must be by $15).
     * 2. A bets $10, B raises to $19. Next valid raise level = $20.
     *
     */
    BigDecimal getNextValidRaiseToLevel(BettingRoundContext context);

    /**
     * Checks if this bet or raise is a complete bet or raise.
     *
     * Fixed limit:
     * If the current bet level is $10 and the next level is $20, then a raise to $15 or more is considered complete, anything below is incomplete.
     *
     * No limit:
     * If the current bet level is $10 and the next level is $20, then a raise to $20 or more is considered complete, anything below is incomplete.
     *
     * @param context
     * @param amountRaisedOrBetTo
     * @return
     */
    boolean isCompleteBetOrRaise(BettingRoundContext context, BigDecimal amountRaisedOrBetTo);

    /**
     * Checks whether betting should be capped given the number of bets and raises.
     *
     */
    boolean shouldBettingBeCapped(int betsAndRaises, boolean headsUp);
}
