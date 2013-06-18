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
package com.cubeia.firebase.api.util;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.junit.Assert;

public class TaskRepeaterTest extends TestCase {

	public void testTrivial() throws Exception {
		TaskRepeater r = new TaskRepeater("test", 1, 10);
		final AtomicInteger c = new AtomicInteger();
		r.execute(new Callable<Boolean>() {
			
			@Override
			public Boolean call() throws Exception {
				c.incrementAndGet();
				return true;
			}
		});
		Assert.assertEquals(1, c.intValue());
	}
	
	public void testSimple() throws Exception {
		TaskRepeater r = new TaskRepeater("test", 3, 10);
		final AtomicInteger c = new AtomicInteger();
		long time = System.currentTimeMillis();
		boolean res = r.execute(new Callable<Boolean>() {
			
			@Override
			public Boolean call() throws Exception {
				c.incrementAndGet();
				return false;
			}
		});
		assertEquals(3, c.intValue());
		assertEquals(res, false);
		long now = System.currentTimeMillis();
		assertTrue(now - time >= 30);
	}
	
	public void testClose() throws Exception {
		final TaskRepeater r = new TaskRepeater("test", 10, 1000);
		final AtomicInteger c = new AtomicInteger();
		final AtomicBoolean res = new AtomicBoolean(true);
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					boolean b = r.execute(new Callable<Boolean>() {
						
						@Override
						public Boolean call() throws Exception {
							c.incrementAndGet();
							return false;
						}
					});
					res.set(b);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		th.start();
		Thread.sleep(1200);
		r.close();
		th.join();
		assertEquals(2, c.intValue());
		assertEquals(false, res.get());
	}
}
