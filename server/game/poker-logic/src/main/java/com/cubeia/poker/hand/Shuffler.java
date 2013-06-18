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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shuffles a list.
 * <p/>
 * This class implements the Fisher-Yates modern algorithm: http://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
 * <p/>
 * <code>
 * To shuffle an array a of n elements (indexes 0..n-1):
 * for i from n − 1 downto 1 do
 * j <- random integer with 0 <= j <= i
 * exchange a[j] and a[i]
 * </code>
 *
 * @author w
 */
public class Shuffler<T> {

    private final Random rng;

    public Shuffler(Random rng) {
        this.rng = rng;
    }

    /**
     * Returns a new shuffled copy of the given list.
     *
     * @param list list to be shuffled
     * @return shuffled list
     */
    public List<T> shuffle(List<T> list) {
        ArrayList<T> shuffledList = new ArrayList<T>(list);
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            Collections.swap(shuffledList, j, i);
        }
        return shuffledList;
    }


}
