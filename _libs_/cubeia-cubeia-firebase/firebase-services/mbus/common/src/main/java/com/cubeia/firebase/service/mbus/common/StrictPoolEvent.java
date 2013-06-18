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
package com.cubeia.firebase.service.mbus.common;

import org.apache.log4j.Logger;

import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventType;

public final class StrictPoolEvent extends ChannelEvent { 
	
	private static final long serialVersionUID = 559131529570758034L;
	
	private final Logger log = Logger.getLogger(getClass());
	
	private final StrictQueueReceiver<String> queue;
	private final StrictPooledHandoff handoff;
	private final String key;

	StrictPoolEvent(Event<?> event, EventType type, int channel, String key, StrictQueueReceiver<String> queue, StrictPooledHandoff handoff) {
		super(event, type, channel, false);
		this.key = key;
		this.queue = queue;
		this.handoff = handoff;
	}
	
	@Override
	public boolean isValid() {
		return handoff.isValid(getChannel());
	}
	
	public void acknowledge() {
		if(log.isTraceEnabled()) {
			log.trace("Acknownledged event [" + getRoutedEvent() + "] with key " + key + " on channel " + getChannel());
		}
		handoff.retract(getChannel());
		queue.remove(key);
	}
}
