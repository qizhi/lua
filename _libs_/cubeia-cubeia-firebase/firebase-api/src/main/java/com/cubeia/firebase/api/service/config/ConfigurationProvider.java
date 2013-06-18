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
package com.cubeia.firebase.api.service.config;

import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Namespace;

/**
 * A configuration provider is responsible for a set of configuration properties and
 * has the ability to adapt configuration interfaces on top of these properties. Please
 * refer to {@link Configurable} for more details on the config mechanism.
 * 
 * @author lars.j.nilsson
 */
public interface ConfigurationProvider {
	
	/**
	 * @return Get all properties in the handled configuration, never null
	 */
	public ConfigProperty[] getAllProperties();

	
	/**
	 * @param <T> Generic parameter for a {@link Configurable} extension
	 * @param cl Actual class of the {@link Configurable} to implement, must not be null
	 * @param ns Namespace to use for the adaption, may be null
	 * @return An instantiation of the given {@link Configurable}, never null
	 */
	public <T extends Configurable> T getConfiguration(Class<T> cl, Namespace ns);
	
}
