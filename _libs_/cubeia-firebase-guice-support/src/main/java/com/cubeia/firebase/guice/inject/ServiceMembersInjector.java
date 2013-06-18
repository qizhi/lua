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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.google.inject.MembersInjector;

/**
 * This is the actual injector for Firebase {@link Service services} 
 * for the Firebase Guice support.
 * 
 * @author larsan
 */
@SuppressWarnings("unchecked")
public class ServiceMembersInjector<T> implements MembersInjector<T> {

	private final Field field;
	private final ServiceRegistry reg;
	private final boolean proxy;

	/**
	 * @param field Field to inject, must not be null
	 * @param reg Service registry, must not be null
	 */
	@SuppressWarnings("rawtypes")
	ServiceMembersInjector(Field field, ServiceRegistry reg) {
		this.field = field;
		this.reg = reg;
		field.setAccessible(true);
		Service a = field.getAnnotation(Service.class);
		proxy = a.proxy();
		Class t = field.getType();
		if(!Contract.class.isAssignableFrom(t)) {
			throw new IllegalArgumentException("Illegal annotation, type " + t.getName() + " is not a service Contract");
		}
	}

	public void injectMembers(T t) {
		try {
			if(proxy) {
				field.set(t, newProxy());
			} else {
				field.set(t, getService());
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Object getService() {
		Object o = reg.getServiceInstance((Class<? extends Contract>)field.getType());
		if(o == null) {
			Logger.getLogger(getClass()).warn("Injected service of contract " + field.getType().getName() + " does not exist!");
		}
		return o;
	}
	
	private Object newProxy() {
		return Proxy.newProxyInstance(
							getClass().getClassLoader(), 
							new Class<?>[] { field.getType() }, 
							new Handler());
	}
	
	// --- INNER CLASSES --- //
	
	private class Handler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object o = getService();
			if(o != null) {
				return method.invoke(o, args);
			} else {
				Logger.getLogger(getClass()).error("Proxied and injected service of contract " + field.getType().getName() + " does not exist!");
				return null;
			}
		}
	}
}