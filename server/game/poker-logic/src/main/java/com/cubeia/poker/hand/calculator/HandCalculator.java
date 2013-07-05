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

package com.cubeia.poker.hand.calculator;

import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandStrength;

/**
 * <p>Inspect and calculate what poker hands are implemented in a Hand.</p>
 * <p/>
 * <p>Calculates the best hand strength given a poker hand.
 * The calculator does not require 5 cards specifically, but
 * if you provide more or less it might produce unpredictive results.</p>
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public interface HandCalculator {

    /**
     * <p>Get the hand strength representation for the given hand.</p>
     *
     * @param hand, cannot be null
     * @return HandStrength, never null
     */
    public HandStrength getHandStrength(Hand hand);

}