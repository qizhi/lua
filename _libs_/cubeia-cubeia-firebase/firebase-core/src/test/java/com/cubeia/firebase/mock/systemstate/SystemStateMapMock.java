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
package com.cubeia.firebase.mock.systemstate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;

public class SystemStateMapMock implements SystemStateServiceContract {

	private Map<String, Object> data = new HashMap<String, Object>();
	
	public boolean exists(String fqn) {
		throw new UnsupportedOperationException("not yet implemented in this mock, you do it!");
	}

	public Object getAttribute(String fqn, String attribute) {
		throw new UnsupportedOperationException("not yet implemented in this mock, you do it!");
	}

	public Map<Object, Object> getAttributes(String fqn) {
		Map<Object, Object> attributes = new HashMap<Object, Object>();
		
		for (Map.Entry<String, Object> e : data.entrySet()) {
			if (e.getKey().startsWith(fqn)) {
				String attributeKey = e.getKey().split(":")[1];
				attributes.put(attributeKey, e.getValue());
			}
		}
		
		return attributes;
	}

	public SystemStateCacheHandler getCacheHandler() {
		throw new UnsupportedOperationException("not yet implemented in this mock, you do it!");
	}

	public Set<String> getChildren(String fqn) {
		HashSet<String> children = new HashSet<String>();

		for (String key : data.keySet()) {
			if (key.startsWith(fqn)) {
				key = key.substring(fqn.length());
				key = key.split("/")[0];
				children.add(key);
			}
		}
		
		return children;
	}

	public Set<String> getEndNodes(String address) {
		throw new UnsupportedOperationException("not yet implemented in this mock, you do it!");
	}

	public boolean hasNode(String fqn) {
		throw new UnsupportedOperationException("not yet implemented in this mock, you do it!");
	}

	public void printAllData() {
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			System.out.println("\t"+entry);
		}
	}

	public void removeAttribute(String fqn, String attr) {
		throw new UnsupportedOperationException("not yet implemented in this mock, you do it!");
	}

	public void removeNode(String fqn) {
		throw new UnsupportedOperationException("not yet implemented in this mock, you do it!");
	}

	public void setAttribute(String fqn, String attribute, Object value) {
		setAttribute(fqn, attribute, value, false);
	}
	
	public void setAttribute(String fqn, String attribute, Object value, boolean doAsynch) {
		data.put(fqn+":"+attribute, value);
	}

	public void setAttributes(String fqn, Map<String, Object> attributes) {
		setAttributes(fqn, attributes, false);
	}
	
	public void setAttributes(String fqn, Map<String, Object> attributes, boolean doAsynch) {
		for (String key : attributes.keySet()) {
			setAttribute(fqn, key, attributes.get(key));
		}
	}

	public void addNode(String string) {
		
	}

}
