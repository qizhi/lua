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

package com.cubeia.games.poker.adapter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A sequence generator for actions. This class should be a singleton at least per table.
 * Sequence numbers are used to check the validity of responses from the client.
 * <p/>
 * This class is thread safe.
 *
 * @author w
 */
public class ActionSequenceGenerator {

    private AtomicInteger sequenceCounter = new AtomicInteger();

    public int next() {
        int seq = sequenceCounter.incrementAndGet();
        seq = resetSequenceIfWrappedAround(seq);
        return seq;
    }

    private int resetSequenceIfWrappedAround(int seq) {
        if (seq < 0) {
            // This is not thread safe in the respect that we might set
            // the counter to 0 multiple times. However, this should be
            // fine since we will not be able to this on the same table.
            seq = 0;
            sequenceCounter.set(seq);
        }
        return seq;
    }

}
