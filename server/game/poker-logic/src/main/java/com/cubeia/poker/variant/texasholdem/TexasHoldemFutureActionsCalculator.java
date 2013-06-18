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

package com.cubeia.poker.variant.texasholdem;

import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.betting.BetStrategyType;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.betting.FutureActionsCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TexasHoldemFutureActionsCalculator implements FutureActionsCalculator {

    private final BetStrategyType betStrategyType;

    public TexasHoldemFutureActionsCalculator(BetStrategyType betStrategyType) {
        this.betStrategyType = betStrategyType;
    }

    private static final long serialVersionUID = 6513501780238216186L;

    @Override
    public List<PokerActionType> calculateFutureActionOptionList(PokerPlayer player, BigDecimal highestBet, boolean bettingCapped) {
        List<PokerActionType> options = new ArrayList<PokerActionType>();

        // Players who are all in or have folded do not have any future actions.
        if (player.hasFolded() || player.isAllIn() || player.isSittingOut() || player.hasActed() || player.isAway()) {
            return options;
        }

        if (player.getBetStack().compareTo(highestBet) >= 0 && !player.hasActed()) {
            options.add(PokerActionType.CHECK);
        }

        if (player.getBetStack().compareTo(highestBet) < 0) {
            options.add(PokerActionType.CALL);
        }

        if (betStrategyType == BetStrategyType.FIXED_LIMIT && !bettingCapped) {
            options.add(PokerActionType.RAISE);
        }

        options.add(PokerActionType.FOLD);


        return options;
    }
}
