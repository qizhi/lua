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
package com.cubeia.firebase.server.service.jmx;

import javax.management.openmbean.TabularData;

/**
 * A simple interface for getting information about the mounted
 * services within a Firebase server. 
 * 
 * @author Larsan
 */
public interface RegistryStatsMBean {
	
	/**
	 * This method returns tabular data describing all services mounted
	 * in a Firebase server. The contains composite type data is described in
	 * the {@link ServiceBeanType} class.
	 * 
	 * @return All mounted services, never null
	 */
	public TabularData getServices();

	
	/**
	 * This method returns the number of services within the Firebase server.
	 * 
	 * @return The number of services, or 0
	 */
	public int getServiceCount();
	
}
