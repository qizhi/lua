/**
 * Copyright (C) 2010 Cubeia Ltd info@cubeia.com
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.cubeia.firebase.guice.inject;

import java.lang.reflect.Field;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * This is the type listener for Firebase {@link Service services} 
 * for the Firebase Guice support.
 * 
 * @author larsan
 */
public class ServiceTypeListener implements TypeListener {
	
	private final ServiceRegistry reg;
	
	public ServiceTypeListener(ServiceRegistry reg) {
		this.reg = reg;
	}

	public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
		for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
			if (Contract.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(Service.class)) {
				typeEncounter.register(new ServiceMembersInjector<T>(field, reg));
			}
		}
	}
}