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

import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.MBusRedistributor;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.RedistributionListener;

public class Redistributor implements MBusRedistributor {
	
	private final Logger log = Logger.getLogger(getClass());
	
	private final InternalMapping mapping;
	private final List<RedistributionListener> listeners;

	public Redistributor(InternalMapping mapping, List<RedistributionListener> listeners) {
		this.mapping = mapping;
		this.listeners = listeners;
	}

	@Override
	public void addChannels(String partition, int[] ids) throws MBusException { 
		mapping.doAddChannel(partition, ids, toChannelNames(ids));
	}

	@Override
	public void addPartition(EventType type, String id, SocketAddress parentId, String serverId) throws MBusException { 
		Partition p = mapping.doAddPartition(id, type, "Partition " + id, parentId, serverId);
		fireAdded(p);
	}

	@Override
	public void dropChannel(String partition, int id) throws MBusException { 
		mapping.doRemChannel(partition, new int[] { id });
	}

	@Override
	public void dropPartition(String id) throws MBusException { 
		Partition part = mapping.getPartition(id);
		if(part == null) {
			log.warn("Attempt to drop no-existing partition '" + id + "' ignored!");
			return; // SANITY CHECK
		} else {
			mapping.doRemPartition(id);
			fireRemoved(part);
		}
	}

	
	// --- PRIVATE METHODS --- //
	
	private void fireRemoved(Partition part) {
		for (RedistributionListener l : listeners) {
			l.partitionDropped(part);
		}
	}

	private void fireAdded(Partition part) {
		for (RedistributionListener l : listeners) {
			l.partitionCreated(part);
		}
	}
	
	private String[] toChannelNames(int[] ids) {
		String[] arr = new String[ids.length];
		for (int i = 0; i < ids.length; i++) {
			arr[i] = "Channel " + ids[i];
		}
		return arr;
	}
}
