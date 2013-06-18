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

package com.cubeia.games.poker.tournament.rebuy;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RebuySupportTest {

    private RebuySupport rebuySupport;

    @Before
    public void setup() {
        rebuySupport = new RebuySupport(true, new BigDecimal(50000), new BigDecimal(50000), 1000, new BigDecimal(200000), true, 3, BigDecimal.valueOf(100), BigDecimal.valueOf(100));
    }

    @Test
    public void testRemoveRebuyRequest() {
        rebuySupport.addRebuyRequestsForTable(1, Collections.singleton(3));
        assertThat(rebuySupport.getRebuyRequestsForTable(1).isEmpty(), is(false));
        rebuySupport.getRebuyRequestsForTable(1).remove(3);
        assertThat(rebuySupport.getRebuyRequestsForTable(1).isEmpty(), is(true));
    }
}
