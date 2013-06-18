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
package com.cubeia.firebase.util;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.service.ServiceRegistry;

/**
 * This adapter can be initialized with a map of contracts. And
 * only get by contract works. 
 * 
 * @author Larsan
 */
// FIXME: Trac issue #417
public class ServiceRegistryAdapter implements ServiceRegistry {
	
	private final Map<Class<?>, Contract> map = new HashMap<Class<?>, Contract>();
	
	public ServiceRegistryAdapter(Map<Class<?>, Contract> map) {
		if(map != null) {
			this.map.putAll(map);
		}
	}
	
	public ServiceRegistryAdapter() {
		this(null);
	}
	
	public ServiceRegistryAdapter(Class<?> c, Contract con) {
		map.put(c, con);
	}
	
	@Override
	public <T extends Annotation> List<ServiceInfo> listServicesByAnnotation(Class<T> annotation) {
		return Collections.emptyList();
	}
	
	@Override
	public <T extends Contract> List<ServiceInfo> listServicesByContract(Class<T> contract) {
		return Collections.emptyList();
	}
	
	public void addContract(Class<?> c, Contract con) {
		map.put(c, con);
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
		return (T) map.get(contract);
	}

	public <T extends Contract> T getServiceInstance(Class<T> contract, String publicId) {
		// TODO Auto-generated method stub
		return null;
	}
}
