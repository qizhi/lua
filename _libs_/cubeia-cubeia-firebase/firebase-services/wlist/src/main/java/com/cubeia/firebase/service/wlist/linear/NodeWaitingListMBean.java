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
package com.cubeia.firebase.service.wlist.linear;


public interface NodeWaitingListMBean {
	
	/**
	 * Dump all and everything in the node waiting list to logs in a 
	 * human readable format.
	 * 
	 * This is very expensive, so please do call on this in production code.
	 *
	 */
	public void dumpToLog();

	
	/**
	 * Get all created keys.
	 * 
	 * @return
	 */
	public String[] getKeys();
	
	/**
	 * This is the size of the default queue
	 * 
	 */
	public long getSize();
	
	/**
	 * This is the size of the default queue
	 * 
	 */
	public long getSize(String key);
	
	/**
	 * This is the average size for a key.
	 * I.e.
	 * the sum of all queue-sizes divided by number
	 * of keys.
	 */
	public long getAverageSize();
	
	/**
	 * Will clear all waiting list queues for this node.
	 * 
	 * @return
	 */
	public void clearAllQueues();
}
