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
package com.cubeia.firebase.service.clientreg.state;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.server.gateway.client.ClientState;
import com.cubeia.firebase.server.service.systemstate.SystemStateServiceContract;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;
import com.cubeia.firebase.service.clientreg.ClientReaper;
import com.cubeia.firebase.service.clientreg.ClientRegistry;
import com.cubeia.firebase.service.clientreg.ClientStatusFields;
import com.cubeia.util.threads.NamedThreadFactory;

/**
 * System State implementation of a Client Reaper.
 *
 * @author Fredrik
 */
public class StateClientReaper implements ClientReaper, StateClientReaperMBean {

	private transient Logger log = Logger.getLogger(this.getClass());

	/** Thread used for inspecting the client registry */
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ClientReaper"));
	
	/** The local client registry */
	private final ClientRegistry registry;


	/** TimeUnit used for scheduled inspection */
	private TimeUnit periodTimeUnit = TimeUnit.MILLISECONDS;

	/** Period for inspecting the client registry */
	private long period = 2000;

	/** Reconnect timeout. Default is 60 seconds */
	private long clientReconnectTimeout = 60000;

	/** This is the scheduled task for the inspection */
	private ScheduledFuture<?> inspectionFuture;

	/** We need direct access to the cache for better performance */
	private SystemStateCacheHandler cacheHandler;

	/** Total number of reaped clients */
	private AtomicLong reapedClients = new AtomicLong(0);
	private AtomicLong executionTime = new AtomicLong(0);

	/**
	 * Create a Client Reaper for the given system state and
	 * client registry.
	 * 
	 * @param registry
	 */
	public StateClientReaper(SystemStateServiceContract systemState, ClientRegistry registry) {
		this.registry = registry;
		cacheHandler = systemState.getCacheHandler();
		bindToJMX();
	}

	
	public long getInspectionPeriodMs() {
		return period;
	}
	
	/**
	 * Reset period time and restart scheduling
	 */
	public void setInspectionPeriodMs(long period) {
		setInspectionPeriod(TimeUnit.MILLISECONDS, period);
	}	

	/**
	 * Reset period time and restart scheduling
	 */
	public void setInspectionPeriod(TimeUnit timeUnit, long period) {
		this.periodTimeUnit = timeUnit;
		this.period = period;
		log.info("Restarting client reaper with new period: "+period+ " "+timeUnit.toString());
		stop();
		start();
	}

	public void setClientReconnectTimeout(long timeout) {
		log.debug("Reaper timeout set to: "+timeout);
		clientReconnectTimeout = timeout;
	}

	public long getClientReconnectTimeout() {
		return clientReconnectTimeout;
	}

	
	public void init(ServiceContext con) throws SystemException {}

	/**
	 * Start scheduling of inspection job
	 */
	public void start() {
		inspectionFuture = scheduler.scheduleWithFixedDelay(new Inspection(), period, period, periodTimeUnit);
	}

	/**
	 * Cancel the inspection job
	 */
	public void stop() {
		inspectionFuture.cancel(false);
	}

	/**
	 * Shutdown thread pool
	 */
	public void destroy() {
		scheduler.shutdownNow();
	}




	public long getExecutionTime() {
		return executionTime.get();
	}


	public long getReapedSessions() {
		return reapedClients.get();
	}


	/* (non-Javadoc)
	 * @see com.cubeia.firebase.service.clientreg.state.StateClientReaperMBean#runReaperNow()
	 */
	public long runReaperNow() {
		// The return count is not concurrency safe,
	    // but this method should only be used for debugging anyway
		long before = reapedClients.get();
		try {
			scheduler.submit(new Inspection()).get();
		} catch (Exception e) {
			log.debug("failed to wait for inspection", e);
		}
		long reaped = reapedClients.get() - before;
		return reaped;
	}
	
	public void forceDisconnects() {
		try {
			scheduler.submit(new ForcedReaping()).get();
		} catch (Exception e) {
			log.debug("failed to wait for forced reaping", e);
		}
	}

	
	/**
	 * Register the service to the JMX server
	 *
	 */
	private void bindToJMX() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName monitorName = new ObjectName("com.cubeia.firebase.clients:type=ClientReaper");
			mbs.registerMBean(this, monitorName);
		} catch (Exception ex) {
			log.warn("Could not bind Client Registry to JMX: "+ex);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void executeReaper(boolean forceDisconnect) {
		long start = System.currentTimeMillis();
		Cache cache = cacheHandler.getCache();
		Node root = cache.getRoot().getChild(Fqn.fromString(SystemStateConstants.CLIENT_ROOT_FQN));

		if (root == null) return; // EARLY RETURN, Root node not created yet

		Set<Node> children = root.getChildren();
		
		for (Node child : children) {
			// Surrounding this with a try catch, since Node is not thread safe. 
			try {
				// Make sure we have proper data
				if (isValidNodeWithData(child, ClientStatusFields.STATUS.name())) {
					String status = String.valueOf(child.getData().get(ClientStatusFields.STATUS.name()));
	
					if (status.equals(String.valueOf(ClientState.CONNECTED.ordinal())) || status.equals(String.valueOf(ClientState.LOGGED_IN.ordinal())) ) {
						// Do nothing. Checking CONNECTED or LOGGED_IN first prevents checking other possibilities
					} else if (status.equals(String.valueOf(ClientState.DISCONNECTED.ordinal()))) {
						checkRejoin(child, forceDisconnect);
					} 
				}
			} catch (Exception e) {
				// Catch all, see ticket #506. (client.getData() might return null after not returning null, see #441)
				log.debug("Reaper could not inspect client in cache, we will ignore this client this run. Reported exception: " + e);
			}
		}

		executionTime.set(System.currentTimeMillis() - start);
	}

	/**
	 * Checks if the node is valid and has the requested data.
	 * 
	 * @param child
	 * @param key the key that should be contained in the data
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean isValidNodeWithData(Node child, String key) {
		return child != null && child.getData() != null && child.isValid() && child.getData().containsKey(ClientStatusFields.STATUS.name());
	}
	
	/**
	 * Check timeouts for client waiting to reconnect.
	 * 
	 * @param child
	 */
	@SuppressWarnings("rawtypes")
	private void checkRejoin(Node child, boolean force) {
		int pid = Integer.parseInt(child.getFqn().getLastElementAsString());
		long waitTime = System.currentTimeMillis() - (Long)child.getData().get(ClientStatusFields.TIMESTAMP.toString());
		if (waitTime > clientReconnectTimeout || force) {
			log.debug("Client " + pid + " has timed out. Removing client.");
			registry.clientReaperTimeout(pid);
			reapedClients.incrementAndGet();
		}
	}
	
	/**
	 * Task for check clients for timeouts.
	 * The inspection performs a linear search for all clients in 
	 * registry and inspects the status.
	 * 
	 * This could probably get CPU intensive when the number of clients
	 * are rising. So, if this starts to eat CPU we should consider making
	 * the client reaper execute on a separate node.
	 *
	 * @author Fredrik
	 */
	private class Inspection implements Runnable {

		public void run() {
			try {
				executeReaper(false);
			} catch (Throwable th) {
				log.error("Failed to inspect client registry.", th);
			}
		}
		
	}

	private class ForcedReaping implements Runnable {

		public void run() {
			try {
				executeReaper(true);
			} catch (Throwable th) {
				log.error("Failed to inspect client registry.", th);
			}
		}
		
	}
	
}
