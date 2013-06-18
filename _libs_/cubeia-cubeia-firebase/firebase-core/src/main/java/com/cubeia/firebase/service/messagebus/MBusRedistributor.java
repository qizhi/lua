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

import com.cubeia.firebase.api.util.SocketAddress;

/**
 * A redistributor is responsible for programmatic changes in the
 * message bus layout. It is important only the currently acting
 * master node ever uses this interface for concurrency reasons.
 * 
 * <p><b>Orphan Channels</b><br>
 * Of a partition is dropped when it currently "contains" channels these
 * channels will move into an "orphan" state indicating that they currently
 * has no partition. Currently this will not be indicated other than in the logs. 
 * Orphaned channels should be avoided as they may be a strain on resources but the
 * message bus will attempt to handle them transparently to the user. Orphan 
 * channels will remain in orphan state until a new partition is added corresponding
 * the their event type. This has the effect that a newly added empty channel, may 
 * be immediately populated "behind the scenes".
 * 
 * @author lars.j.nilsson
 */
public interface MBusRedistributor { 
	
	/**
	 * Drop partition with a given id. This method logs a warning if the 
	 * partition cannot be found. Should the partition not be empty, so called
	 * "orphan channels" may be created (see above).
	 * 
	 * @param id Id of the partition to drop, must not be null
	 * @throws MBusException 
	 */
	public void dropPartition(String id) throws MBusException;
	
	/**
	 * Add a partition for an event type to the message bus. The given 
	 * id must be unique. The partition will be created empty unless there
	 * are "orphan channels" corresponding to the event type (see above).
	 * 
	 * @param type Event type for the channel, must not be null
	 * @param id Partition id, must be unique and not null
	 * @param mbusSocketId Socket if for the partition in the mbus, must not be null
	 * @param serverId Server id matching the mbus socket, must not be null
	 * @throws MBusException 
	 */
	public void addPartition(EventType type, String id, SocketAddress mbusSocketId, String serverId) throws MBusException;
	
	/**
	 * Remove a channel from a partition. This method logs a warning if the 
	 * partition or the channel cannot be found.
	 * 
	 * @param partition Partition id to drop channel from, must not be null
	 * @param id Id of the channel to drop
	 * @throws MBusException
	 */
	public void dropChannel(String partition, int id) throws MBusException;

	
	/**
	 * Add a channel to a partition. The channel ids should be unique. An exception
	 * will be thrown if the partition cannot be found.
	 * 
	 * @param partition Partition id to add channel to, must not be null
	 * @param ids Id of the new channels, must be unique
	 * @throws MBusException
	 */
	public void addChannels(String partition, int[] ids) throws MBusException;
	
}
