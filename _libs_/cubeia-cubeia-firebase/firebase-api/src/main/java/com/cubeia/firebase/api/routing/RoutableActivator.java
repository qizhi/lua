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
package com.cubeia.firebase.api.routing;

/**
 * This interface should be implemented by activators wishing to be
 * available within the routing system to receive service events from 
 * the rest of the system. 
 * 
 * <p>Implementing services should be aware that this interface works much like
 * the message driven enterprise bean interface. It's accessor method should
 * be able to work asynchronously.
 * 
 * @author Lars J. Nilsson
 */
public interface RoutableActivator {

	/**
	 * @param e Received service action, never null
	 */
	public void onAction(ActivatorAction<?> e);

}
