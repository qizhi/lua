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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.util.Arguments;


public class ConfigDelta {

	public static ConfigDelta calculate(Map<PropertyKey, String> oldConf, Map<PropertyKey, String> newConf) {
		Arguments.notNull(oldConf, "old config");
		Arguments.notNull(newConf, "new config");
		ConfigDelta delta = new ConfigDelta();
		populateRemoved(oldConf, newConf, delta);
		populateAdded(oldConf, newConf, delta);
		populateModded(oldConf, newConf, delta);
		return delta;
	}
	
	private static void populateModded(Map<PropertyKey, String> oldConf, Map<PropertyKey, String> newConf, ConfigDelta delta) {
		for (Entry<PropertyKey, String> e : oldConf.entrySet()) {
			if(newConf.containsKey(e.getKey())) {
				String test = newConf.get(e.getKey());
				if(!isEqual(test, e.getValue())) {
					delta.modded.add(new ConfigProperty(e.getKey(), test));
				}
			}
		}
	}

	private static boolean isEqual(String one, String two) {
		if(one == null && two == null) return true;
		else if(one != null) return one.equals(two);
		else return two.equals(one);
	}

	private static void populateAdded(Map<PropertyKey, String> oldConf, Map<PropertyKey, String> newConf, ConfigDelta delta) {
		for (Entry<PropertyKey, String> e : newConf.entrySet()) {
			if(!oldConf.containsKey(e.getKey())) {
				delta.added.add(new ConfigProperty(e.getKey(), e.getValue()));
			}
		}
	}

	private static void populateRemoved(Map<PropertyKey, String> oldConf, Map<PropertyKey, String> newConf, ConfigDelta delta) {
		for (Entry<PropertyKey, String> e : oldConf.entrySet()) {
			if(!newConf.containsKey(e.getKey())) {
				delta.removed.add(new ConfigProperty(e.getKey(), e.getValue()));
			}
		}
	}


	private final List<ConfigProperty> removed;
	private final List<ConfigProperty> added;
	private final List<ConfigProperty> modded;
	
	private ConfigDelta() {
		removed = new LinkedList<ConfigProperty>();
		added = new LinkedList<ConfigProperty>();
		modded = new LinkedList<ConfigProperty>();
	}
	
	public boolean isEmpty() {
		return (removed.size() == 0 && added.size() == 0 && modded.size() == 0);
	}
	
	public ConfigProperty[] getRemovedProperties() {
		return removed.toArray(new ConfigProperty[removed.size()]);
	}
	
	public ConfigProperty[] getAddedProperties() {
		return added.toArray(new ConfigProperty[added.size()]);
	}
	
	public ConfigProperty[] getModifiedProperties() {
		return modded.toArray(new ConfigProperty[modded.size()]);
	}
}
