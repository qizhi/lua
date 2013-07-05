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

package com.cubeia.game.poker.util;

import java.util.Random;

/**
 * FIXME: This is a duplicate of the Arithmetic class in the
 * bots project. But if I depend on it I get cyclic dependency in
 * maven.
 *
 * @author Fredrik Johansson, Cubeia Ltd
 */
public class Arithmetic {

    private static Random rng = new Random();

    /**
     * Returns a gaussian average for the given mean and deviation.
     *
     * @param mean
     * @param deviation
     * @return result, may be negative
     */
    public static int gaussianAverage(int mean, int deviation) {
        float g = (float) rng.nextGaussian();
        g = g * (float) deviation;
        return mean + (int) g;
    }

}
