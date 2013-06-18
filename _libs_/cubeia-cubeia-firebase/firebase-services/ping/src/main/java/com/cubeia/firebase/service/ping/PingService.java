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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.Namespace;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.api.util.SecondCounter;
import com.cubeia.firebase.api.util.TimeCounter;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.instance.SystemCoreException;
import com.cubeia.firebase.util.executor.JmxScheduler;
import com.cubeia.util.threads.SafeRunnable;

public class PingService implements PingManager, Service, PingServiceMBean {
	
	private static final String JMX_NAME = "com.cubeia.firebase.service:type=PingManager";
	private static final String SCHEDULER_JMX_NAME = "PingManager";
	private static final String CONFIG_NS = "service.ping";

	private final Logger log = Logger.getLogger(getClass());
	
	private final Map<Session, Future<?>> pings = new ConcurrentHashMap<Session, Future<?>>();
	// private final Map<Session, Future> timeouts = new ConcurrentHashMap<Session, Future>();
	private final Map<Client, Session> sessions = new ConcurrentHashMap<Client, Session>();

	private ServiceContext con;
	private JmxScheduler scheduler;
	private PingConfiguration config;

	private boolean isEnabled = false;
	private long pingInterval;
	private long timeout;
	
	private final SecondCounter perSecond = new SecondCounter();
	private final TimeCounter respTime = new TimeCounter(1000);
	
	public void init(ServiceContext con) throws SystemException {
		this.con = con;
		setupConfiguration();
		setupConfigVars();
		initJmx();
	}

	private void setupConfigVars() {
		isEnabled = config.isPingEnabled();
		log.info("Ping service initialized; Enabled: " + isEnabled);
		timeout = config.getPingTimeout();
		pingInterval = config.getPingInterval();
		destroyJmx();
	}
	
	public void destroy() { }

	public void start() { 
		createScheduler();
	}

	public void stop() {
		destroyScheduler();
	}

	
	// --- PING MANAGER --- //
	
	public PingSession register(Client client) {
		if(isEnabled) {
			Session ses = new Session(this, client, config.getInitialPingDelay(), config.getFailureThreshold());
			this.sessions.put(client, ses);
			return ses;
		} else {
			return new NullSession();
		}
	}
	
	public void unregister(Client client) {
		if(isEnabled) {
			 Session session = sessions.remove(client);
			 if(session != null) {
				 session.close();
			 }
		}
	}
	
	
	// --- MBEAN METHODS --- //
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public int getActiveSessions() {
		int i = 0;
		for (Session ses : sessions.values()) {
			if(ses.isPinging()) {
				i++;
			}
		}
		return i;
	}
	
	public double getAvaragePingResponseTime() {
		return respTime.calculate();
	}
	
	public int getAvaragePingsPerSecond() {
		return perSecond.current();
	}
	
	public int getRegisteredSessions() {
		return sessions.size();
	}

	
	// --- PACKAGE METHODS --- //

	void registerResponse(long millis) {
		respTime.register(millis);
	}
	
	void cancelAll(Session session) {
		// cancelTimeout(session);
		cancelPing(session);
	}

	void cancelPing(Session session) {
		Future<?> future = pings.remove(session);
		if(future != null) {
			future.cancel(true);
		}
	}
	
	/*void cancelTimeout(Session session) {
		Future future = timeouts.remove(session);
		if(future != null) {
			future.cancel(true);
		}
	}*/
	
	void startPing(Session session, boolean immediate) {
		long initial = (immediate ? 0 : pingInterval);
		if(sessions.containsKey(session.getClient())) {
			pings.put(session, scheduler.scheduleAtFixedRate(new PingTask(session), initial, pingInterval, TimeUnit.MILLISECONDS));
		}
	}
	
	void scheduleTimeout(Session session, int id) {
		// timeouts.put(session, scheduler.schedule(new TimeoutTask(session, id), pingInterval, TimeUnit.MILLISECONDS));
		scheduler.schedule(new TimeoutTask(session, id), timeout, TimeUnit.MILLISECONDS);
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private void setupConfiguration() throws SystemCoreException {
		ClusterConfigProviderContract provider = con.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
		config = provider.getConfiguration(PingConfiguration.class, new Namespace(CONFIG_NS));
	}

	private void createScheduler() {
		if(isEnabled) {
			scheduler = new JmxScheduler(4, SCHEDULER_JMX_NAME);
		}
	}
	
	private void destroyScheduler() {
		if(scheduler != null) {
			scheduler.stop();
		}
	}
	
	private void destroyJmx() {
		MBeanServer serv = con.getMBeanServer();
		try {
			if(serv.isRegistered(new ObjectName(JMX_NAME))) {
				serv.unregisterMBean(new ObjectName(JMX_NAME));
			}
		} catch(Exception e) {
			log.error("Failed to unbind from JMX", e);
		}
	}
	
	private void initJmx() {
		MBeanServer serv = con.getMBeanServer();
		try {
			if(!serv.isRegistered(new ObjectName(JMX_NAME))) {
				serv.registerMBean(this, new ObjectName(JMX_NAME));
			}
		} catch(Exception e) {
			log.error("Failed to bind to JMX", e);
		}	
	}
	
	
	// --- PRIVATE CLASSES --- //
	
	private class PingTask extends SafeRunnable {
		
		private final Session session;

		private PingTask(Session session) {
			this.session = session;
		}

		public void innerRun() {
			int id = session.ping();
			scheduleTimeout(session, id);
			perSecond.register();
		}
	}
	
	private class TimeoutTask extends SafeRunnable {
		
		private final Session session;
		private final int id;

		private TimeoutTask(Session session, int id) {
			this.session = session;
			this.id = id;
		}

		public void innerRun() {
			// timeouts.remove(session);
			session.timeout(id);
		}
	}
}