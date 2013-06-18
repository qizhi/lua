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

package com.cubeia.poker.blinds;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for providing a random seatId.
 * <p/>
 * Used when moving the dealer button to a random seat.
 */
public interface RandomSeatProvider extends Serializable {

    /**
     * Returns a random seat id of the given seat ids.
     *
     * @param availableSeatIds the list of available seat ids
     * @return A random seat id of the given seat ids. The result must be one of the values in the given list
     */
    public int getRandomSeatId(List<Integer> availableSeatIds);
}
