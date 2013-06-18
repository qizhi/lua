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

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * A simple interface for accessing system services. Service can be
 * accessed via their public id or via their contract class. If several
 * services implements the same contract, the public id must be used if a
 * specific implementation is wanted, otherwise the first found implementation
 * is used.
 * 
 * @author lars.j.nilsson
 */
public interface ServiceRegistry {

	/**
	 * This method retrieves the public information known about a particular 
	 * service. If the public id is not known null is returned.
	 * 
	 * @param publicId Public id of the service, must not be null
	 * @return The service info with the id, or null if not found
	 */
	public ServiceInfo getServiceInfo(String publicId);
	
	
	/**
	 * This method retrieves the public information known about a particular 
	 * service. If the public id is not known the registry will attempt to match
	 * the given contract class to available services, and should multiple services
	 * declare the same contract the first instance found will be returned.
	 * 
	 * @param contract Class of the contract, must not be null
	 * @param publicId Public id of the service if known, may be null
	 * @return The first service info found of the specific service class, or null if none was found
	 */
	public <T extends Contract> ServiceInfo getServiceInfo(Class<T> contract, String publicId);
	
	
	/**
	 * This method retrieves the public information of all services implementing
	 * a particular contract interface. 
	 * 
	 * @param contract Class of the contract, must not be null
	 * @return All services implementing the interface, never null
	 */
	public <T extends Contract> List<ServiceInfo> listServicesByContract(Class<T> contract);
	
	
	/**
	 * This method retrieves the public information of all services annotated
	 * with a particular annotation. 
	 * 
	 * @param annotation Annotation of the service, must not be null
	 * @return All services with the annotation, never null
	 */
	public <T extends Annotation> List<ServiceInfo> listServicesByAnnotation(Class<T> annotation);
	
	
	/**
	 * This method retrieves the public contract for a particular 
	 * service. If the public id is not known null is returned.
	 * 
	 * @param publicId Public id of the service, must not be null
	 * @return The service contract with the id, or null if not found
	 */
	public Contract getServiceInstance(String publicId);
	
	
	/**
	 * This method retrieves the public contract for a service. Should multiple services declare 
	 * the same contract the first instance found will be returned.
	 * 
	 * @param contract Class of the contract, must not be null
	 * @return The first service info found of the specific class, or null if none was found
	 */
	public <T extends Contract> T getServiceInstance(Class<T> contract);
	
	
	/**
	 * This method retrieves the public contract for a service. If the public id is 
	 * not known the registry will attempt to match the given contract class to available 
	 * services, and should multiple services declare the same contract the first instance found 
	 * will be returned.
	 * 
	 * @param contract Class of the contract, must not be null
	 * @param publicId Public id of the service if known, may be null
	 * @return The first service info found of the specific class, or null if none was found
	 */
	public <T extends Contract> T getServiceInstance(Class<T> contract, String publicId);

}
