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
package com.cubeia.firebase.service.activation;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceListener;

/**
 * This simple service keeps track of activation configuration
 * files within the system. The deployment manager should depend on this
 * service. Config sources are registered by name and type, and listener
 * will be notified when sources appear, are modified or disappear. 
 * 
 * @see ConfigSource
 * @see ConfigSourceListener
 * @author lars.j.nilsson
 */
public interface ActivationConfigManager extends Contract {

	/**
	 * Remove a listener from the manager. This method fails silently 
	 * if the listener is not previously added. 
	 * 
	 * @param list Listener to remove, must not be null
	 */
	public void removeConfigSourceListener(ConfigSourceListener list);
	
	
	/**
	 * Add config source listener. Currently the listener will get all
	 * configuration sources regardless of type. 
	 * 
	 * @param list Listener to add, must not be null
	 */
	public void addConfigSourceListener(ConfigSourceListener list);

	
	/**
	 * Get a configuration source for a named artifact. The artifact name is
	 * case-insensitive. The method returns null if no config is found.
	 * 
	 * @param name Source name, case-insensitive, must not be null
	 * @param type Type of the activation, must not be null
	 * @return The config source, or null if not found
	 */
	public ActivationConfigSource getConfigSourceFor(String name, ActivationType type);
	
	
	/**
	 * Register a new config source with the manager. 
	 * 
	 * @param src Register a new, or updated source, must not be null
	 * @param type Type of the activation, must not be null
	 */
	public void registerConfigSource(ConfigSource src, ActivationType type);
	
	
	/**
	 * Unregister a config source with the manager. 
	 * 
	 * @param name Source name, case-insensitive, must not be null
	 * @param type Type of the activation, must not be null
	 */
	public void unregisterConfigSource(String name, ActivationType type);
}
