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
package com.cubeia.firebase.api.mtt.activator;

import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.api.routing.RoutableActivator;
import com.cubeia.firebase.api.server.Context;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceListener;

/**
 * Holds contextual references needed by implementing MTT Activator.
 * 
 * @author Fredrik
 */
public interface ActivatorContext extends Context { 
	
	
	/**
	 * @return The mtt id
	 */
	public int getMttId();
	
	
	/**
	 * @return The service registry of the platform, never null
	 */
	public ServiceRegistry getServices();

	
	/**
	 * @return The configuration source for the activator, or null if not found
	 */
	public ConfigSource getConfigSource();
	
	
	/**
	 * @param list Config source listener, may be null
	 */
	public void setConfigSourceListener(ConfigSourceListener list);
	
	
	/**
	 * This method returns a router for the activator which can be
	 * used to send events to tables and tournaments. To receive actions
	 * the activator should implement {@link RoutableActivator}.
	 * 
	 * @return The activator router, never null
	 */
	public ActivatorRouter getActivatorRouter();
	
}
