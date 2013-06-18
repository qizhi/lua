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

import com.cubeia.poker.hand.Card;
import com.cubeia.poker.player.PokerPlayer;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;

public interface PlayerToActCalculator extends Serializable {

    /**
     * Returns the first player in the round to act.
     *
     * @param seatingMap         seating
     * @return player to act
     */
    PokerPlayer getFirstPlayerToAct(SortedMap<Integer, PokerPlayer> seatingMap, List<Card> communityCards);

    /**
     * Returns the next player in the round to act.
     *
     * @param lastActedSeatId seat of the player that acted previously
     * @param seatingMap      seating
     * @return player to act
     */
    PokerPlayer getNextPlayerToAct(int lastActedSeatId, SortedMap<Integer, PokerPlayer> seatingMap);

}
