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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceContextAdapter;
import com.google.inject.Inject;

public class GuiceServiceTest {

	@Test
	public void testSimple() throws SystemException {
		TestGuiceService s = new TestGuiceService();
		s.init(new ServiceContextAdapter());
		Assert.assertEquals(s.echo("kalle"), "kalle");
		Assert.assertTrue(s.sameSame());
	}
	
	
	
	
	public static interface TestContract extends Contract {
		
		public String echo(String s);
	
	}
	
	public static class TestService implements TestContract {
		
		@Inject
		private ServiceContext context;

		@Override
		public String echo(String s) {
			assert(context != null);
			return s;
		}
	
	}
	
	public static class TestGuiceService extends GuiceService implements TestContract {
		
		@Override
		public String echo(String s) {
			return guice(TestContract.class).echo(s);
		}
		
		public boolean sameSame() {
			return guice(TestContract.class) == guice(TestContract.class);
		}
		
		@Override
		public Configuration getConfigurationHelp() {
			return new Configuration() {
				
				@Override
				public ContractsConfig getServiceContract() {
					return new ContractsConfig(TestService.class, TestContract.class);
				}
			};
		}
	}
}
