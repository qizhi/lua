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

import java.util.List;

/**
 * The partition map represents a current view of the message bus
 * layout. It is a mbus listener in order to be used by client modules that needs
 * a constantly updating layout, as the message bus only returns partition
 * map copies.
 * 
 * @author Larsan
 */
public interface PartitionMap extends MBusListener {
	
	public int countChannels();
	
	public List<Partition> getAllPartitions(EventType type);

	/**
	 * @param part Partition to get channels for, never null
	 * @return All channels for the partition, null if partition is not found
	 */
	public Channel[] getChannelsForPartition(Partition part);
	
	
	/**
	 * @param part Partition to count channels for
	 * @return The number of known channels for the partition
	 */
	public int countChannelsForPartition(Partition part);
	
	/**
	 * @param type Type of the partition, must not be null
	 * @param channelId Id of channel to find
	 * @return The channels partition, or null if not found
	 */
	public Partition getPartitionForChannel(EventType type, int channelId);

	
	/**
	 * @return All known partitions, never null
	 */
	public Partition[] getAllPartitions();
	
	
	/**
	 * @param id Partition id, must not be null
	 * @return The partition object, null if not found
	 */
	public Partition getPartition(String id);

}