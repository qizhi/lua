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

package com.cubeia.games.poker.activator;

import com.cubeia.firebase.api.game.table.Table;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;

public class MapTableNameManagerTest {

    @Test
    public void testAddAndRemove() {
        TableNameManager man = new MapTableNameManager("test_table_names.txt", false);
        // test 3 first
        assertEquals("A", man.tableCreated(table(1)));
        assertEquals("B", man.tableCreated(table(2)));
        assertEquals("C", man.tableCreated(table(3)));
        // new 3 more
        assertEquals("A 2", man.tableCreated(table(4)));
        assertEquals("B 2", man.tableCreated(table(5)));
        assertEquals("C 2", man.tableCreated(table(6)));
        // remove and readd
        man.tableDestroyed(5); // B 2
        assertEquals("B 2", man.tableCreated(table(7)));
    }

    @Test
    public void testFindDefaults() {
        new MapTableNameManager();
    }


    // --- PRIVATE METHODS --- //

    private Table table(int tableId) {
        Table t = Mockito.mock(Table.class);
        Mockito.when(t.getId()).thenReturn(tableId);
        return t;
    }
}
