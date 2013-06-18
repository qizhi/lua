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

import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;

public class InternalSender implements Sender<Event<?>> {

	private final EventSink queue;
	private final String ownerId;

	public InternalSender(String ownerId, EventSink queue) {
		this.ownerId = ownerId;
		this.queue = queue;
	}
	
	@Override
	public void destroy() { }

	@Override
	public void dispatch(Event<?> event) throws ChannelNotFoundException {
		queue.put(event);
	}

	@Override
	public String getOwnerId() {
		return ownerId;
	}
}
