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
package com.cubeia.firebase.api.game.context;

import com.cubeia.firebase.api.server.Context;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.ResourceLocator;

/**
 * This is the context published to a game when it is
 * initialized. It gives access to a resource locator for 
 * accessor GAR resources, and to the service registry.
 *
 * @author lars.j.nilsson
 */
public interface GameContext extends Context {

	/**
	 * The resource locator is for reading resources from the
	 * game archive. This can be used to load configuration etc.
	 * 
	 * @return A resource locator for the game archive, never null
	 */
	public ResourceLocator getResourceLocator();
	
	/**
	 * @return The service registry, never null
	 */
	public ServiceRegistry getServices();
	
}