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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This is a random wrapper that adds background polling to a given random. It
 * will poll the underlying implementation for 0-N integers (of 1 bit) every Y 
 * milliseconds.
 * 
 * @author Lars J. Nilsson
 */
public class BackgroundPollingRandom extends RandomWrapper implements InternalRandom {
	
	private static final long serialVersionUID = 1L;

	private final int DISCARDED_BITS = 1; // draws an INT from the sequence anyway

	private final SecureRandom random = new SecureRandom();
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

	private final long interval;
	private final int maxDiscard;
	
	private boolean isStarted;
	
	/**
	 * @param wrapped Underlying random to use, must not be null
	 * @param interval Interval between polls in milliseconds
	 * @param maxDiscard Max (N) discarded integers per poll, must be positive
	 */
	BackgroundPollingRandom(InternalRandom wrapped, long interval, int maxDiscard) {
		super(wrapped);
		this.interval = interval;
		this.maxDiscard = maxDiscard;
	}
	
	/**
	 * Call to start the internal thread which poll in the background.
	 */
	void start() {
		exec.scheduleWithFixedDelay(new Task(), interval, interval, MILLISECONDS);
		isStarted = true;
	}
	
	/**
	 * @return True if the random is started, false otherwise
	 */
	boolean isStarted() {
		return isStarted;
	}
	
	@Override
	public int next(int bits) {
		return super.next(bits);
	}
	
	/**
	 * Stop the background polling.
	 */
	void stop() {
		exec.shutdown();
		isStarted = false;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private class Task implements Runnable {
		
		@Override
		public void run() {
			doDiscard();
		}

		private void doDiscard() {
			int len = random.nextInt(maxDiscard);
			for (int i = 0; i < len; i++) {
				next(DISCARDED_BITS);
			}
		}
	}
}
