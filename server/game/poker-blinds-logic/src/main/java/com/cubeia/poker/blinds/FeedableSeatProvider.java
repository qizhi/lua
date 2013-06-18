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

import java.util.List;
import java.util.Random;

/**
 * A {@link RandomSeatProvider} that can be fed with values in two ways. Either, the feedNextSeatId can be
 * used, or a mocked randomizer can be injected.
 */
public class FeedableSeatProvider implements RandomSeatProvider {

    /**
     * Used for fetching random values.
     */
    private Random randomizer;

    /**
     * The next "random" seat id to return.
     */
    int nextSeatId = -1;

    public FeedableSeatProvider() {
        this(new Random());
    }

    public FeedableSeatProvider(Random randomizer) {
        this.randomizer = randomizer;
    }


    /**
     * Feeds the next seat id to return.
     * <p/>
     * After the next call to getRandomSeatId, this will be reset.
     *
     * @param nextSeatId
     */
    public void feedNextSeatId(int nextSeatId) {
        this.nextSeatId = nextSeatId;
    }

    /**
     * Gets a "random" seat id. If we have a nextSeatId, that seat id is returned, otherwise randomizes
     * a seat id in the given collection.
     * <p/>
     * If the fed nextSeatId is not contained in the list of available seat ids, a random seat id is returned instead.
     */
    public int getRandomSeatId(List<Integer> availableSeatIds) {
        final int randomSeatId;
        if (nextSeatId != -1 && availableSeatIds.contains(nextSeatId)) {
            randomSeatId = nextSeatId;
            nextSeatId = -1;
        } else {
            final Integer seatIds[] = new Integer[availableSeatIds.size()];
            availableSeatIds.toArray(seatIds);
            final int index = randomizer.nextInt(availableSeatIds.size());
            randomSeatId = seatIds[index];
        }
        return randomSeatId;
    }

}
