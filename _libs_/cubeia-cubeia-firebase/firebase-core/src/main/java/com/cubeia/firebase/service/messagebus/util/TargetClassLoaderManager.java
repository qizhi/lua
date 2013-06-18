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

import com.cubeia.firebase.server.event.Event;

/**
 * This is a manager interface for finding target class loaders
 * for event. This is used by the unwrapping event listener.
 * 
 * @author Larsan
 */
public interface TargetClassLoaderManager {

	/**
	 * Given an event, return the class loader that should be 
	 * used when deserializing it.
	 */
	public ClassLoader getTargetClassLaoder(Event<?> e);
	
}
