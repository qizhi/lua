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
package com.cubeia.test.systest.game.impl;

import static com.cubeia.test.systest.game.impl.Handler.CHECK;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.test.systest.dao.ServiceCount;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ServiceImpl implements ServiceContract {
	
	// private final Logger log = Logger.getLogger(getClass());
	
	private static final AtomicInteger C = new AtomicInteger();
	private static final int PLAYER_ID = 666;
	
	@Inject
	private Provider<EntityManager> em;
	
	@Transactional
	public void init(ServiceContext con) throws SystemException { 
		createCount();
		checkProxyAccess();
	}

	@Transactional
	public void start() { 
		verifyAndIncrement();
		checkProxyAccess();
	}

	@Override
	@Transactional
	public void checkProxyAccess() {
		verifyAndIncrement();
		doCheckproxy();
	}
	
	@Transactional
	public void stop() {
		// verifyAndIncrement(2);
		doCheckproxy();
	}
	
	@Transactional
	public void destroy() {
		// verifyAndIncrement(3);
		doCheckproxy();
	}
	
	// --- PRIVATE METHODS --- //
	
	private void doCheckproxy() {
		if(!CHECK.get().get()) {
			throw new IllegalStateException("Proxied service accessed outside of invocation handler!");
		}
	}
	
	private void createCount() {
		// log.info("Creating count at 1 for service " + PLAYER_ID);
		ServiceCount c = new ServiceCount();
		c.setCount(1);
		c.setPlayerId(PLAYER_ID);
		em.get().persist(c);
		C.incrementAndGet();
	}

	private void verifyAndIncrement() {
		// log.info("Expecting count " + C.get() + " for service " + PLAYER_ID);
		ServiceCount c = em.get().find(ServiceCount.class, PLAYER_ID);
		if(c.getCount() != C.get()) {
			throw new IllegalStateException("Expected " + C.get() + ", found " + c.getCount());
		}
		C.incrementAndGet();
		c.increaseCount();
	}
}