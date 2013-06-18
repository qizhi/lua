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
package com.cubeia.space;


import java.util.Collection;

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.api.server.Startable;

/**
 * Accessor interface towards the table storage.
 * 
 * The Space implementation will rely on that the taking
 * Thread will execute the changes on the taken object
 * synchronuously and then return the object.
 * 
 * I.e. it must always be the same thread that returns a modified
 * object (put(...)) and a thread is not permitted to do two sequential
 * take(...) without a put in between.
 * 
 * Created on 2006-sep-11
 * @author Fredrik Johansson
 *
 * $RCSFile: $
 * $Revision: $
 * $Author: $
 * $Date: $
 */
public interface Space<E extends Identifiable> extends Startable, Haltable {
    
    /**
     * Acquire read lock and get the table.
     * Concurrent peeks are allowed.
     * 
     * Do not use the returned table for modification.
     *  
     * @param id
     * @return
     */
    public E peek(int id);
    
   
    /**
     * Acquire write lock and get the table.
     * Exclusive rights must be guaranteed by the underlying
     * implementation.
     *
     * The take must be followed by a put or rollback.
     * 
     * The procedure for assuring proper lock release is specified 
     * by the implementing handler.
     *
     * @param id
     * @return
     */
    public E take(int id);
    
    /**
     * Releases the object with the given id. This operation must have been preceded by a take operation.
     * @param id the object to release.
     */
    public void release(int id);
    
    /**
     * Release the write lock and update/write the table to the space.
     * This *must* currently be returned by the same thread that 
     * did the take(...) operation.
     * 
     * @param table
     */
    public void put(E object);
    
    /**
     * Get a collection of all tables contained in the space.
     * The collection is not for manipulating tables.
     * 
     * @return
     */
    public Collection<E> snapshot();
    
    /**
     * Returns true if the space contains the table with
     * the given tableid.
     * 
     * @param tableid
     * @return
     */
    public boolean exists(int objectid);
    
    
    /**
     * @param id If of object to remove
     * @return True if the object was removed, false otherwise
     */
	public boolean remove(int id);
	
	/**
     * Check if external JTA is supported.
     * 
     * @return If true we can apply XA JTA transactions, if false this is not supported (and might result in failure).
     */
    public boolean isJtaEnabled();
    
    /*
     * <p>Check if stateful replication is enabled.</p>
     * 
     * <p>If true then objects in the space should be replicated in order to provide
     * fail over in case of a member failure. The space will most likely still provide
     * replication of the initial object for propagation of initialized objects across
     * the cluster (i.e. objects created by an activator).</p>
     * 
     * <p>Disabling replication increases performance but disables transparent fail over.</p>
     *  
     * @return
     */
    // public boolean isFailOverEnabled();

    /**
     * Adds an object to the space. This operation is atomic.
     * @param data
     */
    public void add(E data);
    
}

