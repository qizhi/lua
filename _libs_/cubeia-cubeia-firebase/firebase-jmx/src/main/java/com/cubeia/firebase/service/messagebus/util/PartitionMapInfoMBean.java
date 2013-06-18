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

import javax.management.openmbean.TabularData;

/**
 * MBean interface for exposing a partition map. It currently only 
 * contains tabular data for the map and an operation for getting all
 * channel ID's from a specific partition.
 * 
 * @author Larsan
 */
public interface PartitionMapInfoMBean {

	/**
	 * Get all partitions as tabular data. 
	 * 
	 * @return All partitions and channels, null on errors
	 */
	public TabularData getAllPartitions();

	
	/**
	 * Get all channel ids for a partition. 
	 * 
	 * @param partId Partition id, must not be null
	 * @return All channel ID's for the partition, or null on error
	 */
	public int[] getChannelsForPartition(String partId);
	
	/**
	 * This method returns the partition id for a given channel. The channel
	 * must also be prefixed with its type, which is determined by the ordinal of
	 * the EventType enumeration.
	 * 
	 * @param typeOrdinal EventType enum ordinal
	 * @param channelId The channel to look for within the type
	 * @return The partition id, or null if not found
	 */
	public String getPartitionForChannel(int typeOrdinal, int channelId);

}
