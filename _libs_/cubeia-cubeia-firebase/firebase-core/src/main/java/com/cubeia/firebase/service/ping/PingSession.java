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
package com.cubeia.firebase.service.ping;

/**
 * A ping session represents a given client within the failure 
 * detection manager. The transport layer must report when a client
 * is idle, ie. when it has not received any data from the client 
 * within {@link #getMaxIdleTime()} millis, by calling 
 * {@link #reportIdle()}.
 * 
 * @author larsan
 */
public interface PingSession {
	
	/**
	 * The transport layer should call this method when it
	 * discovers that the client this session is associated with
	 * has been idle for at least {@link #getMaxIdleTime()} millis.
	 * This will initiate pings on the client.
	 */
	public void reportIdle();
	
	/**
	 * @return The max time a client may be idle, in millis, or -1 for disabling idle time
	 */
	public long getMaxIdleTime();

	/**
	 * The transport layer has received a ping response. The id corresponds to a 
	 * ping request.
	 * 
	 * @param id Id of the ping
	 */
	public void pingReceived(int id);
	
	/**
	 * The transport layer should call this method for each received data packet
	 * which is not a ping in order to let the ping session cancel current pinging
	 * if needed.
	 */
	public void dataReceived();

	/**
	 * Close this session and cleanup.
	 */
	public void close();
	
}
