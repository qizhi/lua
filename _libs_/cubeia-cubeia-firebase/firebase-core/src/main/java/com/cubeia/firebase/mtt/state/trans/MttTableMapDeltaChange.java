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
package com.cubeia.firebase.mtt.state.trans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.game.DeltaChange;
import com.cubeia.util.Lists;

@Deprecated
public class MttTableMapDeltaChange implements MttTableMap, DeltaChange {

	private final VersionedStateData data;
	private Map<Integer, MttTable> map;
	
	MttTableMapDeltaChange(VersionedStateData data) {
		this.data = data;
		initMap();
	}

	public void addTable(MttTable tab) {
		Arguments.notNull(tab, "table");
		map.put(tab.getId(), tab);
	}
	
	public MttTable[] getTables() {
		return Lists.toArray(map.values(), MttTable.class);
	}

	public void clear() {
		map.clear();
	}

	public void removeTable(MttTable tab) {
		Arguments.notNull(tab, "table");
		map.remove(tab.getId());
	}

	public void commit() {
		data.setTables(getMapIds());
	}

	
	// --- PRIVATE METHODS --- //

	private int[] getMapIds() {
		int i = 0;
		Collection<Integer> c = map.keySet();
		int[] arr = new int[map.size()];
		for (Integer id : c) {
			arr[i++] = id.intValue();
		}
		return arr;
	}
	
	private void initMap() {
		 map = new HashMap<Integer, MttTable>();
		 int[] arr = data.getTables();
		 if(arr != null) {
			 for (int id : arr) {
				 map.put(id, new MttTable(id));
			 }
		}	 
	}
}
