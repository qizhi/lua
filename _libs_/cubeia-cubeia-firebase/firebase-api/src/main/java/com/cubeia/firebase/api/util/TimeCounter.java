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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TimeCounter {
	
	private final Queue<Long> samples = new ConcurrentLinkedQueue<Long>();
	private final int windowSize;
	
	public TimeCounter(int windowSize) {
		this.windowSize = windowSize;
	}
	
	public void register(long millis) {
		//lock.lock();
		try {
			if(samples.size() >= windowSize) {
				samples.remove();
			}
			samples.add(millis);
		} finally {
			//lock.unlock();
		}
	}
	
	public double calculate() {
		//lock.lock();
		try {
			if(samples.size() == 0) return -1;
			else {
				long sum = 0;
				for (Long l : samples) {
					sum += l.longValue();
				}
				return sum / samples.size();
			}
		} finally {
			//lock.unlock();
		}
	}
}
