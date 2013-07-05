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

package com.cubeia.poker.hand;

import com.cubeia.poker.handhistory.api.BestHandType;

public enum HandType {
    NOT_RANKED(0),
    HIGH_CARD(1),
    PAIR(2),
    TWO_PAIRS(3),
    THREE_OF_A_KIND(4),
    STRAIGHT(5),
    FLUSH(7),
    FULL_HOUSE(6),
    FOUR_OF_A_KIND(8),
    STRAIGHT_FLUSH(9),
    ROYAL_STRAIGHT_FLUSH(10);

    public final int telesinaHandTypeValue;

    private HandType(int telesinaHandTypeValue) {
        this.telesinaHandTypeValue = telesinaHandTypeValue;
    }

    public BestHandType translate() {
        return BestHandType.values()[ordinal()];
    }
}
