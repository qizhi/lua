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
import java.util.concurrent.CopyOnWriteArrayList;

import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.Sender;

public class TrivialSenderReceiver implements Sender<Event<?>>, Receiver<ChannelEvent> {

	private final List<EventListener<ChannelEvent>> listeners = new CopyOnWriteArrayList<EventListener<ChannelEvent>>();
	
	private final String ownerId;
	private final EventType type;

	public TrivialSenderReceiver(String ownerId, EventType type) {
		this.ownerId = ownerId;	
		this.type = type;
	}
	
	@Override
	public void destroy() { }

	@Override
	public void dispatch(Event<?> event) throws ChannelNotFoundException {
		ChannelEvent e = new ChannelEvent(event, type, getChannelForEvent(event), false);
		for (EventListener<ChannelEvent> list : listeners) {
			list.eventReceived(e);
		}
	}

	@Override
	public String getOwnerId() {
		return ownerId;
	}

	@Override
	public void addEventListener(EventListener<ChannelEvent> list) {
		listeners.add(list);
	}

	@Override
	public void removeEventListener(EventListener<ChannelEvent> list) {
		listeners.remove(list);
	}

	@Override
	public void setOrphanEventListener(OrphanEventListener<ChannelEvent> list) { }

	
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
}
