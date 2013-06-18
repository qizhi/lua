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
package com.cubeia.firebase.api.service.dosprotect;

import java.util.concurrent.atomic.AtomicInteger;

import com.cubeia.firebase.api.service.dosprotect.ChainImpl;
import com.cubeia.firebase.api.service.dosprotect.FrequencyRule;
import com.cubeia.firebase.api.service.dosprotect.RuleChain;

import junit.framework.TestCase;

public class FrequencyRuleTest extends TestCase {

	private static final Object KEY = "moi";

	public void testFrequency1() throws Exception {
		FrequencyRule r = new FrequencyRule(10, 1000);
		
		/*
		 * Ten accesses should be allowed.
		 */
		for (int i = 0; i < 10; i++) {
			assertTrue(chain(r).next(KEY));
		}
		/*
		 * But the eleventh should be blocked
		 */
		assertFalse(chain(r).next(KEY));
		
		/*
		 * Sleep to check cleanup...
		 */
		Thread.sleep(1100);
		
		/*
		 * Ten accesses should be allowed.
		 */
		for (int i = 0; i < 10; i++) {
			assertTrue(chain(r).next(KEY));
		}
		/*
		 * But the eleventh should be blocked
		 */
		assertFalse(chain(r).next(KEY));
	}
	
	public void testFrequency2() throws Exception {
		/*
		 * Check that we can do "only once every 500ms"
		 */
		FrequencyRule r = new FrequencyRule(1, 500);
		assertTrue(chain(r).next(KEY));
		assertFalse(chain(r).next(KEY));
		/*
		 * Sleep to check cleanup...
		 */
		Thread.sleep(510);
		assertTrue(chain(r).next(KEY));
	}
	
	public void testCleanup1() throws Exception {
		FrequencyRule r = new FrequencyRule(10, 1000);
		assertTrue(chain(r).next(KEY));
		
		/*
		 * Sleep to check cleanup...
		 */
		Thread.sleep(1100);
		r.cleanup();
		
		/*
		 * We should be clean here...
		 */
		assertEquals(0, r.size());
	}
	
	public void testMultipleRules() {
		AtomicInteger c = new AtomicInteger();
		TrivialRule r1 = new TrivialRule(c);
		TrivialRule r2 = new TrivialRule(c);
		assertTrue(new ChainImpl(r1, r2).next("moi"));
		assertEquals(2, c.get());
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private RuleChain chain(FrequencyRule r) {
		return new ChainImpl(r);
	}
}
