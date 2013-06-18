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
package com.cubeia.firebase.service.pubsysstate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.sysstate.PublicSystemStateService;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

/**
 * This is the public state service. It depends on the internal system
 * state service and is deployed as a trusted, public service.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 29
 */
public class GameSystemStateService implements PublicSystemStateService, Service {

	private SystemStateServiceContract wrapped;

	public boolean exists(String nodePath) {
		return wrapped.exists(nodePath);
	}

	public AttributeValue getAttribute(String nodePath, String attribute) {
		checkArgs(nodePath, attribute);
		Object o = wrapped.getAttribute(nodePath, attribute);
		return AttributeValue.wrapObject(o);
	}

	public Map<String, AttributeValue> getAttributes(String nodePath) {
		Arguments.notNull(nodePath, "nodePath");
		Map<Object, Object> raw = wrapped.getAttributes(nodePath);
		Map<String, AttributeValue> vals = new HashMap<String, AttributeValue>(raw.size());
		for (Object key : raw.keySet()) {
			Object o = raw.get(key);
			AttributeValue v = AttributeValue.wrapObject(o);
			if(v != null) {
				vals.put(key.toString(), v);
			}
		}
		return vals;
	}

	public boolean remove(String nodePath) {
		Arguments.notNull(nodePath, "nodePath");
		boolean ret = wrapped.exists(nodePath);
		wrapped.removeNode(nodePath);
		return ret;
	}

	public AttributeValue removeAttribute(String nodePath, String attribute) {
		checkArgs(nodePath, attribute);
		AttributeValue v = getAttribute(nodePath, attribute);
		wrapped.removeAttribute(nodePath, attribute);
		return v;
	}

	public void setAttribute(String nodePath, String attribute, AttributeValue value) {
		checkArgs(nodePath, attribute);
		if(value == null) return; // SANITY CHECK
		wrapped.setAttribute(nodePath, attribute, value.data);
	}
	
	public Set<String> getChildren(String fqn) {
		Arguments.notNull(fqn, "fqn");
		return wrapped.getChildren(fqn);
	}	

	public void destroy() {
		wrapped = null;
	}

	public void init(ServiceContext con) throws SystemException {
		wrapped = con.getParentRegistry().getServiceInstance(SystemStateServiceContract.class);
		if(wrapped == null) throw new SystemCoreException("Failed dependencies; The public system state service requires the internal system state service.");
	}

	public void start() { }

	public void stop() { }
	
	
	// --- PRIVATE METHODS --- //
	
	private void checkArgs(String nodePath, String attribute) {
		Arguments.notNull(nodePath, "nodePath");
		Arguments.notNull(attribute, "attribute");
	}

}
