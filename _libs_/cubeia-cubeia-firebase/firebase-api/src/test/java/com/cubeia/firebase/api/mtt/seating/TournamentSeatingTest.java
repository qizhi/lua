/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.api.mtt.seating;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class TournamentSeatingTest {

//    @Test
//    public void testGetTableSeating() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetTableSeatings() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testAddPlayerToTable() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetAllPlayers() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetAllTables() {
//        fail("Not yet implemented");
//    }

    @Test
    public void testRemovePlayer() {
        TournamentSeating ts = new TournamentSeating();
        
        ts.addPlayerToTable(1, 1, -1);
        ts.addPlayerToTable(2, 2, -1);
        ts.addPlayerToTable(3, 3, -1);
        ts.addPlayerToTable(4, 4, -1);
        assertEquals(4, ts.getAllPlayers().size());
        
        boolean removed = ts.removePlayer(3);
        assertTrue(removed);
        assertEquals(3, ts.getAllPlayers().size());
        assertEquals(
            new HashSet<Integer>(Arrays.asList(1, 2, 4)),
            ts.getAllPlayers());
    }

//    @Test
//    public void testRemoveTable() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetTableByPlayer() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testRemoveTables() {
//        fail("Not yet implemented");
//    }

}