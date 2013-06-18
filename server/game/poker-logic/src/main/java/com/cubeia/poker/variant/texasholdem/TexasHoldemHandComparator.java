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

package com.cubeia.poker.variant.texasholdem;

import java.io.Serializable;
import java.util.Comparator;

import com.cubeia.poker.hand.Hand;
import com.cubeia.poker.hand.HandStrength;
import com.cubeia.poker.hand.HandStrengthComparator;

/**
 * Compares hands. The most valued hand is greater than a lesser one.
 * This implementation delegates to {@link HandStrengthComparator}.
 * <p/>
 * NOTE this impl sorts hands in the "wrong" order according to
 * behavior specified by Comparator interface. In some parts of the
 * code base Collections.reverseOrder must be used
 *
 * @author w
 */
public class TexasHoldemHandComparator implements Comparator<Hand>, Serializable {

    private HandStrengthComparator hsc;
    private TexasHoldemHandCalculator calc = new TexasHoldemHandCalculator();


    public TexasHoldemHandComparator() {
        hsc = new HandStrengthComparator();
    }

    @Override
    public int compare(Hand h1, Hand h2) {
        HandStrength h1Strength = calc.getBestCombinationHandStrength(h1, 5);
        HandStrength h2Strength = calc.getBestCombinationHandStrength(h2, 5);
        return hsc.compare(h1Strength, h2Strength);
    }


}
