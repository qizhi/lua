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

import javax.management.openmbean.TabularData;

/**
 * A simple MBean for the activation config manager. It exposes
 * the number of configurations tracked, and a table of config
 * source names and type.
 * 
 * @see ActivationConfigManagerImpl
 * @author Lars J. Nilsson
 */
public interface ActivationConfigMBean {

	/**
	 * Return the number of registered configurations
	 * if the manager.
	 * 
	 * @return the number of registered configurations
	 */
	public int getConfigCount();
	
	/**
	 * This returns a table of configu source names and types of
	 * all configus registered with the manager.
	 * 
	 * @return All configurations as a tabular data, or null on errors
	 */
	public TabularData getConfigs();
	
}
