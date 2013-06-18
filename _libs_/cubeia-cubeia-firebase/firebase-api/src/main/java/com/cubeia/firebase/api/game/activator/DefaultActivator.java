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
package com.cubeia.firebase.api.game.activator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.common.AttributeValue;
import com.cubeia.firebase.api.common.AttributeValue.Type;
import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.game.lobby.LobbyTable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.api.util.ConfigSource;
import com.cubeia.firebase.api.util.ConfigSourceListener;

/**
 * This is the default implementation of a game activator used
 * by Firebase if no other activator is found. It can be configured with
 * an XML file in the deployment folder. 
 * 
 * @author lars.j.nilsson
 */
public class DefaultActivator implements GameActivator {

	private final Logger log;
	private final Object threadLock = new Object();
	private final AtomicReference<DefaultActivatorConfig> CONF;
	private final AtomicReference<CreationParticipant> PART;
	
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> future;
	// private ActivatorContext context;
	protected TableFactory tableRegistry;
	
	public DefaultActivator() {
		PART = new AtomicReference<CreationParticipant>(new DefaultCreationParticipant());
		log = Logger.getLogger(getClass());
		CONF = new AtomicReference<DefaultActivatorConfig>();
	}
	
	public void destroy() {
		destroyThreading();
	}

	public void init(ActivatorContext context) throws SystemException {
		// this.context = context;
		tableRegistry = context.getTableFactory();
		ConfigSource source = context.getConfigSource();
		if(source == null) {
			log.info("Default activator for game id " + context.getGameId() + " is used the default configuration");
			CONF.set(new DefaultActivatorConfig());
		} else {
			log.info("Default activator for game id " + context.getGameId() + " is used the configuration from source: " + source);
			CONF.set(new DefaultActivatorConfig(source));
		}
		context.setConfigSourceListener(new Listener());
		setupThreading();
		initTables();
	}

	public void start() {
		startThreading();
	}

	public void stop() {
		stopThreading();
	}
	
	public void doModified(ConfigSource src) {
		try {
			CONF.set(new DefaultActivatorConfig(src));
			changeThreads();
		} catch (SystemException e) {
			log.error("Failed to reload configuration from source; Received message: " + e.getMessage(), e);
		}
	}
	
	// --- PROTECTED METHODS --- //
	
	/**
	 * @param part A new creation participant to use, must not be null
	 */
	protected void setCreationParticipant(CreationParticipant part) {
		Arguments.notNull(part, "part");
		PART.set(part);
	}
	
	/**
	 * Check all tables in the lobby which are of interest.
	 * 
	 * If you need to create tables you need to implement your
	 * own create tables-method. You can then create the table 
	 * by calling:
	 * <br/>
	 * <code>tableRegistry.createTable(int seats, CreationParticipant part);</code>
	 * <br/>
	 * You can override this if you want to provide your own
	 * checking and creation of tables.
	 *
	 */
	protected void checkTables() {
		LobbyTable[] tables = tableRegistry.listTables();
		List<LobbyTable> empty = findEmpty(tables);
		DefaultActivatorConfig c = CONF.get();
		if(empty.size() < c.getMinAvailTables()) {
			incrementTables(c.getIncrementSize());
		} else {
			checkTimeoutTables(tables.length, empty, c);
		}
	}
	
	
	/**
	 * Utility method for getting empty tables out of an array
	 * of tables.
	 * 
	 * @param tables
	 * @return
	 */
	protected List<LobbyTable> findEmpty(LobbyTable[] tables) {
		List<LobbyTable> tmp = new LinkedList<LobbyTable>();
		for (LobbyTable t : tables) {
			if(isEmpty(t)) {
				tmp.add(t);
			}
		}
		return tmp;
	}
	
	/**
	 * Get the configuration for the given Activator
	 * 
	 * @return
	 */
	protected DefaultActivatorConfig getConfiguration() {
		return CONF.get();
	}
	
	/**
	 * Call this method when you have excess empty tables to see if
	 * we have any tables that has timed out (empty for a defined time).
	 * 
	 * In the case of time out we will close and remove the table if 
	 * applicable (i.e. more empty then configured as minimum).
	 * 
	 * @param fullSize
	 * @param empty
	 * @param c
	 */
	protected void checkTimeoutTables(int fullSize, List<LobbyTable> empty, DefaultActivatorConfig c) {
		int initSize = empty.size();
		if(initSize <= c.getMinAvailTables()) return; // SANITY CHECK
		if(initSize <= c.getMinTables()) return; // SANITY CHECK
		removeNonOldTables(empty, c.getTableTimeout());
		int target = empty.size();
		int minAvail = c.getMinAvailTables();
		int minTab = c.getMinTables();
		target = adjustTarget(fullSize, initSize, target, minAvail, minTab);
		for (int i = 0; i < target && i < empty.size(); i++) {
			LobbyTable table = empty.get(i);
			tableRegistry.destroyTable(table, false);
		}
	}
	
