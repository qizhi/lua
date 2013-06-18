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
package com.cubeia.firebase.service.mcastgen;

import junit.framework.TestCase;

public class ServiceImplTest extends TestCase {

	//private ServiceContextAdapter context;
	//private MCastGenerationServiceImpl impl;
	
	@Override
	protected void setUp() throws Exception {
		//context = new ServiceContextAdapter();
	}

	public void testConfigured() throws Exception {
		/*ServiceContextAdapter.HAVE_VALUE = true;
		GeneratorConfigMock.HAVE_VALUE = true;
		impl = new MCastGenerationServiceImpl();
		impl.init(context);
		// test with 'mcastgen-conf-test.conf'
		SocketAddress test = impl.getGeneratedAddress("service1");
		assertEquals(new SocketAddress("224.223.0.16:6667"), test);
		// test unknown service
		SocketAddress test2 = impl.getGeneratedAddress("unknown");
		assertEquals(new SocketAddress("224.224.50.2:8901"), test2);*/
	}
	
	public void testConfigDir() throws Exception {
		/*ServiceContextAdapter.HAVE_VALUE = true;
		GeneratorConfigMock.HAVE_VALUE = false;
		impl = new MCastGenerationServiceImpl();
		impl.init(context);
		// test with 'mcastgen.conf'
		SocketAddress test = impl.getGeneratedAddress("service1");
		assertEquals(new SocketAddress("224.223.0.25:6676"), test);*/
	}
}
