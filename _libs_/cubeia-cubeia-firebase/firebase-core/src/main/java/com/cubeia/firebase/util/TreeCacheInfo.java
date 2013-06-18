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
package com.cubeia.firebase.util;

import java.util.List;
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.util.CachePrinter;
import org.jgroups.Address;

import com.cubeia.firebase.api.util.Arguments;

public class TreeCacheInfo implements TreeCacheInfoMBean {

    private Cache<?, ?> cache;
	
	public TreeCacheInfo(Cache<?, ?> cache) {
		Arguments.notNull(cache, "cache");
		this.cache = cache;
	}
	
	public String getLocalAddress() {
		return cache.getLocalAddress().toString();
	}
	
	public String printJGroupsConfig() {
		return cache.getConfiguration().getClusterConfig();
	}
	
	public String printCacheContentDetails() {
		return CachePrinter.printCacheDetails(cache);
	}
	
	public String printCacheLockingInfo() {
        return CachePrinter.printCacheLockingInfo(cache);
    }
	
	public String[] getMembers() {
		int count = 0;
		List<Address> members = cache.getMembers(); // UNCHECKED !!
		String[] arr = new String[members.size()];
		for (Address a : members) {
			arr[count++] = a.toString();
		}
		return arr;
	}

	public int getObjectCount() {
		Set<?> childrenNames = cache.getRoot().getChildrenNames();
		return childrenNames.size();
	}
}
