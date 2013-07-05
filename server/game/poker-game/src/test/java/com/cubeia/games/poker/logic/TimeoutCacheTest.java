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

package com.cubeia.games.poker.logic;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TimeoutCacheTest {


    @Test
    public void testBasicUsage() throws Exception {
        TimeoutCache cache = new TimeoutCache();
        UUID u1 = UUID.randomUUID();
        cache.addTimeout(1, 2, u1);
        assertEquals(1, cache.actions.size());

        cache.removeTimeout(1, 2, null);
        assertEquals(0, cache.actions.size());
    }

}
