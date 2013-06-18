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
package com.cubeia.firebase.service.dosprotect;

import com.cubeia.firebase.api.service.dosprotect.FrequencyRule;

import junit.framework.TestCase;

public class ServiceImplTest extends TestCase {
	
	private static final String KEY = "key";
	private static final String ANOTHER_KEY = "another_key";
	private static final Object ID = "moi";
	private static final Object ANOTHER_ID = "toi";

	private ServiceImpl service;

	protected void setUp() throws Exception {
		service = new ServiceImpl();
		service.config(KEY, new FrequencyRule(10, 1000));
		service.setDelays(1000, 1000);
		service.start();
	}

	protected void tearDown() throws Exception {
		service.stop();
	}

	public void testFrequency1() throws Exception {
		/*
		 * Ten accesses should be allowed.
		 */
		for (int i = 0; i < 10; i++) {
			assertTrue(service.allow(KEY, ID));
		}
		/*
		 * But the eleventh should be blocked.
		 */
		assertFalse(service.allow(KEY, ID));
		
		// Anyone else should still be allowed.
		assertTrue(service.allow(KEY, ANOTHER_ID));
		
		/*
		 * Sleep to check cleanup...
		 */
		Thread.sleep(1100);
		
		/*
		 * Ten accesses should be allowed.
		 */
		for (int i = 0; i < 10; i++) {
			assertTrue(service.allow(KEY, ID));
		}
		/*
		 * But the eleventh should be blocked.
		 */
		assertFalse(service.allow(KEY, ID));
	}
	
	public void testNoRules() throws Exception {
		assertTrue(service.allow(ANOTHER_KEY, ID));
	}	
}
