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

import com.cubeia.poker.handhistory.api.GameCard;

/**
 * Suits.
 * The order of the enums are ascending rank according to: http://www.pagat.com/poker/rules/ranking.html#suit
 *
 * @author w
 */
public enum Suit {

    CLUBS(1),
    DIAMONDS(2),
    HEARTS(3),
    SPADES(0);

    public final int telesinaSuitValue;

    private Suit(int telesinaSuitValue) {
        this.telesinaSuitValue = telesinaSuitValue;
    }

    public GameCard.Suit translate() {
        return GameCard.Suit.values()[ordinal()];
    }

    public String toShortString() {
        return name().substring(0, 1);
    }

    public static Suit fromShortString(char suit) {
        switch (suit) {
            case 'h':
                return HEARTS;
            case 'd':
                return DIAMONDS;
            case 's':
                return SPADES;
            case 'c':
                return CLUBS;
            case 'H':
                return HEARTS;
            case 'D':
                return DIAMONDS;
            case 'S':
                return SPADES;
            case 'C':
                return CLUBS;
            default:
                throw new IllegalArgumentException("Invalid enum value for Suit: " + suit);
        }
    }

}
