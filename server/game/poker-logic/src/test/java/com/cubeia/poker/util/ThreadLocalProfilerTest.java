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

package com.cubeia.poker.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;


public class ThreadLocalProfilerTest {

    @Test
    public void testProfiler() {
        ThreadLocalProfiler.start();
        Tester tester = new Tester();
        tester.a();
        tester.b();
        ThreadLocalProfiler.stop();

        Map<String, Long> map = ThreadLocalProfiler.get();
        Assert.assertEquals(4, map.size());
        Assert.assertNotNull(map.get("start"));
        Assert.assertNotNull(map.get("a"));
        Assert.assertNotNull(map.get("b"));

        ThreadLocalProfiler.clear();
        Assert.assertNull(ThreadLocalProfiler.get());
    }


    private static class Tester {

        public void a() {
            ThreadLocalProfiler.add("a");
        }

        public void b() {
            ThreadLocalProfiler.add("b");
        }
    }
}


