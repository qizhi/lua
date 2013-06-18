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
 * This is an interface for asynchronous message retrieving. Since receivers can be created 
 * by the message bus on demand, the module handling the sender must call  {@link #destroy()} on the 
 * sender when it is no longer needed to clear up resources.
 * @author lars.j.nilsson
 */
public interface Receiver<T> {

	/**
	 * @param list Listener to add, must not be null
	 */
	public void addEventListener(EventListener<T> list);
	
	
	/**
	 * @param list Listener to remove, must not be null
	 */
	public void removeEventListener(EventListener<T> list);

	
	/**
	 * @param list Listener to use for orphaned events, may be null
	 */
	public void setOrphanEventListener(OrphanEventListener<ChannelEvent> list);

	
	/**
	 * This method clears eventual resources taken by the receiver. It is important 
	 * that this method is called when the receiver is no longer used as the receiver may
	 * have a one-to-one correspondence with system resources such as network sockets.
	 */
	public void destroy();
	
}
