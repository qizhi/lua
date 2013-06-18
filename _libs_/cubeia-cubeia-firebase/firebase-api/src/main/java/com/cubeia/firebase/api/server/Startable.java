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
package com.cubeia.firebase.api.server;

/**
 * Interface for startable components. These components are guaranteed by
 * their context that:
 * 
 * <ul>
 *     <li>If they are {@link Initializable} they will always by initialized 
 *         before started, and never destroyed unless previously stopped.</li>
 *     <li>That they will never be stopped unless previously started.</li>
 * </ul>
 * 
 * @author lars.j.nilsson
 */
public interface Startable {

	public static enum State {
		STARTED,
		STOPPED
	}
	
    /**
     * Start the component. Some components may allow this method to 
     * throw runtime exception to signal a late error.
     */
    public void start();
    
    /**
     * Stop the component.
     */
    public void stop();
    
}
