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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusListener;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;
import com.cubeia.firebase.util.FirebaseLockFactory;

/**
 * This is a utility object mapping partitions to channels. It is a
 * mbus listener for easy adoption. This object is thread safe.
 * 
 * @author lars.j.nilsson
 */

// FIXME: Rewrite maps in this class!! 
public class PartitionMapImpl implements MBusListener, PartitionMap {

	protected final ReadWriteLock lock;
	protected final Map<Partition, Map<Integer, Channel>> map = new HashMap<Partition, Map<Integer,Channel>>();
	protected final Map<EventType, Map<Integer, Partition>> channelToPartitionMap = new EnumMap<EventType, Map<Integer, Partition>>(EventType.class);
	protected final Map<String, Partition> partitionMap = new HashMap<String, Partition>();
	
	protected PartitionMapImpl(String jmxName) { 
		lock = FirebaseLockFactory.createLock(jmxName);
	}
	
	/**
	 * @param graph Initial partition layout, must not be null
	 */
	public PartitionMapImpl(Map<Partition, Channel[]> graph, String jmxName) {
		Arguments.notNull(graph, "graph");
		lock = FirebaseLockFactory.createLock(jmxName);
		unsafePutAll(graph);
	}
	
	/**
	 * @param clone Map to take clone from, must not be null
	 */
	public PartitionMapImpl(PartitionMapImpl clone) {
		Arguments.notNull(clone, "clone");
		lock = FirebaseLockFactory.createLock();
		clone.lock.readLock().lock();
		try {
			this.channelToPartitionMap.putAll(clone.channelToPartitionMap);
			this.partitionMap.putAll(clone.partitionMap);
			this.map.putAll(clone.map);
		} finally {
			clone.lock.readLock().unlock();
		}
	}
	
	
	// --- ACCESSORS --- //
	
	public int countChannels() {
		lock.readLock().lock();
		try {
			int len = 0;
			for (Map<Integer, Channel> m : map.values()) {
				len += m.size();
			}
			return len;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public List<Partition> getAllPartitions(EventType type) {
		lock.readLock().lock();
		try {
			List<Partition> list = new LinkedList<Partition>();
			for (Partition p : map.keySet()) {
				if(type == null || type.equals(p.getType())) {
					list.add(p);
				}
			}
			return list;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public int countChannelsForPartition(Partition part) {
		Arguments.notNull(part, "parts");
		lock.readLock().lock();
		try {
			Map<Integer, Channel> tmp = map.get(part);
			if(tmp == null) return 0; 
			else return tmp.size();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Partition getPartition(String id) {
		Arguments.notNull(id, "id");
		lock.readLock().lock();
		try {
			return partitionMap.get(id);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Partition getPartitionForChannel(EventType type, int channelId) {
		lock.readLock().lock();
		try {
			Map<Integer, Partition> map = channelToPartitionMap.get(type);
			return (map == null ? null : map.get(channelId));
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Channel[] getChannelsForPartition(Partition part) {
		Arguments.notNull(part, "parts");
		lock.readLock().lock();
		try {
			Map<Integer, Channel> tmp = map.get(part);
			if(tmp == null) return null; 
			else {
				return tmp.values().toArray(new Channel[tmp.size()]);
			}
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public Partition[] getAllPartitions() {
		lock.readLock().lock();
		try {
			return map.keySet().toArray(new Partition[map.size()]);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	
	// --- MBUS LISTENER --- //
	
	public void channelAdded(Partition part, Channel[] channels) {
		Arguments.notNull(part, "part");
		Arguments.notNull(channels, "channels");
		lock.writeLock().lock();
		try {
			Map<Integer, Channel> tmp = checkGetChannels(part);
			Map<Integer, Partition> map = checkGetPartitions(part);
			for (Channel ch : channels) {
				int channelId = ch.getId();
				map.put(channelId, part);
				tmp.put(channelId, ch);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void channelMoved(Partition from, Partition to, Channel[] channels) {
		Arguments.notNull(from, "from");
		Arguments.notNull(to, "to");
		Arguments.notNull(channels, "channels");
		lock.writeLock().lock();
		try {
			Map<Integer, Channel> fromChanMap = checkGetChannels(from);
			Map<Integer, Channel> toChanMap = checkGetChannels(to);
			Map<Integer, Partition> partMap = checkGetPartitions(from);
			for (Channel ch : channels) {
				int channelId = ch.getId();
				// Switch channel mapping
				fromChanMap.remove(channelId);	
				toChanMap.put(channelId, ch);
				// Switch partition mapping
				partMap.put(channelId, to);
			}
			/*if(fromChanMap.size() == 0) {
				map.remove(from);
			}*/
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void channelRemoved(Partition part, Channel[] channels) {
		Arguments.notNull(part, "part");
		Arguments.notNull(channels, "channels");
		lock.writeLock().lock();
		try {
			Map<Integer, Channel> fromMap = checkGetChannels(part);
			Map<Integer, Partition> fromMap2 = checkGetPartitions(part);
			for (Channel ch : channels) {
				int channelId = ch.getId();
				fromMap2.remove(channelId);
				fromMap.remove(channelId);
			}
			/*if(fromMap.size() == 0) {
				map.remove(part);
			}
			partitionMap.remove(part.getId());*/
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void partitionCreated(Partition part, Channel[] channels) {
		Arguments.notNull(part, "part");
		lock.writeLock().lock();
		try {
			Map<Integer, Channel> tmp = checkGetChannels(part);
			Map<Integer, Partition> map = checkGetPartitions(part);
			if (channels != null) {
				for (Channel ch : channels) {
					int channelId = ch.getId();
					map.put(channelId, part);
					tmp.put(channelId, ch);
				}
			}
			partitionMap.put(part.getId(), part);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void partitionDropped(Partition part) {
		Arguments.notNull(part, "part");
		lock.writeLock().lock();
		try {
			Map<Integer, Channel> channels = map.remove(part);
			if(channels != null) {
				for (int id : channels.keySet()) {
					channelToPartitionMap.remove(id);
				}
			}
			partitionMap.remove(part.getId());
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	
	// --- PROTECTED METHODS --- //
	
	/**
	 * This method sets the a given layout on the current object
	 * without using any read/write locks.
	 * 
	 * @param graph Layout to put, must not be null
	 */
	protected void unsafePutAll(Map<Partition, Channel[]> graph) {
		for (Partition p : graph.keySet()) {
			Channel[] arr = graph.get(p);
			partitionCreated(p, arr);
		}
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private Map<Integer, Partition> checkGetPartitions(Partition part) {
		Map<Integer, Partition> map = channelToPartitionMap.get(part.getType());
		if(map == null) {
			map = new TreeMap<Integer, Partition>();
			channelToPartitionMap.put(part.getType(), map);
		}
		return map;
	}
	
	// create map if it doesn't exist
	private Map<Integer, Channel> checkGetChannels(Partition part) {
		Map<Integer, Channel> tmp = map.get(part);
		if(tmp == null) {
			tmp = new TreeMap<Integer, Channel>();
			map.put(part, tmp);
		}
		return tmp;
	}
}
