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
package com.cubeia.firebase.server.conf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.util.Arguments;

public class MapConfiguration implements Configuration {
	
	/*
	 * Keys must be in "namespace":"prop" format.
	 */
	
	public static Map<PropertyKey, String> convert(Map<String, String> map) {
		if(map == null || map.size() == 0) return new ConcurrentHashMap<PropertyKey, String>();
		else {
			Map<PropertyKey, String> props = new ConcurrentHashMap<PropertyKey, String>(map.size());
			for (String s : map.keySet()) {
				int i = s.lastIndexOf(':');
				if(i == -1) props.put(new PropertyKey(Namespace.NULL, s), map.get(s));
				else {
					String prop = s.substring(i + 1);
					String ns = s.substring(0, i);
					props.put(new PropertyKey(new Namespace(ns), prop), map.get(s));
				}
			}
			return props;
		}
	}
	
	/*
	 * Keys must be in "namespace"."prop" format.
	 */
	public static Map<PropertyKey, String> convert(Properties props) {
		if(props == null || props.size() == 0) return new ConcurrentHashMap<PropertyKey, String>();
		else {
			// props = (Properties)props.clone();
			Map<PropertyKey, String> next = new ConcurrentHashMap<PropertyKey, String>(props.size());
			for (Object o : props.keySet()) {
				String s = o.toString();
				int i = s.lastIndexOf('.');
				String val = props.getProperty(s);
				if(i == -1) next.put(new PropertyKey(Namespace.NULL, s), val);
				else {
					String prop = s.substring(i + 1);
					String ns = s.substring(0, i);
					next.put(new PropertyKey(new Namespace(ns), prop), val);
				}
			}
			return next;
		}
	}
	
	
	/// --- INSTANCE MEMBERS --- ///

	protected final Map<PropertyKey, String> props;
	
	public MapConfiguration() {
		this((Map<PropertyKey, String>)null);
	}
	
	/**
	 * @param p Properties in 'namespace.prop' format
	 */
	public MapConfiguration(Properties p) {
		this(convert(p));
	}
	
	public MapConfiguration(Map<PropertyKey, String> map) {
		if(map == null) props = new ConcurrentHashMap<PropertyKey, String>();
		else props = new ConcurrentHashMap<PropertyKey, String>(map);
	}
	
	public int size() {
		return props.size();
	}
	
	public PropertyKey[] getKeys() {
		return props.keySet().toArray(new PropertyKey[props.size()]);
	}
	
	public void setProperty(PropertyKey key, String val) {
		Arguments.notNull(key, "key");
		Arguments.notNull(val, "value");
		props.put(key, val);
	}
	
	public void removeProperty(PropertyKey key) {
		Arguments.notNull(key, "key");
		props.remove(key);
	}
 
	public String[] getProperties(Namespace ns) {
		Arguments.notNull(ns, "namespace");
		List<String> tmp = new LinkedList<String>();
		for(PropertyKey key : props.keySet()) {
			if(key.getNamespace().equals(ns)) {
				tmp.add(key.getProperty());
			}
		}
		return tmp.toArray(new String[tmp.size()]);
	}

	public String getProperty(Namespace ns, String prop) {
		return getProperty(new PropertyKey(ns, prop));
	}
	
	public String getProperty(PropertyKey key) {
		Arguments.notNull(key, "key");
		return props.get(key);
	}

	public boolean hasProperty(Namespace ns, String prop) {
		return props.containsKey(new PropertyKey(ns, prop));
	}

	public Map<PropertyKey, String> cloneProperties() {
		return new HashMap<PropertyKey, String>(props);
	}
}
