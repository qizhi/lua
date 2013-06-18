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
package com.cubeia.firebase.util;

/**
 * This is a generalized MBean to describe a JBoss tree cache. It contains
 * simple debugging information and methods to retrieve the cache contents
 * as a string.
 * 
 * @author Larsan
 */
public interface TreeCacheInfoMBean {

	/**
	 * Get the local socket address of the cache. This is the
	 * end point to which the cache binds. 
	 * 
	 * @return The local socket end point, or null if not connected
	 */
	public String getLocalAddress();
	
	/**
	 * This method attempts to print the entire cache content as a
	 * string. <em>NB: </em> Use with care as this method is very resource
	 * demanding on large caches.
	 * 
	 * @return The cache content as a string, never null
	 */
	public String printCacheContentDetails();
	
	/**
	 * The cache is backed up by a jgroups channel for replication. This
	 * method returns the jgroups configuration as a string.
	 * 
	 * @return The jgroups configuration, or null if not started
	 */
	public String printJGroupsConfig();
	
	/**
	 * This method returns all members in a cache cluster, represented
	 * by their {@link #getLocalAddress() local addresses}. 
	 * 
	 * @return The cache cluster members, never null
	 */
	public String[] getMembers();
	
	/**
	 * Get the number of nodes in the cache. 
	 * 
	 * @return The number of nodes in the cache
	 */
	public int getObjectCount();
	
	/**
	 * Print debugging information about the cache locks. This method may
	 * be resource heavy, so use with care.
	 * 
	 * @return Cache lock information as a string, or null if not connected
	 */
	public String printCacheLockingInfo();
	
}
