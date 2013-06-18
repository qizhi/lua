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

import com.cubeia.firebase.server.event.Event;

/**
 * This is a wrapper interface for an event that has passed through the 
 * message bus and may be subject persistent or fail safe delivery rules. 
 * It has a {@link #acknowledge()} method which should be called when an event is processed
 * and can be discarded. This method acts much like the acknowledge methods in 
 * JMS but with accommodations for the slightly different message bus demands.
 * 
 * @author Larsan
 */
public interface RouterEvent {
    
	/**
	 * @return The routed event, never null
	 */
	public Event<?> getRoutedEvent();
	
	/**
	 * This method verifies whether an event is "valid" or not. This should
	 * be called in case an halt has occurred during the processing in which case
	 * this method may return false. In practicality this method verifies that the
	 * underlying mbus still considers the event valid for the current node.
	 * 
	 * @return True if the mbus agrees the event should be processed on the current node
	 */ 
	public boolean isValid();
	
    /**
     * If this method returns true, the mbus will attempt to bypass
     * fail-over replication and only send the message to a single
     * target.
     * 
     * @return True to bypass fail-over sending, false otherwise
     */
    public boolean getForceSingleTarget();
	
	/**
	 * Acknowledge event when processed. This method acts much like the acknowledge methods in 
	 * JMS but with accommodations for the slightly different message bus demands. The root implementation
	 * is empty and should be overridden by subclasses.
	 */
	public void acknowledge();
	
}
