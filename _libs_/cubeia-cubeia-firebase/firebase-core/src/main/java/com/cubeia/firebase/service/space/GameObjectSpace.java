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
package com.cubeia.firebase.service.space;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.api.server.Startable;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.processor.GameObjectProcessor;


/**
 * A Game Object Space provides the user with accessors to the space/storage
 * as well as an entry point for actions.
 * 
 * Starting this service creates an object space.
 * 
 * 
 * 
 * @author Fredrik
 *
 * @param <E> the object in the space
 * @param <T> The action within the event
 * @param <S> The event object
 */
public interface GameObjectSpace<E extends Identifiable, T extends Action> extends Startable, Haltable {

	/**
	 * Check if the service has been started.
	 * 
	 * @return true if started
	 */
	public boolean isStarted();
	
    /**
     * Acquire read lock and get the object.
     * Concurrent peeks are allowed.
     * 
     * Do not use the returned object for modification.
     * 
     * @param id
     * @return E or null if not found
     */
    public E peek(int id);
    
    /**
     * Add an object if not yet present in the space.
     * The space will be checked if the object already exists,
     * if so the current object will not be overwritten and the
     * method will return null;
     *  
     * @param id
     * @return The supplied object if added, otherwise null. 
     */
    public E add(E object);
    
    
	/**
	 * Remove an object from teh space. This method returns 
	 * true if the object was found and removed and false if
	 * the obejct could not be found.
	 * 
	 * @param id
	 * @return Trrue if the object was removed
	 */
	public boolean remove(int id);
    
    /*
     * Get a collection of all objects contained in the space.
     * The collection is not for manipulating objects.
     * 
     * @return
     */
    // public Collection<E> snapshot();
    
    /*
     * Returns true if the space contains the object with
     * the given objectid.
     * 
     * @param objectid
     * @return
     */
    // public boolean exists(int objectid);
    
    /**
     * Execute the action on the table space.
     * I.e. take the object, execute the action and
     * finally write the object back.
     * 
     * @param event
     * @return true if handled properly.
     */
    public boolean handle(Event<T> event, GameObjectProcessor<T> proc) throws SpaceObjectNotFoundException;
	
    /*
     * The is an executor style method for non actions, eg. maintainance.
     * This method goes through largely the same steps as the action handling
     * {@link #handle(GameAction, TableActionProcessor) method}, but uses the
     * callback interface for the actual execution.
     * 
     * <p>Should the object not exist, the callback interface will not be
     * called at all.
     * 
     * @param objectid Id of the object to process
     * @param callback Callback to use, must not be null
     * @return True if the handle executed normally
     */
    // @Deprecated
    // public boolean handle(int objectid, Processor<E> callback);
    
    
    /**
     * Given an element id, a "region id", register a specific class loader. This
     * can be used in circumstances where one space is used for objects partitioned
     * over multiple class loaders. The class loader is used for de-serialization.
     * 
     * @param gameId Game, or region, id
     * @param loader Class loader to use, must not be null
     */
    public void registerClassloader(int gameId, ClassLoader loader);
    
    /**
     * Unregister an element id, region id, from the space. If a class loader was 
     * previously registered for the region it should be removed.
     * 
     * @param gameId Game, or region, id
     */
    public void unregisterClassloader(int gameId);
    
}
