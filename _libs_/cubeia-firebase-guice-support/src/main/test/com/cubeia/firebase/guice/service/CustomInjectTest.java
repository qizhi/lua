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

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistryAdapter;
import com.cubeia.firebase.guice.inject.FirebaseModule;
import com.cubeia.firebase.guice.inject.Log4j;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class CustomInjectTest {

	@Test
	public void testInjectLogger() {
		Injector i = Guice.createInjector(new FirebaseModule(new DummyReg()));
		Tester t = i.getInstance(Tester.class);
		Assert.assertTrue(t.hasLogger());
		Assert.assertTrue(t.hasService());
	}
	
	
	public static class Tester {
		
		@Log4j
		private Logger log;
		
		@com.cubeia.firebase.guice.inject.Service
		private DummyContract serv;
		
		public boolean hasLogger() {
			return log != null;
		}
		
		public boolean hasService() {
			return serv != null;
		}
	}
	
	public static class DummyReg extends ServiceRegistryAdapter {
		
		public DummyReg() {
			super.addImplementation(DummyContract.class, new DummyService());
		}
	}
	
	public static interface DummyContract extends Contract { }
	
	public static class DummyService implements Service, DummyContract {

		@Override
		public void destroy() { }

		@Override
		public void init(ServiceContext arg0) throws SystemException { }

		@Override
		public void start() { }

		@Override
		public void stop() { }
		
	}
}
