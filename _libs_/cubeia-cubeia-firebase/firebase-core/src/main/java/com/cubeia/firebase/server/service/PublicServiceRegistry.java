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
package com.cubeia.firebase.server.service;

import java.lang.annotation.Annotation;
import java.util.List;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.util.Arguments;

/**
 * This service registry wraps another registry, but only exposes
 * services that are "public", ie. ethier trusted services that have
 * exported their interface or isolated services.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 29
 */
public class PublicServiceRegistry implements ServiceRegistry {

	private final InternalServiceRegistry parent;
	
	public PublicServiceRegistry(InternalServiceRegistry parent) {
		Arguments.notNull(parent, "parent");
		this.parent = parent;
	}
	
	public ServiceInfo getServiceInfo(String publicId) {
		Arguments.notNull(publicId, "publicId");
		if(!parent.isPublic(publicId)) return null;
		else return parent.getServiceInfo(publicId);
	}
	
	@Override
	public <T extends Contract> List<ServiceInfo> listServicesByContract(Class<T> contract) {
		Arguments.notNull(contract, "contract");
		return parent.listPublicServices(contract);
	}
	
	@Override
	public <T extends Annotation> List<ServiceInfo> listServicesByAnnotation(Class<T> annotation) {
		Arguments.notNull(annotation, "annotation");
		return parent.listPublicServicesByAnnotation(annotation);
	}

	public <T extends Contract> ServiceInfo getServiceInfo(Class<T> contract, String publicId) {
		Arguments.notNull(contract, "contract");
		if(!parent.isPublic(contract, publicId)) return null;
		else return parent.getServiceInfo(contract, publicId);
	}

	public Contract getServiceInstance(String publicId) {
		Arguments.notNull(publicId, "publicId");
		if(!parent.isPublic(publicId)) return null;
		else return parent.getServiceInstance(publicId);
	}

	public <T extends Contract> T getServiceInstance(Class<T> contract) {
		Arguments.notNull(contract, "contract");
		if(!parent.isPublic(contract, null)) return null;
		else return parent.getServiceInstance(contract);
	}

	public <T extends Contract> T getServiceInstance(Class<T> contract, String publicId) {
		Arguments.notNull(contract, "contract");
		if(!parent.isPublic(contract, publicId)) return null;
		else return parent.getServiceInstance(contract, publicId);
	}
}
