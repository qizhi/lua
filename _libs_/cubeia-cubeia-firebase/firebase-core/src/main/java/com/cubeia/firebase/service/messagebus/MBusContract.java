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

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.layout.ClusterLayout;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;

/**
 * This is the main message bus service contract. The server will use this
 * contract to create fetchers and sender for different system channels and 
 * partitions. This interface should be used sparingly and only accessed by
 * low level code. For example, the node routing is handled by separated classes
 * shielding the nodes from the messages bus.
 * 
 * @author Larsan
 */
public interface MBusContract extends Contract {
	
	public void halt();
	
	public void resume(ClusterLayout lay);
	
	
	/**
	 * @return Get the mbus details, never null
	 */
	public MBusDetails getMBusDetails();
	
	
	/**
	 * This method returns a copy of the current partition map. For modules
	 * needing to have a change-by-change update the partition map can be used as
	 * a {@link #addMBusListener(MBusListener) listener} on mbus events.
	 *  
	 * @return A copy of the current partition map, never null
	 */
	public PartitionMap getCurrentPartitionMap();

	
	/**
	 * @param list Listener to add, must not be null
	 */
	public void addMBusListener(MBusListener list);
	
	
	/**
	 * @param list Listener to remove, must not be null
	 */
	public void removeMBusListener(MBusListener list);

	
	/**
	 * @param list Listener to add, must not be null
	 */
	public void addRedistributorListener(RedistributionListener list);
	
	
	/**
	 * @param list Listener to remove, must not be null
	 */
	public void removeRedistributorListener(RedistributionListener list);
	
	
	/**
	 * This method creates a receiver for a given event type and partition. Usually
	 * the partition aggregates channels and thus one fetcher will represent a number 
	 * of channels. 
	 * 
	 * @param type Event type to the receiver, must not be null
	 * @param partition Partition id of the receiver, must not be null
	 * @param ownerId Identifier for the module which is going to own the receiver, must not be null
	 * @return A receiver for the given event type and partition, never null
	 * @throws MBusException If the fetcher cannot be created
	 */
	public Receiver<ChannelEvent> createReceiver(EventType type, String partition, String ownerId) throws MBusException;
	
	
	/**
	 * This method creates a sender for a given event type. Routing into partitions and
	 * channels is handled transparently by the message bus.
	 * 
	 * <b>NB:</b> Please note that this sender is not an instance of {@link WrappingSender}, events must
	 * be manually wrapped before sent with this object.
	 * 
	 * @param type Event type for the sender, must not be null
	 * @param ownerId Identifier for the module which is going to own the sender, must not be null
	 * @return A sender for the event type, never null
	 * @throws MBusException If the sender could not be created
	 */
	public Sender<Event<?>> createSender(EventType type, String ownerId) throws MBusException;

	
	/**
	 * This method returns a re-distributor for the message bus. It is important
	 * that only the currently acting master ever uses this method.
	 * 
	 * @return The mbus re-distributor, never null
	 */
	public MBusRedistributor getRedistributor();
	
	
	/*
	 * Check if the mbus supports a particular feature. Must features
	 * are used by individual fetchers/senders.
	 */
	// public boolean supports(Feature feat);
	
}
