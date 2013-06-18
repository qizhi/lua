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

import org.apache.log4j.Logger;

import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.service.mbus.common.StrictPooledHandoff;
import com.cubeia.firebase.service.mbus.common.StrictQueueReceiver;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;

public abstract class PartitionedQueue implements EventSink {

	protected final PartitionedQueues repo;
	
	public PartitionedQueue(PartitionedQueues repo) {
		this.repo = repo;
	}
	
	@Override
	public void put(Event<?> event) {
		int id = getChannelForEvent(event);
		StrictQueueReceiver<String> queue = getObjectReceiver(id);
		if(queue != null) {
			handoff(id, event, queue);
		} else {
			orphan(id, event);
		}
	}

	protected abstract StrictQueueReceiver<String> getObjectReceiver(int id);
	
	protected abstract StrictPooledHandoff getObjectHandoff();
	
	protected abstract OrphanEventListener<ChannelEvent> getOrphanListener();
	
	
	// --- PRIVATE METHODS --- //
	
	private int getChannelForEvent(Event<?> e) {
		if(e instanceof GameEvent) {
		    return ((GameEvent)e).getTableId();
		} else if (e instanceof MttEvent) {
		    return ((MttEvent)e).getMttId();
		} else if (e instanceof ClientEvent<?>) {
			return ((ClientEvent<?>)e).getTableId();
		} else {
			return -1;
		}
	}

	private void orphan(int id, Event<?> event) {
		OrphanEventListener<ChannelEvent> list = getOrphanListener();
		if(list == null) {
			Logger.getLogger(getClass()).error("Event received from non-existing channel: " + id + "; event: " + event);
		} else {
			list.orphanedEvent(new ChannelEvent(event, EventType.GAME, id, false));
		}
	}

	private void handoff(int id, Event<?> event, StrictQueueReceiver<String> queue) {
		((QueueReceiverWrap)queue).put(event);
		getObjectHandoff().submit(id);
	}
}
