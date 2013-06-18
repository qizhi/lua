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

import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.player.PokerPlayer;
import com.cubeia.poker.rounds.betting.FutureActionsCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// This calculator is also known as the "check or fold" calculator. TODO: Remove this stupidity.
public class TelesinaFutureActionsCalculator implements FutureActionsCalculator {


    private static final long serialVersionUID = 6922360869266062794L;

    /* (non-Javadoc)
    * @see com.cubeia.poker.variant.texasholdem.FutureActionsCalculator#calculateFutureActionOptionList(com.cubeia.poker.player.PokerPlayer, java.lang.Long)
    */
    @Override
    public List<PokerActionType> calculateFutureActionOptionList(PokerPlayer player, BigDecimal highestBet, boolean bettingCapped) {
        List<PokerActionType> options = new ArrayList<PokerActionType>();

        // players that are all in or has folded should not have anything
        if (player.hasFolded() || player.isAllIn() || player.isSittingOut()) {
            return options;
        }

        // in telesina if you have ever acted then you will never be able to check
        if (player.getBetStack().compareTo(highestBet) >= 0 && !player.hasActed()) {
            options.add(PokerActionType.CHECK);
        }

        options.add(PokerActionType.FOLD);


        return options;
    }
}
