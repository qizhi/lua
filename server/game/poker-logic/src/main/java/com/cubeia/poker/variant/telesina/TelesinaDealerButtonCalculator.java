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

import com.cubeia.poker.player.PokerPlayer;
import com.google.common.collect.Iterators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;

public class TelesinaDealerButtonCalculator implements Serializable {

    public int getNextDealerSeat(SortedMap<Integer, PokerPlayer> currentSeatingMap, int currentDealerSeatId, boolean wasHandCancelled) {

        // no players seated the dealerbutton should not move
        if (currentSeatingMap.isEmpty()) {
            return currentDealerSeatId;
        }

        // cancelled hand should not move dealerbutton
        if (wasHandCancelled) {
            return currentDealerSeatId;
        }

        // one player will make never ending loop
        if (currentSeatingMap.size() == 1) {
            return currentSeatingMap.firstKey();
        }

        ArrayList<Integer> seatList = new ArrayList<Integer>(currentSeatingMap.keySet());
        if (!seatList.contains(currentDealerSeatId)) {
            seatList.add(currentDealerSeatId);
        }
        Collections.sort(seatList);

        Iterator<Integer> seatIterator = Iterators.cycle(seatList);

        // wind ahead until
        int seat = seatIterator.next();

        while (seat < currentDealerSeatId) {
            seat = seatIterator.next();
        }
        return seatIterator.next();


    }

}
