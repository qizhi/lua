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
package com.cubeia.firebase.service.ping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.cubeia.firebase.server.gateway.client.Client;

public class Session implements PingSession {

	private final PingService service;
	private final Client client;
	
	private final Logger log = Logger.getLogger(getClass());
	
	private final AtomicInteger idCount = new AtomicInteger(0);
	private final AtomicInteger lastPing = new AtomicInteger(-1);
	private final Map<Integer, Long> pings = new HashMap<Integer, Long>();

	private final AtomicLong dataTimestamp = new AtomicLong(System.currentTimeMillis());

	private int failureCount = 0;
	private final int threshold;
	private final long maxIdle;
	
	public Session(PingService service, Client client, long maxIdle, int threshold) {
		this.service = service;
		this.client = client;
		this.maxIdle = maxIdle;
		this.threshold = threshold;
	}
	
	public Client getClient() {
		return client;
	}
	
	public void reportIdle() {
		if(!isPinging()) {
			service.startPing(this, true);
			if(log.isDebugEnabled()) {
				log.debug("Session report idle, starting ping; Session: " + client.getSessionId() + "; Client: " + client.getId());
			}
		}
	}
	
	public long getMaxIdleTime() {
		return maxIdle;
	}

	public boolean isPinging() {
		return lastPing.get() != -1;
	}
	
	public long getLastDataTimestamp() {
		return dataTimestamp.get();
	}
	
	public synchronized void close() {
		dataReceived();
	}

	public synchronized void dataReceived() {
		dataTimestamp.set(System.currentTimeMillis());
		if(isPinging()) {
			failureCount = 0;
			service.cancelAll(this);
			lastPing.set(-1);
			pings.clear();
		}
	}

	public synchronized void pingReceived(int id) {
		if(isPinging() && pings.containsKey(id)) {
			long time = System.currentTimeMillis() - pings.remove(id).longValue();
			checkTracePingReceivedTime(id, time);
			service.registerResponse(time);
		}
	}

	
	// --- PACKAGE METHODS --- //
	
	synchronized int ping() {
		int id = idCount.incrementAndGet();
		long time = System.currentTimeMillis();
		pings.put(id, time);
		client.sendPing(id);
		lastPing.set(id);
		return id;
	}

	synchronized void timeout(int id) {
		if(isPinging() && pings.containsKey(id)) {
			failureCount++;
			pings.remove(id);
			if(failureCount >= threshold) {
				log.debug("Final ping timeout; Session: " + client.getSessionId() + "; Client: " + client.getId() + "; Failure count: " + failureCount);
				client.pingDisconnect();
				close();
			} else {
				if(log.isTraceEnabled()) {
					log.trace("Non-final ping timeout; Session: " + client.getSessionId() + "; Client: " + client.getId() + "; Failure count: " + failureCount);
				}
			}
		} 
	}
	
	
	// --- PRIVATE METhODS --- //
	
	private void checkTracePingReceivedTime(int id, long time) {
		if(log.isTraceEnabled()) {
			log.trace("Ping received; Session: " + client.getSessionId() + "; Client: " + client.getId() + "; Roundtip time (ms): " + time);
		}
	}
}
