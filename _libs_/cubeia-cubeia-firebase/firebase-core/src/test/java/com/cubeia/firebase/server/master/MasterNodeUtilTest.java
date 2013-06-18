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
package com.cubeia.firebase.server.master;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.server.master.MasterNodeUtil;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.Partition;

import junit.framework.TestCase;

public class MasterNodeUtilTest extends TestCase {

	public void testNewDistribution() throws Exception {
		Partition p1 = new Partition(EventType.GAME, "p1", "p1", null, null);
		Partition p3 = new Partition(EventType.GAME, "p3", "p3", null, null);
		Map<Partition, Integer> sizes = new HashMap<Partition, Integer>();
		sizes.put(p1, 5);
		sizes.put(p3, 3);
		int[] ids = { 1, 2, 3, 4 };
		Map<Partition, List<Integer>> redist = MasterNodeUtil.calculateNewDistribution(sizes, ids);
		/*
		 * (5 + 3) + ids.length = 12 ->
		 * 
		 * 8 / 2 = 6 ->
		 * 
		 * 6 - p1.length = 1 &
		 * 6 - p3.length = 3
		 */
		assertEquals(1, redist.get(p1).size());
		assertEquals(3, redist.get(p3).size());
	}
}
