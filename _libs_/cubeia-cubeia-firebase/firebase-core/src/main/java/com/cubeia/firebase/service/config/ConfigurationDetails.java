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
package com.cubeia.firebase.service.config;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.server.conf.PropertyKey;
import com.cubeia.firebase.api.service.config.ConfigurationProvider;

public class ConfigurationDetails implements ConfigurationDetailsMBean {
	
	private static final String[] TYPE_NAMES = new String[] { "namespace", "name", "value" };

	private final ConfigurationProvider prov;

	public ConfigurationDetails(ConfigurationProvider prov) {
		this.prov = prov;
	}
	
	public TabularData getProperties() {
		ConfigProperty[] props = prov.getAllProperties();
		try {
			TabularDataSupport t = new TabularDataSupport(getTabType());
			t.putAll(toArray(props));
			return t;
		} catch (OpenDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String getProperty(String s) {
		if(s == null) return null; // SANITY CHECK
		ConfigProperty[] props = prov.getAllProperties();
		int test = s.indexOf('.');
		return search(props, s, test != -1);
	}


	// --- PRIVATE METHODS --- //
	
	private String search(ConfigProperty[] props, String name, boolean isFullName) {
		String tmp = name;
		if(isFullName) {
			/*
			 * Substitute last '.' for a ':' in order to search the 
			 * property keys easier. /LJN
			 */
			int i = name.lastIndexOf('.');
			tmp = name.substring(0, i);
			tmp += ":";
			tmp += name.substring(i + 1, name.length());
		}
		for (ConfigProperty p : props) {
			PropertyKey key = p.getKey();
			if(isFullName && key.toString().equalsIgnoreCase(tmp)) {
				return p.getValue();
			} else if(!isFullName && key.getProperty().equalsIgnoreCase(tmp)) {
				return p.getValue();
			}
		}
		return null;
	}
	
	private CompositeData[] toArray(ConfigProperty[] props) throws OpenDataException {
		int count = 0;
		CompositeData[] arr = new CompositeData[props.length];
		for (ConfigProperty p : props) {
			arr[count++] = toData(p);
		}
		return arr;
	}

	private CompositeType getDataType() throws OpenDataException {
		return new CompositeType("ConfigProperty",
								 "Configuration property",
								 TYPE_NAMES,
								 new String[] { "Property namespace", "Property name", "Property value" },
								 new OpenType[] { SimpleType.STRING, SimpleType.STRING, SimpleType.STRING });
	}
	
	private TabularType getTabType() throws OpenDataException {
		return new TabularType("ComnfigPropertyList",
							   "List of config properties",
							   getDataType(),
							   new String[] { TYPE_NAMES[0], TYPE_NAMES[1] });
	}
	
	private CompositeData toData(ConfigProperty p) throws OpenDataException {
		return new CompositeDataSupport(getDataType(), TYPE_NAMES, new String[] { p.getKey().getNamespace().toString(), p.getKey().getProperty(), p.getValue() });
	}
}
