/**
 * Copyright (C) 2011 Cubeia Ltd info@cubeia.com
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

package com.cubeia.firebase.service.random.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceContextAdapter;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.ServiceRegistryAdapter;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContractAdapter;
import com.cubeia.firebase.service.random.api.RandomServiceConfig;

public class ServiceImplTest {

	@Test
	public void testDiscardedDrawEnabled() throws Exception {
		ServiceImpl service = new ServiceImpl();
		service.init(createContext(new DiscardedDrawConfig()));
		service.start();
		RandomWrapper random = (RandomWrapper) service.getSystemDefaultRandom();
		Object wrapped = ReflectionTestUtils.getField(random, "wrapped");
		assertTrue(wrapped instanceof DiscardedDrawRandom);
		service.stop();
		service.destroy();
	}
	
	@Test
	public void testBackgroundPollingEnabled() throws Exception {
		ServiceImpl service = new ServiceImpl();
		service.init(createContext(new ConfigAdapter()));
		RandomWrapper random = (RandomWrapper) service.getSystemDefaultRandom();
		Object wrapped = ReflectionTestUtils.getField(random, "wrapped");
		assertTrue(wrapped instanceof BackgroundPollingRandom);
		Assert.assertFalse(((BackgroundPollingRandom) wrapped).isStarted());
		service.start();
		Assert.assertTrue(((BackgroundPollingRandom) wrapped).isStarted());
		service.stop();
		Assert.assertFalse(((BackgroundPollingRandom) wrapped).isStarted());
		service.destroy();
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private ServiceContext createContext(ConfigAdapter config) {
		ServiceContextAdapter ada = new ServiceContextAdapter();
		ada.setParentRegistry(createConfigRegistry(config));
		return ada;
	}
	
	private ServiceRegistry createConfigRegistry(ConfigAdapter config) {
		ServiceRegistryAdapter ada = new ServiceRegistryAdapter();
		ClusterConfigProviderContractAdapter prov = new ClusterConfigProviderContractAdapter();
		prov.addConfiguration(RandomServiceConfig.class, config);
		ada.addImplementation(ClusterConfigProviderContract.class, prov);
		return ada;
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class DiscardedDrawConfig extends ConfigAdapter {
		
		@Override
		public boolean enableBackgroundPolling() {
			return false;
		}
		
		@Override
		public boolean enableDiscardedDraw() {
			return true;
		}
	}
}
