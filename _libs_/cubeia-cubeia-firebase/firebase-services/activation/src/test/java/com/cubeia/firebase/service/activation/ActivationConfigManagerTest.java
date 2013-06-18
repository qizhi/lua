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
package com.cubeia.firebase.service.activation;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceAdapter;
import com.cubeia.firebase.api.util.ConfigSourceListener;
import com.cubeia.firebase.util.ServiceContextAdapter;

public class ActivationConfigManagerTest extends TestCase {

	private ActivationConfigManagerImpl manager;

	@Override
	protected void setUp() throws Exception {
		manager = new ActivationConfigManagerImpl();
		manager.init(new ServiceContextAdapter());
		manager.start();
	}
	
	@Override
	protected void tearDown() throws Exception {
		manager.stop();
		manager.destroy();
	}
	
	
	// --- TEST METHODS --- //
	
	public void testListenAddRem() throws Exception {
		Listener list = new Listener();
		manager.addConfigSourceListener(list);
		/*
		 * Add sources
		 */
		ConfigSource src1 = new ConfigSourceAdapter("one");
		ConfigSource src2 = new ConfigSourceAdapter("one");
		manager.registerConfigSource(src1, ActivationType.GAR);
		manager.registerConfigSource(src2, ActivationType.TAR);
		/*
		 * Verify added 
		 */
		assertEquals(2, list.added.size());
		ActivationSource asrc1 = (ActivationSource)list.added.get(0);
		ActivationSource asrc2 = (ActivationSource)list.added.get(1);
		assertEquals(src1, asrc1.getWrappedSource());
		assertEquals(ActivationType.GAR, asrc1.getType());
		assertEquals(src2, asrc2.getWrappedSource());
		assertEquals(ActivationType.TAR, asrc2.getType());
		/*
		 * Remove sources
		 */
		manager.unregisterConfigSource("one", ActivationType.GAR);
		/*
		 * Check only one removed (despite same name)
		 */
		assertEquals(1, list.removed.size());
		ActivationSource asrc3 = (ActivationSource)list.removed.get(0);
		assertEquals(src1, asrc3.getWrappedSource());
		assertEquals(ActivationType.GAR, asrc1.getType());
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private static class Listener implements ConfigSourceListener {
		
		private List<ConfigSource> added = new LinkedList<ConfigSource>();
		private List<ConfigSource> modified = new LinkedList<ConfigSource>();
		private List<ConfigSource> removed = new LinkedList<ConfigSource>();
		
		public void sourceAdded(ConfigSource src) {
			added.add(src);
		}

		public void sourceModified(ConfigSource src) {
			modified.add(src);
		}

		public void sourceRemoved(ConfigSource src) {
			removed.add(src);
		}
	}
}
