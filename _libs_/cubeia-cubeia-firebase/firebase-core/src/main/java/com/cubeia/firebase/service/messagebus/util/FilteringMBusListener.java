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
import com.cubeia.firebase.service.messagebus.MBusListener;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.util.Filter;

/**
 * This listener passes the event through to another mbus listener
 * only if the event regards a partition according to a filter. For 
 * operations involving two partitions, the filter is checked for either
 * or.
 * 
 * @author Larsan
 * @date 2007 maj 15
 */
public abstract class FilteringMBusListener implements MBusListener {

	private final Filter<Partition> filter;
	
	protected MBusListener next;

	/**
	 * @param filter Filter to check on, must not be null
	 * @param next Listener to pass the events through to, may be null
	 */
	public FilteringMBusListener(Filter<Partition> filter, MBusListener next) {
		Arguments.notNull(filter, "filter");
		this.filter = filter;	
		this.next = next;
	}
	
	public void channelAdded(Partition part, Channel[] channels) {
		if(next != null && filter.accept(part)) next.channelAdded(part, channels);
	}

	public void channelMoved(Partition from, Partition to, Channel[] channels) {
		if(next != null && (filter.accept(from) || filter.accept(to))) next.channelMoved(from, to, channels);
	}

	public void channelRemoved(Partition part, Channel[] channels) {
		if(next != null && filter.accept(part)) next.channelRemoved(part, channels);
	}

	public void partitionCreated(Partition part, Channel[] channel) {
		if(next != null && filter.accept(part)) next.partitionCreated(part, channel);
	}

	public void partitionDropped(Partition part) {
		if(next != null && filter.accept(part)) next.partitionDropped(part);
	}
}
