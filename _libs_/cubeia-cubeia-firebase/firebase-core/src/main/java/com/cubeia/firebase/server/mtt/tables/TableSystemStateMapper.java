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
package com.cubeia.firebase.server.mtt.tables;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public class TableSystemStateMapper {
	
	private static final String SYSSTATE_ROOT = SystemStateConstants.MTT_TABLEMAP_ROOT_FQN;
	private static final String TABLE_SET = "tables";
	
	@SuppressWarnings("unchecked")
	public static synchronized Set<Integer> getAllTables(SystemStateServiceContract sysState, int mttId) {
		return (Set<Integer>)sysState.getAttribute(SYSSTATE_ROOT + mttId, TABLE_SET);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized void handleTableRemoved(SystemStateServiceContract sysState, int mttId, int tableId) {
		Set<Integer> set = (Set<Integer>)sysState.getAttribute(SYSSTATE_ROOT + mttId, TABLE_SET);
		if(set != null) {
			set.remove(tableId);
			if(set.size() == 0) {
				removeStateTableMap(sysState, mttId);
			} else {
				updateStateTableMap(sysState, mttId, set);
			}
		}
	}

	private static void removeStateTableMap(SystemStateServiceContract sysState, int mttId) {
		String path = SYSSTATE_ROOT + mttId;
		sysState.removeAttribute(path, TABLE_SET);
		/*
		 * Trac issue #396, we need to remove the entire node if this
		 * is the last table/attribute for the mtt.
		 */
		Map<Object, Object> tmp = sysState.getAttributes(path);
		if(tmp.size() == 0) {
			sysState.removeNode(path);
		}
	}

	private static void updateStateTableMap(SystemStateServiceContract sysState, int mttId, Set<Integer> set) {
		sysState.setAttribute(SYSSTATE_ROOT + mttId, TABLE_SET, set);
	}

	@SuppressWarnings("unchecked")
	public static synchronized void handleTableAdded(SystemStateServiceContract sysState, int mttId, int tableId) {
		Set<Integer> set = (Set<Integer>)sysState.getAttribute(SYSSTATE_ROOT + mttId, TABLE_SET);
		if(set == null) {
			set = new TreeSet<Integer>();
		}
		set.add(tableId);
		updateStateTableMap(sysState, mttId, set);
	}
}
