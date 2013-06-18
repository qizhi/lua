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
package com.cubeia.test.systest.game;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.test.systest.dao.ServiceCount;
import com.cubeia.test.systest.game.tests.ActivatorServiceRouteProcessor;
import com.cubeia.test.systest.game.tests.BroadcastProcessor;
import com.cubeia.test.systest.game.tests.GameObjectRouteProcessor;
import com.cubeia.test.systest.game.tests.GarResourceProcessor;
import com.cubeia.test.systest.game.tests.QueryProcessor;
import com.cubeia.test.systest.game.tests.QuickSeatProcessor;
import com.cubeia.test.systest.io.protocol.ActivatorGameRoutePacket;
import com.cubeia.test.systest.io.protocol.ActivatorTestRequestPacket;
import com.cubeia.test.systest.io.protocol.GarResourceRequestPacket;
import com.cubeia.test.systest.io.protocol.ProtocolObjectFactory;
import com.cubeia.test.systest.io.protocol.QuickSeatRequest;
import com.cubeia.test.systest.io.protocol.ServiceBroadcastRequestPacket;
import com.cubeia.test.systest.io.protocol.ServiceQueryRequestPacket;

public class SystestServiceImpl implements SystestService, Service, RoutableService {
	
	// private final Logger log = Logger.getLogger(getClass());

	private static final AtomicInteger C = new AtomicInteger(1);
	private static final int PLAYER_ID = 667;
	
	private ServiceRouter router;
	private ServiceContext con;
	
	private EntityManagerFactory factory;

	public void destroy() { 
		checkContextClassLoader();
	}

	public void init(ServiceContext con) throws SystemException {
		factory = Persistence.createEntityManagerFactory("systest");
		doCheckContextClassLoader();
		this.con = con;
		createCount();
		verifyAndIncrement();
	}

	public void start() { 
		doCheckContextClassLoader();
	}
	
	@Override
	public void checkContextClassLoader() {
		doCheckContextClassLoader();
		checkJndiAccess();
	}

	private void doCheckContextClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null || !cl.equals(getClass().getClassLoader())) {
			throw new IllegalStateException("Found class loader: " + cl + "; Expected: " + getClass().getClassLoader());
		}
	}

	public void stop() { 
		doCheckContextClassLoader();
	}
	
	public void onAction(ServiceAction e) {
		verifyAndIncrement();
		checkContextClassLoader();
		ProtocolObject packet = getPacket(e);
		if(packet instanceof GarResourceRequestPacket) {
			new GarResourceProcessor().doTest(con, router, packet, e.getAttributes());
		} else if(packet instanceof ServiceBroadcastRequestPacket) {
			new BroadcastProcessor().doTest(con, router, packet, e.getAttributes());
		} else if(packet instanceof ActivatorTestRequestPacket) {
			new ActivatorServiceRouteProcessor().doTest(con, router, packet, e.getAttributes());
		} else if(packet instanceof ServiceQueryRequestPacket) {
			new QueryProcessor().doTest(con, router, packet, e.getAttributes());
		} else if(packet instanceof QuickSeatRequest) {
		      new QuickSeatProcessor().doTest(con, router, packet, e.getAttributes());
		} else if(packet instanceof ActivatorGameRoutePacket) {
		      new GameObjectRouteProcessor().doTest(con, router, packet, e.getAttributes());
		}
	}

	public void setRouter(ServiceRouter router) {
		doCheckContextClassLoader();
		this.router = router;
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void createCount() {
		EntityManager em = factory.createEntityManager();
		EntityTransaction tr = em.getTransaction();
		tr.begin();
		try {
			// log.info("Creating count at 1 for service " + PLAYER_ID);
			ServiceCount c = new ServiceCount();
			c.setCount(1);
			c.setPlayerId(PLAYER_ID);
			em.persist(c);
			tr.commit();
		} finally {
			em.close();
		}
	}

	private void verifyAndIncrement() {
		EntityManager em = factory.createEntityManager();
		EntityTransaction tr = em.getTransaction();
		tr.begin();
		try {
			// log.info("Expecting count " + C.get() + " for service " + PLAYER_ID);
			ServiceCount c = em.find(ServiceCount.class, PLAYER_ID);
			if(c.getCount() != C.get()) {
				throw new IllegalStateException("Expected " + C.get() + ", found " + c.getCount());
			}
			C.incrementAndGet();
			c.increaseCount();
			tr.commit();
		} finally {
			em.close();
		}
	}
	
	private ProtocolObject getPacket(ServiceAction e) {
		byte[] data = e.getData();
		// try {
			return new StyxSerializer(new ProtocolObjectFactory()).unpack(ByteBuffer.wrap(data));
		/*} catch (IOException ex) {
			Logger.getLogger(getClass()).error("Failed to unmarchal packet", ex);
			return null;
		}*/
	}
	
	private void checkJndiAccess() {
		try {
			InitialContext con = new InitialContext();
			Object o = con.lookup("java:comp/env/jdbc/test");
			DataSource src = (DataSource) o;
			if(src == null) {
				throw new IllegalStateException("Did not find data source java:comp/env/jdbc/test");
			}
		} catch(NameNotFoundException e) {
			throw new IllegalStateException("Did not find data source java:comp/env/jdbc/test");
		} catch (NamingException e) {
			throw new IllegalStateException("Failed to lookup data source java:comp/env/jdbc/test", e);
		}
	}
}
