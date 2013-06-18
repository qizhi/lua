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

/**
 * The event type is an enumeration for the type of events 
 * the message bus knows how to handle. This enumeration currently
 * holds a one-to-one relationship to the event type interfaces, but
 * this may change.
 * 
 * @author Larsan
 */
public enum EventType {

	/**
	 * Game event type. This type corresponds to events
	 * that should be routed to game partitions.
	 */
	GAME,

	/**
	 * Client event type. This type corresponds to events
	 * that should be routed to client partitions.
	 */
	CLIENT,
	
	
	/**
	 * Chat event type. This type corresponds to events
	 * that should be routed to client chat partitions.
	 */
	CHAT, 
	
	
	/**
	 * MTT event type. This type corresponds to events
	 * that should be routed to mtt partitions.
	 */
	MTT;

}
