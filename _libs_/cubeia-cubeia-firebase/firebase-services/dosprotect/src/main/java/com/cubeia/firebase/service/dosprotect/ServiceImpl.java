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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.dosprotect.ChainImpl;
import com.cubeia.firebase.api.service.dosprotect.DosProtector;
import com.cubeia.firebase.api.service.dosprotect.Rule;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.util.threads.SafeRunnable;

public class ServiceImpl implements DosProtector, Service {

	private ScheduledExecutorService cleaner;
	private final Map<String, Chainer> chains = new ConcurrentHashMap<String, Chainer>();
	private long initDelay = 120000;
	private long delay = 60000;
	
	public boolean allow(String key, Object callerId) {
		Arguments.notNull(key, "key");
		Arguments.notNull(callerId, "caller id");
		Chainer ch = getChainer(key);
		return ch.next(callerId);
	}

	public void config(String key, Rule... rules) {
		Arguments.notNull(key, "key");
		createChainer(key, rules);
	}
	
	// --- PRIVATE METHODS --- //
	
	private Chainer getChainer(String key) {
		Chainer ch = chains.get(key);
		if (ch == null) {
			ch = createChainer(key);
		}
		return ch;
	}

	private synchronized Chainer createChainer(String key, Rule...rules) {
		if (chains.containsKey(key)) {
			return chains.get(key);
		} else {
			Chainer ch = new Chainer(rules);
			chains.put(key, ch);
			return ch;
		}
	}
	
	// --- SERVICE METHODS --- //

	public void destroy() { }

	public void init(ServiceContext con) throws SystemException { }

	public void start() { 
		cleaner = Executors.newSingleThreadScheduledExecutor();
		cleaner.scheduleWithFixedDelay(new SafeRunnable() {
		
			public void innerRun() {
				for (Chainer ch : chains.values()) {
					if(ch.rules != null) {
						for (Rule r : ch.rules) {
							r.cleanup();
						}
					}
				}
			}
			
		}, initDelay , delay , TimeUnit.MILLISECONDS);
	}

	public void stop() { 
		if (cleaner != null) {
			cleaner.shutdown();
			cleaner = null;
		}
	}
	
	// --- TEST METHODS --- //
	
	void setDelays(long init, long del) {
		this.initDelay = init;
		this.delay = del;
	}
	
	// --- INNER CLASSES --- //
	
	private static class Chainer {
		
		private final Rule[] rules;

		public Chainer(Rule... rules) {
			this.rules = rules;
		}

		public boolean next(Object callerId) {
			return new ChainImpl(rules).next(callerId);
		}
	}

}
