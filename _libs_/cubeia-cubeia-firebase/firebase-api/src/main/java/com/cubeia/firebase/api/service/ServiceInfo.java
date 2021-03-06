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
package com.cubeia.firebase.api.service;

/**
 * A simple interface used to describe a service. The information in
 * this interface is fetched from the service descriptors, please refer to 
 * the developers manual for more information.
 * 
 * @author lars.j.nilsson
 */
public interface ServiceInfo {
	
	/**
	 * @return True if the service is marked as auto-start, false otherwise
	 */
	public boolean isAutoStart();

	
	/**
	 * @return The service name, never null
	 */
	public String getName();
	
	
	/**
	 * @return The public id of the service, never null
	 */
	public String getPublicId();
	
	
	/**
	 * @return The full class name of the contract, never null
	 */
	public String[] getContractClasses();
	
	
	/**
	 * @return A short service description, may be null
	 */
	public String getDescription();
	
}
