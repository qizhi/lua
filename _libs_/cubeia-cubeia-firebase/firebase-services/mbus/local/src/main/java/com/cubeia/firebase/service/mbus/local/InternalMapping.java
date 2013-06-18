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
package com.cubeia.firebase.service.mbus.local;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusListener;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.util.Channels;
import com.cubeia.firebase.service.messagebus.util.PartitionMapImpl;

public class InternalMapping extends PartitionMapImpl {

	private final Logger log = Logger.getLogger(getClass());
	private final List<MBusListener> listeners = new CopyOnWriteArrayList<MBusListener>();
	
	public InternalMapping() {
		super("mbus_internal_partition_map");
	}
	
	
	@Override
	public void channelAdded(Partition part, Channel[] channels) {
		super.channelAdded(part, channels);
		for (MBusListener l : listeners) {
			l.channelAdded(part, channels);
		}
	}
	
	@Override
	public void channelRemoved(Partition part, Channel[] channels) {
		super.channelRemoved(part, channels);
		for (MBusListener l : listeners) {
			l.channelRemoved(part, channels);
		}
	}
	
	@Override
	public void channelMoved(Partition from, Partition to, Channel[] channels) {
		super.channelMoved(from, to, channels);
		for (MBusListener l : listeners) {
			l.channelMoved(from, to, channels);
		}
	}
	
	@Override
	public void partitionCreated(Partition part, Channel[] channels) {
		super.partitionCreated(part, channels);
		for (MBusListener l : listeners) {
			l.partitionCreated(part, channels);
		}
	}
	
	@Override
	public void partitionDropped(Partition part) {
		super.partitionDropped(part);
		for (MBusListener l : listeners) {
			l.partitionDropped(part);
		}
	}
	
	
	
	// --- LISTENERS --- //
	
	public void addMBusListener(MBusListener list) {
		Arguments.notNull(list, "listener");
		listeners.add(list);
	}
	
	public void removeMBusListener(MBusListener list) {
		Arguments.notNull(list, "listener");
		listeners.remove(list);
	}

	
	// --- REDISTRIBUTION --- //

	public void doAddChannel(String partId, int[] ids, String[] names) {
		Partition p = doGetPartition(partId);
		if(p == null) log.error("Received channel addition command for a non-existing partition '" + partId + "'");
		else {
			Channel[] arr = Channels.toChannels(ids, names);
			channelAdded(p, arr);
		}
	}
	
	public void doRemChannel(String partId, int[] ids) {
		Partition p = doGetPartition(partId);
		if(p == null) log.error("Received channel removal command for a non-existing partition '" + partId + "'");
		else {
			Channel[] arr = doGetChannels(p, ids);
			channelRemoved(p, arr);
		}
	}
	
	public Partition doAddPartition(String partId, EventType type, String name, SocketAddress parentId, String serverId) {
		Partition p = doGetPartition(partId);
		if(p != null) {
			log.error("Received partition addition command for a already existing partition '" + partId + "'");
			return null;
		} else {
			p = new Partition(type, partId, name, parentId, serverId);
			partitionCreated(p, new Channel[0]);
			return p;
		}
	}
	
	public void doRemPartition(String partId) {
		Partition p = doGetPartition(partId);
		if(p == null) log.warn("Received partition removal command for a non-existing partition '" + partId + "'");
		else {
			Channel[] chans = getChannelsForPartition(p);
			if(chans != null && chans.length > 0) {
				log.warn("Non-empty partition '" + p + "' dropped; " + chans.length + " channels will evaporate!");
			}
			partitionDropped(p);
		}
	}
	
	
	
	// --- PRIVATE METHODS --- //
	
	private Channel[] doGetChannels(Partition part, int[] channels) {
		lock.readLock().lock();
		try {
			List<Channel> tmp = new LinkedList<Channel>();
			populate(map.get(part), channels, tmp);
			return tmp.toArray(new Channel[tmp.size()]);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	private Partition doGetPartition(String partId) {
		lock.readLock().lock();
		try {
			for (Partition p : map.keySet()) {
				if(p.getId().equals(partId)) {
					return p;
				}
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	private void populate(Map<Integer, Channel> map, int[] channels, List<Channel> target) {
		for (int id : channels) {
			Channel ch = map.get(id);
			if(ch != null) {
				target.add(ch);
			}
		}
	}
}
