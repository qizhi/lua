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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SecondCounter {
	
	private final AtomicInteger last = new AtomicInteger();
	private final AtomicInteger messages = new AtomicInteger();
	
	private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
	
	/**
	 * Added synchronization to avoid DCL issues.
	 * Until we see this emerge as a bottleneck I will keep it there.
	 * Arguably, accurate data is more important then raw performance
	 * in this stage of product development (1.0 RC1)
	 *
	 */
	public synchronized void register() {
		long t = System.currentTimeMillis();
		if(startTime.get() + 1000 < t) {
			checkLimit(t);
		}
		messages.incrementAndGet();
	}
	
	/**
	 * DCL going on here. However, the atomic long should
	 * ensure memory barriers. And if we do fail on DCL, so be it,
	 * it is still way better then having multi threaded failure
	 * in almost 20% of all cases.
	 */
	private synchronized void checkLimit(long t) {
		if(startTime.get() + 1000 < t) {
			last.set(messages.getAndSet(0));
			startTime.set(t);
		}
	}
	
	public int current() {
		return last.get();
	}
}
