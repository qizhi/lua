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

import java.lang.reflect.Proxy;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistryAdapter;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestFirebaseModule {

	@Test
	public void testProxy() {
		Ref r = new Ref();
		Injector i = Guice.createInjector(new FirebaseModule(r));
		ProxyTester t = i.getInstance(ProxyTester.class);
		Assert.assertNotNull(t.service);
		Assert.assertFalse(r.isCalled);
		t.service.apa();
		Assert.assertTrue(r.isCalled);
		Assert.assertTrue(Proxy.isProxyClass(t.service.getClass()));
	}
	
	@Test
	public void testNotProxy() {
		Ref r = new Ref();
		Injector i = Guice.createInjector(new FirebaseModule(r));
		NotProxyTester t = i.getInstance(NotProxyTester.class);
		Assert.assertNotNull(t.service);
		Assert.assertTrue(r.isCalled);
		Assert.assertFalse(Proxy.isProxyClass(t.service.getClass()));
	}
	
	private static class ProxyTester {
		
		@com.cubeia.firebase.guice.inject.Service(proxy=true)
		private EmtpyContract service;
		
	}
	
	private static class NotProxyTester {
		
		@com.cubeia.firebase.guice.inject.Service
		private EmtpyContract service;
		
	}
	
	private static class Ref extends ServiceRegistryAdapter {
		
		private boolean isCalled = false;
		
		public Ref() {
			super.addImplementation(EmtpyContract.class, new EmtpyService());
		}
		
		@Override
		public <T extends Contract> T getServiceInstance(Class<T> contract) {
			isCalled = true;
			return super.getServiceInstance(contract);
		}
	}
	
	public static interface EmtpyContract extends Contract { 
		
		public void apa();
		
	}
	
	public static class EmtpyService implements EmtpyContract, Service {

		@Override
		public void destroy() { }

		@Override
		public void init(ServiceContext arg0) throws SystemException { }

		@Override
		public void apa() { }
		
		@Override
		public void start() { }

		@Override
		public void stop() { }
		
	}
}
