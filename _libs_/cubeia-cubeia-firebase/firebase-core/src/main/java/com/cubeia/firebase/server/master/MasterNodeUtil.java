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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.service.messagebus.Partition;

public class MasterNodeUtil {

	private MasterNodeUtil() { }
	
	public static Map<Partition, List<Integer>> calculateNewDistribution(Map<Partition, Integer> sizes, int[] ids) {
		Map<Partition, List<Integer>> redist = new HashMap<Partition, List<Integer>>();
		for (int id : ids) {
			Partition p = findSmallestPartition(sizes);
			Integer size = sizes.get(p);
			sizes.put(p, Integer.valueOf(size.intValue() + 1));
			List<Integer> list = redist.get(p);
			if(list == null) {
				list = new LinkedList<Integer>();
				redist.put(p, list);
			}
			list.add(id);
		}
		return redist;
	}
	
	private static Partition findSmallestPartition(Map<Partition, Integer> sizes) {
		Partition small = null;
		int smallest = -1;
		for (Partition p : sizes.keySet()) {
			Integer size = sizes.get(p);
			if(small == null || smallest > size.intValue()) {
				smallest = size.intValue();
				small = p;
			}
		}
		return small;
	}
}