	/**
	 * Create the initial tables.
	 *
	 */
	protected void initTables() {
		DefaultActivatorConfig c = CONF.get();
		LobbyTable[] tables = tableRegistry.listTables();
		if(tables.length == 0) {
			incrementTables(c.getMinTables());
		}
	}
	
	
	
	// --- PRIVATE METHODS --- //

	private void changeThreads() {
		synchronized(threadLock) {
			stopThreading();
			startThreading();
		}
	}
	
	private void destroyThreading() {
		synchronized(threadLock) {
			if(scheduler == null) return; // SANITY CHECK
			if(future != null) stopThreading();
			scheduler.shutdownNow();
			scheduler = null;
		}
	}
	
	private void setupThreading() {
		synchronized(threadLock) {
			if(scheduler != null) return; // SANITY CHECK
			scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setName("TableActivator");
					thread.setDaemon(true);
					return thread;
				}
			});
		}
	}
	
	private void startThreading() {
		synchronized(threadLock) {
			if(scheduler == null) return; // SANITY CHECK
			if(future != null) stopThreading();
			DefaultActivatorConfig c = CONF.get();
			long frequency = c.getScanFrequency();
			long initial = c.getInitialDelay();
			if(frequency > 0) {
				future = scheduler.scheduleWithFixedDelay(new Runner(), initial, frequency, TimeUnit.MILLISECONDS);
			}
		}
	}

	
	private void stopThreading() {
		synchronized(threadLock) {
			if(future == null) return; // SANITY CHECK
			future.cancel(true);
			future = null;
		}
	}

	private int adjustTarget(int size, int allEmpty, int target, int minAvail, int minTab) {
		/*
		 * If the total size minues all removals (expired tables) is less than
		 * the minimum tables for the system, return the full size minus the minimum
		 * 
		 * Example:
		 * 
		 *     We have 24 tables
		 *     8 tables are expired and empty
		 *     17 tables is the minimum
		 *     
		 *     Then: (24 - 8 < 17) = true -> return (24 - 17 = 7)
		 */
		if(size - target < minTab) target = greaterMinusLesser(size, minTab);
		/*
		 * If all possible removals (empty tables) minus the target removals (expired tables) 
		 * is less than the minimum available tables for the system, return the possible rmeovals
		 * minus the minimum available.
		 * 
		 * Example:
		 * 
		 *     8 tables are empty
		 *     5 tables are empty and expired
		 *     5 tables is the minimum
		 *     
		 *     Then: (8 - 5 < 5) = true -> return (8 - 5 = 3)
		 */
		if(allEmpty - target < minAvail) target = greaterMinusLesser(allEmpty, minAvail);
		/*
		 * If we get here the target size is not going to break the min available tables nor
		 * is it going to break the min tables.
		 */
		return target;
	}

	private int greaterMinusLesser(int a, int b) {
		return Math.max(a, b) - Math.min(a, b);
	}

	private void removeNonOldTables(List<LobbyTable> empty, long tableTimeout) {
		if(tableTimeout <= 0) {
			empty.clear();
			return;
		} else {
			for (Iterator<LobbyTable> it = empty.iterator(); it.hasNext(); ) {
				LobbyTable table = it.next();
				if(!isOld(table, tableTimeout)) {
					it.remove();
				}
			}
		}
	}

	private boolean isOld(LobbyTable table, long tableTimeout) {
		AttributeValue value = table.getAttributes().get(DefaultTableAttributes._LAST_MODIFIED.toString());
		if(value == null) return false;
		else {
			try {
				long l = Long.parseLong(value.data.toString());
				return (System.currentTimeMillis() > l + tableTimeout);
			} catch(NumberFormatException e) {
				log.warn("Table with id '" + table.getTableId() + "' has a last modifed attribute which is not a long integer as a string", e);
				return false;
			}
		}
	}

	

	private boolean isEmpty(LobbyTable t) {
		AttributeValue att = t.getAttributes().get(DefaultTableAttributes._SEATED.toString());
		if(att == null || att.getType() != Type.INT) return false;
		else return att.data.equals(new Integer(0));
	}

	
	
	private void incrementTables(int count) {
		DefaultActivatorConfig c = CONF.get();
		CreationParticipant part = PART.get();
		tableRegistry.createTables(count, c.getSeats(), part);
	}


	// --- PRIVATE CLASSES --- //
	
	private class Runner implements Runnable {
		public void run() {
			try {
				checkTables();
			} catch(Throwable th) {
				log.error("Failed to check tables!", th);
			}
		}
	}

	private class Listener implements ConfigSourceListener {

		public void sourceAdded(ConfigSource src) {
			sourceModified(src);
		}

		public void sourceModified(ConfigSource src) {
			doModified(src);
		}

		public void sourceRemoved(ConfigSource src) { }
		
	}
}
