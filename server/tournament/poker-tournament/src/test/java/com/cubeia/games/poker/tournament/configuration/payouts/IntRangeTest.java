/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.games.poker.tournament.configuration.payouts;

import org.junit.Test;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IntRangeTest {

    @Test
    public void testSorting() {
        SortedSet<IntRange> set = new TreeSet<IntRange>();
        IntRange first = new IntRange(2, 2);
        IntRange second = new IntRange(3, 7);

        set.add(second);
        set.add(first);

        Iterator<IntRange> iterator = set.iterator();
        assertThat(iterator.next(), is(first));
        assertThat(iterator.next(), is(second));
    }
}
