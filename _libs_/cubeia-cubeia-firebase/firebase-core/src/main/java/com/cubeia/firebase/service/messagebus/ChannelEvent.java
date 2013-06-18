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
package com.cubeia.firebase.service.messagebus;

import java.io.Serializable;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.event.Event;

/**
 * This class acts as an immutable wrapper for an event which has passed, or
 * should pass, through a channel in the message bus. As such it keeps a reference
 * to the root event as well as to the channel id and the event type.
 * 
 * @author Larsan
 */
public class ChannelEvent implements Serializable, RouterEvent {

	private static final long serialVersionUID = 7247981524136496858L;
	
	private final Event<?> event;
	private final int channel;
	private final EventType type;

	private transient boolean forceSingleTarget;
	
	/**
	 * @param event Root event, must not be null
	 * @param type Event type, may be null
	 * @param channel Channel id
	 */
	public ChannelEvent(Event<?> event, EventType type, int channel, boolean forceSingleCast) {
		Arguments.notNull(event, "event");
		forceSingleTarget = forceSingleCast;
		this.type = type;
		this.event = event;
		this.channel = channel;
	}
	
	/**
	 * Acknowledge event when processed. This method acts much like the acknowledge methods in 
	 * JMS but with accommodations for the slightly different message bus demands. The root implementation
	 * is empty and should be overridden by subclasses.
	 */
	public void acknowledge() { }
	
	
	/**
	 * This method always returns true.
	 */
	public boolean isValid() {
		return true;
	}
	
	
    /**
     * If this boolean is set, the mbus will attempt to 
     * single-cast the message. In other words, only target 
     * a single node for delivery. Please be careful setting
     * this boolean as it may destroy fail-over.
     * 
     * @param force True to bypass fail-over sending, false otherwise
     */
    public void setForceSingleTarget(boolean forceSingleTarget) {
		this.forceSingleTarget = forceSingleTarget;
	}
    
    
    /**
     * If this method returns true, the mbus will attempt to bypass
     * fail-over replication and only send the message to a single
     * target.
     * 
     * @return True to bypass fail-over sending, false otherwise
     */
    public boolean getForceSingleTarget() {
		return forceSingleTarget;
	}
	
    
	/**
	 * @return The event type, may return null
	 */
	public EventType getType() {
		return type;
	}
	
	
	/**
	 * @return The routed root event, never null
	 */
	public Event<?> getRoutedEvent() {
		return event;
	}
	
	
	/**
	 * @return The channel id
	 */
	public int getChannel() {
		return channel;
	}
	
	
	// --- OBJECT METHODS --- //
	
	@Override
	public String toString() {
		return "Channel Event [channel: " + channel + "; type: " + type + "; event: " + event + "]";
	}
}
