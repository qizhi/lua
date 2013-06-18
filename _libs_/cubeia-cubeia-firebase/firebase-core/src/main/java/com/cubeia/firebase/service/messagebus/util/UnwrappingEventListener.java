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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.RouterEvent;

/**
 * This is an event listener adapter that first unwraps the
 * event before passing it on.
 * 
 * @author Larsan
 */
public class UnwrappingEventListener<E extends RouterEvent> implements EventListener<E> {

	private final TargetClassLoaderManager manager;
	private final EventListener<E> target;

	/**
	 * @param manager Manager to use, may be null
	 * @param target Target to use, must not be null
	 */
	public UnwrappingEventListener(TargetClassLoaderManager manager, EventListener<E> target) {
		Arguments.notNull(target, "target");
		this.manager = manager;
		this.target = target;
	}
	
	
	// --- EVENT LISTENER --- //
	
	public void eventReceived(E event) {
		if(manager == null) {
			forward(event);
		} else {
			tryUnwrap(event);
			forward(event);
		}
	}

	
	// --- PRIVATE METHODS --- //
	
	private void tryUnwrap(E event) { 
		try {
			Event<?> e = event.getRoutedEvent();
			ClassLoader load = manager.getTargetClassLaoder(e);
			e.unwrapForTarget(load);
		} catch (Exception e) {
			Logger.getLogger(getClass()).error("Failed to unwrap event", e);
		}
	}

	private void forward(E event) {
		target.eventReceived(event);
	}
}
