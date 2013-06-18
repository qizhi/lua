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

import java.math.BigDecimal;

import static java.lang.Math.min;

/**
 * Implementation of no limit betting strategy.
 * <p/>
 * Rules:
 * Players may bet or raise their entire stack at any given time.
 * Exception: when all players are all-in except for the player to act, in this case he may only call or fold.
 * When betting, the size of the bet must be >= the min bet according to the configuration.
 * When raising, the size of the raise must be >= the size of the last bet or raise.
 * <p/>
 * What we need to know:
 * 1. Min bet (as configured)
 * 2. Player to act's current bet stack and total stack
 * 3. The currently highest (valid) bet (that is, an incomplete bet or raise should not count).
 * 4. The size of the last raise or bet
 * 5. Are all other players all-in?
 */
public class NoLimitBetStrategy implements BetStrategy {

    private static final long serialVersionUID = 1L;

    private final BigDecimal minBet;

    public NoLimitBetStrategy(BigDecimal minBet) {
        this.minBet = minBet;
    }

    @Override
    public BigDecimal getMinBetAmount(BettingRoundContext bettingRoundContext, PokerPlayer player) {
        return player.getBalance().min(minBet);
    }

    @Override
    public BigDecimal getMaxBetAmount(BettingRoundContext bettingRoundContext, PokerPlayer player) {
        return player.getBalance();
    }

    @Override
    public BetStrategyType getType() {
        return BetStrategyType.NO_LIMIT;
    }

    @Override
    public BigDecimal getMinRaiseToAmount(BettingRoundContext context, PokerPlayer player) {
        if (context.allOtherNonFoldedPlayersAreAllIn(player) || !canAffordRaise(context, player)) {
            return BigDecimal.ZERO;
        }

        BigDecimal raiseTo = getNextValidRaiseToLevel(context);

        BigDecimal cost = raiseTo.subtract(player.getBetStack());
        if (cost.compareTo(BigDecimal.ZERO) < 0) {
            // Sanity check that current high bet is not lower than this player's current bet.
            throw new IllegalStateException(String.format("Current high bet (%s) is lower than player's bet stack (%s). MaxRaise(%s) Balance(%s)",
                                                          context.getHighestCompleteBet(), player.getBetStack(), raiseTo, player.getBalance()));
        }
        BigDecimal affordableCost = player.getBalance().min(cost);
        return player.getBetStack().add(affordableCost);
    }

    @Override
    public BigDecimal getMaxRaiseToAmount(BettingRoundContext bettingRoundContext, PokerPlayer player) {
        if (bettingRoundContext.allOtherNonFoldedPlayersAreAllIn(player) || !canAffordRaise(bettingRoundContext, player)) {
            return BigDecimal.ZERO;
        }
        return player.getBetStack().add(player.getBalance());
    }

    @Override
    public BigDecimal getCallAmount(BettingRoundContext bettingRoundContext, PokerPlayer player) {
        BigDecimal diff = bettingRoundContext.getHighestBet().subtract(player.getBetStack());
        if (diff.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return player.getBalance().min(diff);
    }

    @Override
    public BigDecimal getNextValidRaiseToLevel(BettingRoundContext context) {
        if (context.getHighestCompleteBet().compareTo(BigDecimal.ZERO)==0) return context.getHighestBet().add(minBet);
        return context.getHighestBet().add(context.getSizeOfLastCompleteBetOrRaise());
    }

    @Override
    public boolean isCompleteBetOrRaise(BettingRoundContext context, BigDecimal amountRaisedOrBetTo) {
        return amountRaisedOrBetTo.compareTo(getNextValidRaiseToLevel(context)) >= 0;
    }

    @Override
    public boolean shouldBettingBeCapped(int betsAndRaises, boolean headsUp) {
        return false;
    }

    private boolean canAffordRaise(BettingRoundContext context, PokerPlayer player) {
        return player.getBalance().compareTo(getCallAmount(context, player)) > 0;
    }

}
