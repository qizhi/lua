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

import com.cubeia.poker.action.PokerActionType;
import com.cubeia.poker.player.PokerPlayer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public interface FutureActionsCalculator extends Serializable {

    /**
     * Calculates what a player can do in the future given that the state does not change.
     * i.e. the "check next" and "fold next" check boxes.
     *
     * @param player
     * @return
     */
    public abstract List<PokerActionType> calculateFutureActionOptionList(PokerPlayer player, BigDecimal highestBet,boolean bettingCapped);
}