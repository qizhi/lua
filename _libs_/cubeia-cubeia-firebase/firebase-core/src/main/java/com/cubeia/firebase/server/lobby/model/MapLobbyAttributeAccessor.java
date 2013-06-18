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
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.cubeia.firebase.api.common.Attribute;
import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.game.lobby.LobbyTableAttributeAccessor;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;

public class MapLobbyAttributeAccessor implements LobbyTableAttributeAccessor {

	private final LobbyPath path;
	private final Map<String, AttributeValue> values;
	
	public MapLobbyAttributeAccessor(LobbyPath path) {
		Arguments.notNull(path, "path");
		values = new TreeMap<String, AttributeValue>();
		this.path = path;
	}
	
	public MapLobbyAttributeAccessor(LobbyPath path, Map<String, AttributeValue> initValues) {
		Arguments.notNull(path, "path");
		Arguments.notNull(initValues, "initValues");
		values = new TreeMap<String, AttributeValue>(initValues);
		this.path = path;
	}
	
	public MapLobbyAttributeAccessor(LobbyPath path, Attribute[] attributes) {
		Arguments.notNull(path, "path");
		Arguments.notNull(attributes, "attributes");
		values = new TreeMap<String, AttributeValue>();
		for (Attribute a : attributes) {
			values.put(a.name, a.value);
		}
		this.path = path;
	}
	

	/**
	 * Flush all attributes to the system state.
	 */
	public void flush(SystemStateServiceContract state) {
		Arguments.notNull(state, "state");
		Map<String, Object> map = unwrap();
		String fqn = path.getSystemPath();
		state.setAttributes(fqn, map);
	}
	
	/**
	 * @return All attributes, but as raw objects (ie. AttributeValue objects)
	 */
	public Map<String, Object> unwrap() {
		Map<String, Object> map = new TreeMap<String, Object>();
		for (Entry<String, AttributeValue> e : values.entrySet()) {
			map.put(e.getKey(), e.getValue().data);
		}
		return map;
	}

	public LobbyPath getLobbyPath() {
		return path;
	}

	public Map<String, AttributeValue> getDirectAccessAttributes() {
		return values;
	}
	
	public Map<String, AttributeValue> getAllAttributes() {
		return new TreeMap<String, AttributeValue>(values);
	}

	public AttributeValue getAttribute(String attribute) {
		Arguments.notNull(attribute, "attribute");
		return values.get(attribute);
	}

	public Date getDateAttribute(String attribute) throws ClassCastException {
		Arguments.notNull(attribute, "attribute");
		AttributeValue att = getAttribute(attribute);
		if(att == null) return null;
		if(!(att.data instanceof Date)) throw new ClassCastException("Attribute of path '" + path + "' with name '" + attribute + "' is not a Date object");
		return (Date)att.data;
	}

	public int getIntAttribute(String attribute) throws ClassCastException {
		Arguments.notNull(attribute, "attribute");
		AttributeValue att = getAttribute(attribute);
		if(att == null) return -1;
		if(!(att.data instanceof Integer)) throw new ClassCastException("Attribute of path '" + path + "' with name '" + attribute + "' is not an Integer object");
		return ((Integer)att.data).intValue();
	}

	public String getStringAttribute(String attribute) throws ClassCastException {
		Arguments.notNull(attribute, "attribute");
		AttributeValue att = getAttribute(attribute);
		if(att == null) return null;
		if(!(att.data instanceof String)) throw new ClassCastException("Attribute of path '" + path + "' with name '" + attribute + "' is not a String object");
		return (String)att.data;
	}

	public void removeAttribute(String attribute) {
		Arguments.notNull(attribute, "attribute");
		values.remove(attribute);
	}

	public void setAttribute(String attribute, AttributeValue value) {
		checkArgs(attribute, value);
		values.put(attribute, value);
	}

	public void setDateAttribute(String attribute, Date value) {
		checkArgs(attribute, value);
		values.put(attribute, new AttributeValue(value));
	}

	public void setIntAttribute(String attribute, int value) {
		checkArgs(attribute, value);
		values.put(attribute, new AttributeValue(new Integer(value)));
	}

	public void setStringAttribute(String attribute, String value) {
		checkArgs(attribute, value);
		values.put(attribute, new AttributeValue(value));
	}
	
	public void setAttributes(Map<String, AttributeValue> attributes) {
		Arguments.notNull(attributes, "attributes");
		values.putAll(attributes);
	}
	
	// --- PRIVATE METHODS --- //
	
	private void checkArgs(String attribute, Object value) {
		Arguments.notNull(attribute, "attribute");
		Arguments.notNull(value, "value");
	}

	
}
