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
package com.cubeia.firebase.service.activation;

import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.service.activation.ActivationConfigManagerImpl.Key;

/**
 * JMX bean implementation. This implementation works directly on the
 * underlying manager and does not cache any values. 
 * 
 * @author Lars J. Nilsson
 * @see ActivationConfigMBean
 * @see ActivationConfigManagerImpl
 */
public class ActivationConfig implements ActivationConfigMBean {
	
	private static final String[] TYPE_NAMES = new String[] { "name", "source" };
	
	private final ActivationConfigManagerImpl inst;

	/**
	 * @param inst Manager to use, must not be null
	 */
	ActivationConfig(ActivationConfigManagerImpl inst) {
		this.inst = inst;
	}
	
	
	// --- MBEAN METHODS --- //
	
	@Override
	public int getConfigCount() {
		return inst.countConfigs();
	}

	@Override
	public TabularData getConfigs() {
		try {
			TabularDataSupport t = new TabularDataSupport(getTabType());
			t.putAll(toArray(inst.getSources()));
			return t;
		} catch (OpenDataException e) {
			Logger.getLogger(getClass()).error("Failed to create tabular data", e);
			return null;
		}
	}

	
	// --- PRIVATE METHODS --- //
	
	private CompositeData[] toArray(Map<Key, ConfigSource> sources) throws OpenDataException {
		int count = 0;
		CompositeData[] arr = new CompositeData[sources.size()];
		for (Key k : sources.keySet()) {
			ConfigSource s = sources.get(k);
			arr[count++] = toData(k, s);
		}
		return arr;
	}

	private CompositeType getDataType() throws OpenDataException {
		return new CompositeType("ActivationConfig",
								 "Activation config source",
								 TYPE_NAMES,
								 new String[] { "Config name", "Config source" },
								 new OpenType[] { SimpleType.STRING, SimpleType.STRING });
	}
	
	private TabularType getTabType() throws OpenDataException {
		return new TabularType("ActivationConfigList",
							   "List of activaton configs",
							   getDataType(),
							   new String[] { TYPE_NAMES[0] });
	}
	
	private CompositeData toData(Key key, ConfigSource src) throws OpenDataException {
		return new CompositeDataSupport(getDataType(), TYPE_NAMES, new String[] { src.getName() + "-" + key.type.toString().toLowerCase(), src.toString() });
	}
}
