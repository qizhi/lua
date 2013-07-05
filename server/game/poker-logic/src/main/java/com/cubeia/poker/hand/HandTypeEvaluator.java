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

import java.util.Comparator;

public interface HandTypeEvaluator {

    /**
     * Return the best possible hand given context constraints possible
     * Valid inputs may have any number of cards on hand, eg none, 5 or 7 cards.
     *
     * @param hand the Hand to evaluate
     * @return Info holding details on the best hand that can be assembled using
     *         the given cards.
     */
    public HandInfo getBestHandInfo(Hand hand);

    Comparator<Hand> createHandComparator(int playersInPot);


}
