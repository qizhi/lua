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

package com.cubeia.poker.util;

import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.model.PlayerHand;

import java.util.Comparator;

public class PlayerHandComparator implements Comparator<PlayerHand> {

    private Comparator<Hand> comparator;

    public PlayerHandComparator(Comparator<Hand> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(PlayerHand ph1, PlayerHand ph2) {
        return comparator.compare(ph1.getHand(), ph2.getHand());
    }

}
