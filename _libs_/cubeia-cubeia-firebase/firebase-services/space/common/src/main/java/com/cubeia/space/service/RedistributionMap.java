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
package com.cubeia.space.service;

import org.jgroups.ChannelException;

import com.cubeia.firebase.api.common.Identifiable;

public interface RedistributionMap<E extends Identifiable> {

	/**
	 * Connect channel. This will setup the jgroups connection, connect
	 * and send an initial HELO to the cluster.
	 */
	public void connect() throws ChannelException;

	/**
	 * Close the channel. This should only be called once.
	 */
	public void disconnect();

	/**
	 * @param id Id of the object to look for
	 * @return True if the object exists locally, will not cause a data gravitation
	 */
	public boolean existsLocal(int id);

	/**
	 * @param id Id of the object to look for
	 * @return True if the object is backed up on this node, will not case any gravitation
	 */
	public boolean existsBuddy(int id);

	/**
	 * Batch add method. This will add a number of object at the same time. It 
	 * should <em>only</em> be used for add, if any of the given objects already exists
	 * in the cluster the behaviour is undetermined.
	 * 
	 * <p>This method will propagate copies of the objects to the current node's
	 * buddy backup, if one exists.
	 * 
	 * @param objects Objects to add, must not be null
	 */
	public void add(E... objects);

	/**
	 * Set a single object. This can be either an add or an update. This method will
	 * gravitate the data to the current node if the object exists in the cluster, and
	 * will make sure the buddy backup is correctly located. 
	 * 
	 * @param object Object to set, must not be null
	 */
	public void set(E object);

	/**
	 * @param ids Ids of objects to remove from cluster, must not be null
	 */
	public void remove(int[] ids);

	/**
	 * @param id Id of object to remove from cluster
	 */
	public void remove(int id);

	/**
	 * @param id Id of the object to look for
	 * @return An object from the cluster, or null if not found, will gravitate object if necessary
	 */
	public E get(int id);

    /**
     * @return The node name, never null
     */
    public String getName();

    /**
     * Clear the maps. This should only be used for testing.
     */
	public void reset();

    /**
     * @param id Id of the object to look for
     * @return The object if it exists locally, will not cause gravitation
     */
	public E peekLocal(int i);

    /**
     * @param id Id of the object to look for
     * @return The object if it exists in the backup values, will not cause any gravitation
     */
	public E peekBuddy(int i);

    /*
     * @return An unmodifiable list of the members, never null
     */
	// public List<Address> getMembers();

    /*
     * @return The local node address, or null if not connected
     */
	// public Address getLocalAddress();

    /*
     * @return the buddy address, if known
     */
	// public Address getBuddyAddress();

    /*
     * @return An extended "toString" with data from the map
     */
	// public String infoString();
	
}