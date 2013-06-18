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
package com.cubeia.firebase.server.lobby.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public abstract class LobbyAccessorBase {

	private final SystemStateServiceContract state;
	
	protected LobbyAccessorBase(SystemStateServiceContract state) {
		Arguments.notNull(state, "state");
		this.state = state;
	}

	protected void doClearNode(LobbyPath path) {
		Arguments.notNull(path, "path");
		state.removeNode(path.getSystemPath());
	}

	protected Map<String, AttributeValue> doGetAllAttributes(LobbyPath path) {
		Arguments.notNull(path, "path");
		Map<Object, Object> atts = state.getAttributes(path.getSystemPath());
		Map<String, AttributeValue> copy = new HashMap<String, AttributeValue>(atts.size());
		for(Object key : atts.keySet()) {
			Object o = atts.get(key);
			AttributeValue val = toValue(o);
			if(val != null) {
				copy.put(key.toString(), val);
			}
		}
		return copy;
	}

	protected AttributeValue doGetAttribute(LobbyPath path, String attribute) {
		checkArgs(path, attribute);
		Object att = state.getAttribute(path.getSystemPath(), attribute);
		return toValue(att);
	}

	protected Date doGetDateAttribute(LobbyPath path, String attribute) {
		checkArgs(path, attribute);
		Object att = state.getAttribute(path.getSystemPath(), attribute);
		if(att == null) return null; // Ticket # 333
		else if(!(att instanceof Date)) throw new ClassCastException("Attribute of path '" + path + "' with name '" + attribute + "' is not a Date object");
		return (Date)att;
	}

	protected int doGetIntAttribute(LobbyPath path, String attribute) {
		checkArgs(path, attribute);
		Object att = state.getAttribute(path.getSystemPath(), attribute);
		if(att == null) return -1; // Ticket # 333
		else if(!(att instanceof Integer)) throw new ClassCastException("Attribute of path '" + path + "' with name '" + attribute + "' is not an int");
		return (Integer)att;
	}

	protected String doGetStringAttribute(LobbyPath path, String attribute) {
		checkArgs(path, attribute);
		Object att = state.getAttribute(path.getSystemPath(), attribute);
		if(att == null) return null; // Ticket # 333
		else if(!(att instanceof String)) throw new ClassCastException("Attribute of path '" + path + "' with name '" + attribute + "' is not a String object");
		return (String)att;
	}

	protected boolean doHasNode(LobbyPath path) {
		Arguments.notNull(path, "path");
		return state.hasNode(path.getSystemPath());
	}

	protected void doRemoveAttribute(LobbyPath path, String attribute) {
		checkArgs(path, attribute);
		state.removeAttribute(path.getSystemPath(), attribute);
	}

	protected void doRemoveNode(LobbyPath path) {
		Arguments.notNull(path, "path");
		state.removeNode(path.getSystemPath());
	}

	protected void doSetAttribute(LobbyPath path, String attribute, AttributeValue value) {
		checkArgs(path, attribute);
		state.setAttribute(path.getSystemPath(), attribute, value.data);
	}

	protected void doSetDateAttribute(LobbyPath path, String attribute, Date value) {
		checkArgs(path, attribute);
		state.setAttribute(path.getSystemPath(), attribute, value);
	}

	protected void doSetIntAttribute(LobbyPath path, String attribute, int value) {
		checkArgs(path, attribute);
		state.setAttribute(path.getSystemPath(), attribute, new Integer(value));
	}

	protected void doSetStringAttribute(LobbyPath path, String attribute, String value) {
		checkArgs(path, attribute);
		state.setAttribute(path.getSystemPath(), attribute, value);
	}
	
	/**
	 * Batched attribute update (recommended for performance)
	 * 
	 * @param o
	 * @return
	 */
	protected void doSetAttributes(LobbyPath path, Map<String, AttributeValue> attributes) {
		checkPath(path);
		Arguments.notNull(attributes, "attributes");
		
		if (attributes.size() == 1) {
			// If only one attribute we can update that only which will 
			// make things a little easier on the waiting list and lobby
			for (Entry<String, AttributeValue> entry : attributes.entrySet()) {
				doSetAttribute(path,entry.getKey(), entry.getValue());
			}
			
		} else {
		
			Map<String, Object> values = new TreeMap<String, Object>();
			for (Entry<String, AttributeValue> entry : attributes.entrySet()) {
				values.put(entry.getKey(), entry.getValue().data);
			}
			state.setAttributes(path.getSystemPath(), values);
		}
	}

	
	
	// --- PROTECTED METHODS --- //
	
	protected AttributeValue toValue(Object o) {
		if(o instanceof Integer) return new AttributeValue(((Integer)o).intValue());
		else if(o instanceof Date) return new AttributeValue((Date)o);
		else if(o != null) return new AttributeValue(o.toString());
		else return null;
	}
	
	protected void checkArgs(LobbyPath path, String attribute) {
		checkPath(path);
		Arguments.notNull(attribute, "attribute");
	}
	
	protected void checkPath(LobbyPath path) {
		Arguments.notNull(path, "path");
		Arguments.notNull(path.getDomain(), "path.domain");
	}

}
