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
package com.cubeia.firebase.api.game.activator;

import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.api.server.Context;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceListener;

/**
 * This is the context published towards the game activator from
 * the platform. It exposes helper objects for the activation process.
 * 
 * @author lars.j.nilsson
 */
public interface ActivatorContext extends Context { 
	
	/**
	 * This method returns the game id the activator is acting
	 * upon. The activator may be generic to act on several games, in
	 * which case this method will give it the correct id to use
	 * when creating tables, etc.
	 * 
	 * @return The game id
	 */
	public int getGameId();
	
	
	/**
	 * Each activator may be associated with a configuration. This method
	 * returns a source object, or null if the activator is not configured. 
	 * Currently the only available config source is via files in the 
	 * deployment folder.
	 * 
	 * @return The configuration source for the activator, or null if not found
	 */
	public ConfigSource getConfigSource();
	
	
	/**
	 * If the activator expects a configuration to be added, or even
	 * modified at a later time, it can set a listener with this method.
	 * 
	 * @param list Config source listener, may be null
	 */
	public void setConfigSourceListener(ConfigSourceListener list);
	
	
	/**
	 * This method returns the table factory to be used when creating tables. 
	 * The table factory will not be shared between several activators.
	 * 
	 * @return The factory table factory, never null
	 */
	public TableFactory getTableFactory();
	
	
	/**
	 * This method exposes the services available to the activator. The
	 * exposes services will be the same that are later exposed to the game.
	 * 
	 * @return The service registry of the platform, never null
	 */
	public ServiceRegistry getServices();

	/**
	 * This method returns a router for the activator which can be
	 * used to send events to tables and tournaments. To receive actions
	 * the activator should implement {@link RoutableActivator}.
	 * 
	 * @return The activator router, never null
	 */
	public ActivatorRouter getActivatorRouter();
	
}
