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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>NB: </b> This class is within the public API because of build reasons,
 * it should only be used for testing! It will be moved shortly. See Trac issue
 * #417.
 * 
 * <p>This adapter has the following properties:
 * 
 * <ul>
 *  <li>It has a map with contract -> implementation associations, with add/remove methods
 * </ul>
 * 
 * The {@link #getServiceInstance(Class)} method uses an unchecked cast.
 * 
 * @author Larsan
 */
//FIXME: Move to test, if you can get Maven to support it, see Trac issue #417
public class ServiceRegistryAdapter implements ServiceRegistry {

	protected Map<Class<? extends Contract>, Object> implementations = new HashMap<Class<? extends Contract>, Object>();
	
	public ServiceRegistryAdapter() { }
	
	public <T extends Contract> void addImplementation(Class<T> contract, T service) {
		implementations.put(contract, service);
	}
	
	public <T extends Contract> void removeImplementation(Class<T> contract) {
		implementations.remove(contract);
	}
	
	@Override
	public <T extends Annotation> List<ServiceInfo> listServicesByAnnotation(Class<T> annotation) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public <T extends Contract> List<ServiceInfo> listServicesByContract(Class<T> contract) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ServiceInfo getServiceInfo(String publicId) {
		return null;
	}

	public <T extends Contract> ServiceInfo getServiceInfo(Class<T> contract, String publicId) {
		return null;
	}

	public Contract getServiceInstance(String publicId) { 
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends Contract> T getServiceInstance(Class<T> contract) {
		return (T) implementations.get(contract);
	}

	public <T extends Contract> T getServiceInstance(Class<T> contract, String publicId) {
		return null;
	}
}
