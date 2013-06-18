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

package com.cubeia.poker.blinds.utils;

import com.cubeia.poker.blinds.BlindsPlayer;
import junit.framework.TestCase;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PokerUtilsTest extends TestCase {

    public void testGetElementAfter() {
        SortedMap<Integer, BlindsPlayer> map = new TreeMap<Integer, BlindsPlayer>();

        BlindsPlayer p1 = new MockPlayer(1);
        BlindsPlayer p2 = new MockPlayer(2);
        BlindsPlayer p3 = new MockPlayer(3);

        map.put(3, p3);
        map.put(1, p1);
        map.put(2, p2);

        assertEquals(p3, PokerUtils.getElementAfter(2, map));
        assertEquals(p1, PokerUtils.getElementAfter(3, map));
        assertEquals(p2, PokerUtils.getElementAfter(1, map));
    }

    public void testEqualsBug() {
        SortedMap<Integer, BlindsPlayer> map = new TreeMap<Integer, BlindsPlayer>();

        BlindsPlayer p1 = new MockPlayer(1);
        BlindsPlayer p2 = new MockPlayer(2);
        BlindsPlayer p3 = new MockPlayer(3);

        map.put(Integer.valueOf(3), p3);
        map.put(Integer.valueOf(1), p1);
        map.put(Integer.valueOf(2), p2);

        assertEquals(p1, PokerUtils.getElementAfter(Integer.valueOf(3), map));
    }

    public void testGetElementAfterNonExistingElement() {
        SortedMap<Integer, BlindsPlayer> map = new TreeMap<Integer, BlindsPlayer>();

        BlindsPlayer p1 = new MockPlayer(1);
        BlindsPlayer p3 = new MockPlayer(3);

        map.put(3, p3);
        map.put(1, p1);

        assertEquals(p3, PokerUtils.getElementAfter(2, map));
    }

    public void testGetElementAfterSpecialCases() {
        SortedMap<Integer, BlindsPlayer> map = new TreeMap<Integer, BlindsPlayer>();

        BlindsPlayer p3 = new MockPlayer(3);

        map.put(3, p3);

        assertEquals(p3, PokerUtils.getElementAfter(2, map));
        assertEquals(p3, PokerUtils.getElementAfter(3, map));
        assertEquals(p3, PokerUtils.getElementAfter(4, map));
    }

    public void testUnwrapList() {
        SortedMap<Integer, BlindsPlayer> map = new TreeMap<Integer, BlindsPlayer>();

        BlindsPlayer p1 = new MockPlayer(1);
        BlindsPlayer p2 = new MockPlayer(2);
        BlindsPlayer p3 = new MockPlayer(3);

        map.put(Integer.valueOf(3), p3);
        map.put(Integer.valueOf(1), p1);
        map.put(Integer.valueOf(2), p2);

        List<BlindsPlayer> list = PokerUtils.unwrapList(map, 3);
        assertEquals(p3, list.get(0));
        assertEquals(p1, list.get(1));
        assertEquals(p2, list.get(2));
        assertEquals(3, list.size());
    }

}
