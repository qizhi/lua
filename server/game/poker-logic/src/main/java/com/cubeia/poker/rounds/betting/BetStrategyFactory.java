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

import com.cubeia.poker.betting.BetStrategyType;

import java.math.BigDecimal;


public class BetStrategyFactory {

    public static BetStrategy createBetStrategy(BetStrategyType type, BigDecimal minBet) {
        return createBetStrategy(type, minBet, false);
    }

    public static BetStrategy createBetStrategy(BetStrategyType type, BigDecimal minBet, boolean doubleBetRound) {
        if (type == BetStrategyType.NO_LIMIT) {
            return new NoLimitBetStrategy(minBet);
        } else if (type == BetStrategyType.FIXED_LIMIT) {
            return new FixedLimitBetStrategy(minBet, doubleBetRound);
        } else if (type == BetStrategyType.POT_LIMIT) {
            return new PotLimitBetStrategy(minBet);
        }
        throw new IllegalArgumentException("No bet strategy defined for " + type);
    }
}
