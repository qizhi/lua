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

import java.util.HashMap;
import java.util.Map;

import com.cubeia.firebase.api.server.conf.ConfigProperty;
import com.cubeia.firebase.api.server.conf.Configurable;
import com.cubeia.firebase.api.server.conf.Namespace;

/**
 * <b>NB: </b> This class is within the public API because of build reasons,
 * it should only be used for testing! It will be moved shortly. See Trac issue
 * #417.
 * 
 * <p>This adapter base class has the following properties:
 * 
 * <ul>
 *  <li>A map with class to config associations, with add/remove methods
 *  <li>Namespaces and properties are ignored
 * </ul>
 * 
 * @author Larsan
 */
//FIXME: Move to test, if you can get Maven to support it, see Trac issue #417
abstract class ConfigProviderContractBase implements ConfigurationProvider {

	protected Map<Class<? extends Configurable>, Object> configs = new HashMap<Class<? extends Configurable>, Object>();
	
	ConfigProviderContractBase() { }
	
	public <T extends Configurable> void addConfiguration(Class<T> clazz, T config) {
		configs.put(clazz, config);
	}
	
	public <T extends Configurable> void removeConfiguration(Class<T> clazz) {
		configs.remove(clazz);
	}
	
	public ConfigProperty[] getAllProperties() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Configurable> T getConfiguration(Class<T> cl, Namespace ns) {
		return (T) configs.get(cl);
	}
}
