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

package com.cubeia.firebase.guice.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceContextAdapter;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class GuiceServiceHandlerTest {
	
	public static DummyContract contract(InvocationHandler h) {
		return (DummyContract) Proxy.newProxyInstance(GuiceServiceHandlerTest.class.getClassLoader(), new Class[] { DummyContract.class }, h);
	}

	@Test
	public void simpleEchoTest() throws Exception {
		DummyHandler h = new DummyHandler();
		DummyContract c = contract(h);
		h.init(new ServiceContextAdapter());
		Assert.assertEquals("hello", c.echo("hello"));
	}
	
	@Test
	public void simpleEchoTestNoConfig() throws Exception {
		DummyHandlerNoConfig h = new DummyHandlerNoConfig();
		DummyContract c = contract(h);
		h.init(new ServiceContextAdapter());
		Assert.assertNull(c.echo("hello"));
	}
	
	@Test
	public void simpleEchoTestNoConfigSelection() throws Exception {
		DummyHandlerNoConfig h = new DummyHandlerNoConfig() {
			
			@Override
			protected void preInjectorCreation(ServiceContext context, List<Module> modules) {
				modules.add(new AbstractModule() {
					
					@Override
					protected void configure() {
						bind(DummyContract.class).to(DummyService.class);
					}
				});
			}
			
			protected java.lang.Class<?> findClassForMethod(java.lang.reflect.Method method, Object[] args) throws Throwable {
				return DummyService.class;
			}
		};
		DummyContract c = contract(h);
		h.init(new ServiceContextAdapter());
		Assert.assertEquals("hello", c.echo("hello"));
	}
}
