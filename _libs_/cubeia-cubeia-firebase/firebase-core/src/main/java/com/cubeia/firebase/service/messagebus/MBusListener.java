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

/**
 * This is the listener interface on the message bus (mbus). It will be used by the 
 * mbus to signal logical changes in the cluster layout. Routers should listen using this
 * interface. 
 * 
 * @author lars.j.nilsson
 * @date 2007 apr 17
 */
public interface MBusListener {

	/**
	 * @param part Partition the channels are added to, never null
	 * @param channels One or more added channels, never null
	 */
	public void channelAdded(Partition part, Channel[] channels);
	
	
	/**
	 * @param part Partition the channels are removed from, never null
	 * @param channels One or more removed channels, never null
	 */
	public void channelRemoved(Partition part, Channel[] channels);
	
	
	/**
	 * @param from Partition the channels are moved from, never null
	 * @param to Partition the channels are moved to, never null
	 * @param channels One or more moved channels, never null
	 */
	public void channelMoved(Partition from, Partition to, Channel[] channels);
	
	
	/**
	 * @param type Type of the partition, never null
	 * @param part Created partition, never null
	 * @param channels Zero or more initial channels, may be null
	 */
	public void partitionCreated(Partition part, Channel[] channels);
	
	
	/**
	 * @param part Dropped partition, never null
	 */
	public void partitionDropped(Partition part);
	
}
