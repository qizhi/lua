/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.game;

/**
 * A scope listener is notified after a scope is entered, and
 * immediately prior to its exiting. This can be used to setup 
 * thread local resources for the scope.  
 * 
 * @author Lars J. Nilsson
 */
public interface ScopeListener {

	/**
	 * This method is called immediately after the event scope
	 * is entered.
	 */
	public void enter();
	
	/**
	 * This method is called immediately prior to the event scope
	 * is left.
	 */
	public void exit();
	
}
