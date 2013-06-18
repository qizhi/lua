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
package com.cubeia.firebase.api.mtt;

import com.cubeia.firebase.api.action.mtt.MttAction;

/**
 * <p>Tournament Logic interface. Implement this interface to provide your own logic.</p>
 * 
 * <p>By implementing this top-level you need to provide all logic. There are support
 * classes you can use for common task but they are not used per default.</p>
 * 
 * <p>If you wish to take advantage of a default implementation then check out extending
 * the MTTSupport class.</p>  
 *
 * @author Fredrik
 */
public interface MTTLogic {
	
    /**
     * This method is called after this tournament is created and before the
     * first action handler is called.
     * 
     * @param mttInstance the mtt instance object
     */
    public void tournamentCreated(MttInstance mttInstance);
    
    /**
     * This method is called after this tournament has been destroyed
     * and will be removed from the system after this call has completed.
     * 
     * @param mttInstance the mtt instance object
     */
    public void tournamentDestroyed(MttInstance mttInstance);
    
    
	/**
	 * <p>Process the action for the given state-object.</p>
	 * 
	 * <p>This method will be called concurrent, but never for
	 * the same state object. I.e. actions are processed sequentially
	 * for a tournament.</p>
	 * 
	 * @param action
	 * @param mttInstance
	 */
	public void handle(MttAction action, MttInstance mttInstance);
	
}
