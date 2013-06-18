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

import java.util.List;

import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.PartitionMap;

public class PartitionMapAdapter implements PartitionMap {

	public int countChannels() {
		return 0;
	}

	public int countChannelsForPartition(Partition part) {
		return 0;
	}

	public List<Partition> getAllPartitions(EventType type) {
		return null;
	}

	public Partition[] getAllPartitions() {
		return null;
	}

	public Channel[] getChannelsForPartition(Partition part) {
		return null;
	}

	public Partition getPartition(String id) {
		return null;
	}

	public Partition getPartitionForChannel(EventType type, int channelId) {
		return null;
	}

	public void channelAdded(Partition part, Channel[] channels) { }

	public void channelMoved(Partition from, Partition to, Channel[] channels) { }

	public void channelRemoved(Partition part, Channel[] channels) { }

	public void partitionCreated(Partition part, Channel[] channels) { }

	public void partitionDropped(Partition part) { }

}
