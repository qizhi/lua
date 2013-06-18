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

package com.cubeia.poker.pot;

import com.cubeia.poker.pot.Pot;
import com.cubeia.poker.pot.RakeInfoContainer;

import java.io.Serializable;
import java.util.Collection;

public interface RakeCalculator extends Serializable {

    /**
     * Calculates the rakes for the given pots.
     *
     * @param pots                 Pots to calculate rake for. This should be all pots in the current hand.
     * @param firstCallHasBeenMade set to true if the hand has seen a call, false if no one has called yet
     * @return the calculated rakes per pot, total rake and total bets (pot sizes)
     */
    RakeInfoContainer calculateRakes(Collection<Pot> pots, boolean firstCallHasBeenMade);


}