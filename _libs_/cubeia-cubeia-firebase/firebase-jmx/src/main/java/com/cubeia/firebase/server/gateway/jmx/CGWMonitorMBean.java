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
package com.cubeia.firebase.server.gateway.jmx;

/**
 * This bean is attached to the gateway and contains some information
 * about connected clients and packet data.
 * 
 * @author Larsan
 */
public interface CGWMonitorMBean {
	
	/**
	 * This method returns the number of clients connected
	 * to the gateway which are also seated at one or more tables.
	 * 
	 * @return The number of locally seated clients
	 */
	public long getLocalSeatedClients();
	
	/**
	 * This method returns the average number of game packets transmitted
	 * through the gateway per second. This method is only available in 
	 * statistics PROFILING mode.
	 * 
	 * @return The average number of game packets per second.
	 */
	public int getAverageGamePacketsPerSecond();
	
	/**
	 * This method return the number of locally logged in clients. For a 
	 * global count, use {@link #getGlobalClients()}.
	 * 
	 * @return The number of locally connected ad logged in clients
	 */
	public int getLocalClients();
	
	/**
	 * This method returns the number of globally logged in clients. For
	 * a local count, use {@link #getLocalClients()}.
	 * 
	 * @return The number of globally connected ad logged in clients
	 */
	public int getGlobalClients();
}
