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
package com.cubeia.firebase.mock;

import java.lang.annotation.Annotation;
import java.util.List;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.service.ServiceRegistry;

/**
 * I cant complete this since I must depend on the SystemStateServiceImpl which
 * is in a service module... hmmm... ?
 * 
 * @author Fredrik
 *
 */
public class MockServiceRegistry implements ServiceRegistry {

	// private SystemStateCacheHandler cache = new SystemStateCacheHandler("com/cubeia/firebase/systemstate/systemstate-local-test-service.xml");
	
	public ServiceInfo getServiceInfo(String publicId) {
		return null;
	}
	
	@Override
	public <T extends Annotation> List<ServiceInfo> listServicesByAnnotation(Class<T> annotation) {
		return null;
	}
	
	@Override
	public <T extends Contract> List<ServiceInfo> listServicesByContract(Class<T> contract) {
		return null;
	}

	public <T extends Contract> ServiceInfo getServiceInfo(Class<T> contract, String publicId) {
		return null;
	}

	public Contract getServiceInstance(String publicId) {
		return null;
	}

	public <T extends Contract> T getServiceInstance(Class<T> contract) {
//		if(SystemStateServiceContract.class.equals(contract)){
//			
//		} else {
//			return null;
//		}
		
		return null;
	}

	public <T extends Contract> T getServiceInstance(Class<T> contract, String publicId) {
		return null;
	}

}
