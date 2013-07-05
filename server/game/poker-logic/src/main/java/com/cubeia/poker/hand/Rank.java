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

public enum Rank {
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE;

    public GameCard.Rank translate() {
        return GameCard.Rank.values()[ordinal()];
    }

    public String toShortString() {
        switch (this) {
            case TWO:
                return "2";
            case THREE:
                return "3";
            case FOUR:
                return "4";
            case FIVE:
                return "5";
            case SIX:
                return "6";
            case SEVEN:
                return "7";
            case EIGHT:
                return "8";
            case NINE:
                return "9";
            case TEN:
                return "T";
            case JACK:
                return "J";
            case QUEEN:
                return "Q";
            case KING:
                return "K";
            case ACE:
                return "A";
            default:
                throw new IllegalArgumentException("Invalid enum value for Rank: " + this);
        }
    }

    public static Rank fromShortString(char rank) {
        switch (rank) {
            case '2':
                return TWO;
            case '3':
                return THREE;
            case '4':
                return FOUR;
            case '5':
                return FIVE;
            case '6':
                return SIX;
            case '7':
                return SEVEN;
            case '8':
                return EIGHT;
            case '9':
                return NINE;
            case 'T':
                return TEN;
            case 'J':
                return JACK;
            case 'Q':
                return QUEEN;
            case 'K':
                return KING;
            case 'A':
                return ACE;
            case 't':
                return TEN;
            case 'j':
                return JACK;
            case 'q':
                return QUEEN;
            case 'k':
                return KING;
            case 'a':
                return ACE;
            default:
                throw new IllegalArgumentException("Invalid enum value for Rank: " + rank);
        }
    }
}
