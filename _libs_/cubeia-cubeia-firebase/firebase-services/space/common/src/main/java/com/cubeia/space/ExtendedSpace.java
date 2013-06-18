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

import com.cubeia.firebase.api.common.Identifiable;
import com.cubeia.space.service.CommitTimeRecorder;

/**
 * This is a semi-experimental interface for the space. It contains batch methods
 * and extended takes for debug information.
 * 
 * @author larsan
 */
public interface ExtendedSpace<E extends Identifiable> extends Space<E> {

	/**
	 * Batch add. All objects in the array will be 
	 * added to the space. 
	 * 
	 * @param objects Objects to add, must not be null
	 */
	public void add(E[] objects);
	
	
	/**
	 * Extended take, the added object should be the event triggering this
	 * take. It is added for lock debugging reasons. 
	 * 
	 * @param id Object id
	 * @param event Triggering event, may be null
	 * @return The event, or null
	 */
	public E take(int id, Object event);

	
	/**
	 * This method attaches an object to an existing lock. It is used
	 * to attach information on the given action for each event.
	 * 
	 * @param id Object id
	 * @param action Triggering action, may be null
	 */
	public void attachInfo(int id, Object action);
	
	
	/**
	 * A call-back for commit times. 
	 * 
	 * @param rec Recrder to use, or null for none
	 */
	public void setCommitTimeRecorder(CommitTimeRecorder rec);
	
	
	/**
	 * Batch remove. All objects in the array will be
	 * removed. 
	 * 
	 * @param objects Ids of objects to remove
	 */
	public void remove(int[] objects);

	
}
