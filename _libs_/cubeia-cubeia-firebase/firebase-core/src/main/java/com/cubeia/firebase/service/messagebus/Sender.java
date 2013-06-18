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
 * A sender is synchronous or asynchronous object for dispatching
 * events over the mbus channels. Since senders can be created by the message 
 * bus on demand, the module handling the sender must call  {@link #destroy()} on the 
 * sender when it is no longer needed to clear up resources.
 * 
 * @author Larsan
 */
public interface Sender<T> {
	
	/**
	 * @return Id of the owning module, or null if not known
	 */
	public String getOwnerId();
	

	/**
	 * @param event Event type dispatch, must not be null
	 * @throws ChannelNotFoundException If the event cannot be routed
	 */
	public void dispatch(T event) throws ChannelNotFoundException;
	
	
	/**
	 * This method clears eventual resources taken by the sender. It is important 
	 * that this method is called when the sender is no longer used as the sender may
	 * have a one-to-one correspondence with system resources such as network sockets.
	 */
	public void destroy();
	
}
