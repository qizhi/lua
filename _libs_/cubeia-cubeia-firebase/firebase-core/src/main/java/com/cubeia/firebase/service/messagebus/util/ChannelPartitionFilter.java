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

import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.service.messagebus.Channel;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.util.TrivialFilter;

/**
 * Used by the game event deamon to be notified when tables are
 * added to the current partition.
 * 
 * @author Larsan
 */
public class ChannelPartitionFilter extends FilteringMBusListener {

	public ChannelPartitionFilter(final Partition part, final ChannelChangeListener listener) {
		super(new TrivialFilter<Partition>(part), null);
		Arguments.notNull(listener, "listener");
		super.next = new MBusListenerAdapter() {
		
			@Override
			public void channelMoved(Partition from, Partition to, Channel[] channels) {
				if(part.equals(to)) {
					listener.addition(channels, true);
				} else {
					listener.removal(channels, true);
				}
			}
		
			@Override
			public void channelAdded(Partition part, Channel[] channels) {
				listener.addition(channels, false);
			}
			
			@Override
			public void channelRemoved(Partition part, Channel[] channels) {
				listener.removal(channels, false);
			}
			
			@Override
			public void partitionCreated(Partition createdPartition, Channel[] channels) {
				// Ticket #632, we need to reschedule actions when channel-objects are added locally
				if(part.equals(createdPartition)) {
					listener.addition(channels, true);
				}
			}
		};
	}
}
