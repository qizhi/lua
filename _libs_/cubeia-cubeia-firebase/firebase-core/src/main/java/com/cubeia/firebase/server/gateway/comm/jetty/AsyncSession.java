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
package com.cubeia.firebase.server.gateway.comm.jetty;

import java.util.List;

import com.cubeia.firebase.io.ProtocolObject;

/**
 * This is a bridge interface between the HTTP servlets and their
 * client implementation. The session can either queue packets for delivery
 * or pass them on to a listener; however, if the listener is used no
 * packets will be queued. 
 * 
 * @author Lars J. Nilsson
 */
public interface AsyncSession {
	
	/**
	 * Report a disconnect from the HTTP layer.
	 */
	public void disconnected();

	/**
	 * Pass on a list of incoming packets to the local handlers
	 * and onwards.  
	 */
	public void handleIncoming(List<ProtocolObject> list);

	/**
	 * Flush and return and queued packets, waiting until 
	 * packets becomes available or a timeout occurs.
	 */
	@Deprecated
	public List<ProtocolObject> takeOutgoing();

	/**
	 * Set a listener to use, this listener will super-seed the 
	 * internal queue which will always be empty as long as a listener
	 * is used. 
	 */
	public void setAsyncSessionListener(AsyncSessionListener listener);

}
