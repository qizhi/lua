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
package com.cubeia.firebase.service.messagebus.util;

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

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;

/**
 * An mbean implementation class. This class is fairly heavy
 * in its calculations, faster implementation may which to act as mbus
 * listeners directly.
 * 
 * @author Larsan
 */
public class PartitionMapInfo implements PartitionMapInfoMBean {
	
	private static final String[] PART_TYPE_NAMES = new String[] { "id", "channels" };
	private static final String[] CHANNEL_TYPE_NAMES = new String[] { "id", "name" };
	
	private final PartitionMap map;

	/**
	 * @param map Map to base info on, must not be null
	 */
	public PartitionMapInfo(PartitionMap map) {
		Arguments.notNull(map, "map");
		this.map = map;
	}
	
	
	// --- ACCESSORS --- //
	
	public String getPartitionForChannel(int typeOrdinal, int channelId) {
		EventType[] enums = EventType.values();
		EventType t = (typeOrdinal < 0 || typeOrdinal >= enums.length ? null : enums[typeOrdinal]);
		Partition part = (t == null ? null : map.getPartitionForChannel(t, channelId));
		return (part == null ? null : part.getId());
	}
	
	public int[] getChannelsForPartition(String part) {
		Partition p = map.getPartition(part);
		if(p == null) {
			Logger.getLogger(getClass()).debug("Coutld not find partition '" + part + "'");
			return new int[0];
		} else {
			return Channels.toIds(map.getChannelsForPartition(p));
		}
	}
	
	public TabularData getAllPartitions() {
		try {
			TabularDataSupport t = new TabularDataSupport(getPartitionTabType());
			t.putAll(toArray(map));
			return t;
		} catch (OpenDataException e) {
			Logger.getLogger(getClass()).error(e);
			return null;
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private CompositeData[] toArray(PartitionMap map) {
		try {
			int count = 0;
			Partition[] parts = map.getAllPartitions();
			CompositeData[] arr = new CompositeData[parts.length];
			for (Partition p : parts) {
				arr[count++] = toPartitionData(p, map.getChannelsForPartition(p));
			}
			return arr;
		} catch (OpenDataException e) {
			Logger.getLogger(getClass()).error(e);
			return null;
		}
	}
	
	private CompositeData toPartitionData(Partition p, Channel[] chans) throws OpenDataException {
		return new CompositeDataSupport(getPartitionDataType(), PART_TYPE_NAMES, new Object[] { p.getId(), toChannelsData(chans) });
	}

	private TabularData toChannelsData(Channel[] chans) throws OpenDataException {
		TabularDataSupport t = new TabularDataSupport(getChannelsTabType());
		t.putAll(toChannelsDataArr(chans));
		return t;
	}

	private CompositeData[] toChannelsDataArr(Channel[] chans) throws OpenDataException {
		int count = 0;
		CompositeData[] arr = new CompositeData[chans == null ? 0 : chans.length];
		if(chans != null) {
			for (Channel ch : chans) {
				arr[count++] = toChannelData(ch);
			}
		}
		return arr;
	}

	private CompositeData toChannelData(Channel ch) throws OpenDataException {
		return new CompositeDataSupport(getChannelDataType(), CHANNEL_TYPE_NAMES, new Object[] { String.valueOf(ch.getId()), ch.getName() });
	}

	private CompositeType getPartitionDataType() throws OpenDataException {
		return new CompositeType("Partition",
								 "Partition Data",
								 PART_TYPE_NAMES,
								 new String[] { "Id", "Channels" },
								 new OpenType[] { SimpleType.STRING, getChannelsTabType() });
	}
	
	private CompositeType getChannelDataType() throws OpenDataException {
		return new CompositeType("Channel",
								 "Channel Data",
								 CHANNEL_TYPE_NAMES,
								 new String[] { "Id", "Name" },
								 new OpenType[] { SimpleType.STRING, SimpleType.STRING });
	}
	
	private TabularType getChannelsTabType() throws OpenDataException {
		return new TabularType("ChannelTable",
							   "Partitions Channels",
							   getChannelDataType(),
							   new String[] { CHANNEL_TYPE_NAMES[0] });
	}

	private TabularType getPartitionTabType() throws OpenDataException {
		return new TabularType("PartitionTable",
							   "Node Partitions",
							   getPartitionDataType(),
							   new String[] { PART_TYPE_NAMES[0] });
	}
}
