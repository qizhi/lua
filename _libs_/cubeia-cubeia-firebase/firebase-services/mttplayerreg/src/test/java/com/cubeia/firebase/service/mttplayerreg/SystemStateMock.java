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
package com.cubeia.firebase.service.mttplayerreg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;

public class SystemStateMock implements SystemStateServiceContract {

	private final Map<String, Map<Object, Object>> atts = new TreeMap<String, Map<Object,Object>>();
	
	public void addNode(String fqn) { 
		Map<Object, Object> map = new HashMap<Object, Object>();
		atts.put(fqn, map);
	}

	public boolean exists(String fqn) {
		return hasNode(fqn);
	}

	public Object getAttribute(String fqn, String attribute) {
		Map<Object, Object> map = atts.get(fqn);
		return (map == null ? null : map.get(attribute));
	}

	public Map<Object, Object> getAttributes(String fqn) {
		return atts.get(fqn);
	}

	public SystemStateCacheHandler getCacheHandler() {
		return null;
	}

	public Set<String> getChildren(String fqn) {
		return null;
	}

	public Set<String> getEndNodes(String address) {
		return null;
	}

	public boolean hasNode(String fqn) {
		return atts.containsKey(fqn);
	}

	public void printAllData() { }

	public void removeAttribute(String fqn, String attr) {
		Map<Object, Object> map = atts.get(fqn);
		if(map != null) {
			map.remove(attr);
		}
	}

	public void removeNode(String fqn) {
		atts.remove(fqn);
	}

	public void setAttribute(String fqn, String attribute, Object value) {
		Map<Object, Object> map = atts.get(fqn);
		if(map == null) {
			map = new HashMap<Object, Object>();
			atts.put(fqn, map);
		}
		map.put(attribute, value);
	}
	
	public void setAttribute(String fqn, String attribute, Object value,
			boolean doAsynch) {
		// TODO Auto-generated method stub
		
	}
	
	public void setAttributes(String fqn, Map<String, Object> attributes,
			boolean doAsynch) {
		// TODO Auto-generated method stub
		
	}

	public void setAttributes(String fqn, Map<String, Object> attributes) { }

}
