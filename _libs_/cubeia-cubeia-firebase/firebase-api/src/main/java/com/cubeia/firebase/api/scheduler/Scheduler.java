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
package com.cubeia.firebase.api.scheduler;

import java.util.UUID;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.util.UnmodifiableSet;

/**
 * This object manages scheduled actions for a particular object. Using this
 * interface scheduled actions can be associated with a table for later processing.
 * It is important to remember to remove the scheduled action if it is canceled.
 * 
 * @author Lars J. Nilsson
 */
public interface Scheduler<T extends Action> {
	
	/**
	 * @param id Id of action to check, must not be null
	 * @return True if there is an action with the id, false otherwise
	 */
	public boolean hasScheduledGameAction(UUID id);
	
	/**
	 * @return The ids of all known scheduled actions, never null
	 */
	public UnmodifiableSet<UUID> getAllScheduledGameActions();

	/**
	 * @return The delay in millis for a scheduled action, or -1 if not known
	 * @param id Task id, must not be null
	 */
	public long getScheduledGameActionDelay(UUID id);
	
	/**
	 * @return The scheduled action, or null if not found
	 * @param id Task id, must not be null
	 */
	public Action getScheduledGameAction(UUID id);
	
    /**
     * @param action Action to schedule, must not be null
     * @param delay time in millis to wait until executing this action.
     * @return a UUID for identifying the scheduled task
     */
    public UUID scheduleAction(T action, long delay);
    
    /**
     * @param id Task id, must not be null
     */
    public void cancelScheduledAction(UUID id);
    
    /**
     * This method safely cancel all scheduled actions
     * in this scheduler.
     */
	public void cancelAllScheduledActions();
    
}
