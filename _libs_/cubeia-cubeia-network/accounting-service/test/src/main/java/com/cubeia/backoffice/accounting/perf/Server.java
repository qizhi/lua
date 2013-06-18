/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.backoffice.accounting.perf;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.quartz.impl.StdScheduler;
import org.springframework.context.ApplicationContext;

import com.cubeia.backoffice.accounting.core.entity.Account;
import com.cubeia.backoffice.accounting.core.manager.AccountingManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/*
 * This simple server starts a configured number of bots and
 * waits for them to end.
 */

@Singleton
public class Server {

	private static final long SYSTEM = -1;
	private static final long TRANSFER = -2;

	@Inject
	@Named("no-of-bots")
	private int numBots;
	
	@Inject
	@Named("bot-id-offset")
	private int idOffset;
	
	@Inject
	private Provider<Bot> factory;
	
	private ExecutorService exec;
	private List<Future<?>> bots;
	
	private final Logger log = Logger.getLogger(getClass());
	
	@Inject
	private ApplicationContext spring;

	@Inject
	private AccountingManager man;

	@Inject
	private Stats stats;
	
	private long transferAccountId;
	private long systemAccountId;
	
	public void run() {
		log.info("Server started");
		mountStatsBean();
		checkAccounts();
		startBots();
		log.info("Server waiting for bots to finish");
		doWait();
		log.info("Server stopped");
	}


	// --- PRIVATE METHODS --- //

	private void mountStatsBean() {
		MBeanServer serv = ManagementFactory.getPlatformMBeanServer();
		try {
			serv.registerMBean(stats, new ObjectName("com.cubeia.bo.accounting.perf:name=Statistics"));
		} catch (Exception e) {
			log.error("Failed to mount stats mbean", e);
		} 
	}
	
	private void doWait() {
		for (Future<?> fut : bots) {
			if(!fut.isDone()) {
				try {
					fut.get();
				} catch (Exception e) {
					log.error("Unexpected error while witing for bot", e);
				} 
			}
		}
		shutdownScheduler();
		exec.shutdown();
	}
	
	private void shutdownScheduler() {
		StdScheduler b = (StdScheduler) spring.getBean("checkpointSchedulerFactory");
		//try {
			b.shutdown();
		/*} catch (SchedulerException e) {
			log.error("Failed to shutdown scheduler", e);
		}*/
	}


	private void startBots() {
		bots = new LinkedList<Future<?>>();
		exec = Executors.newCachedThreadPool();
		for (int i = idOffset; i < (idOffset + numBots); i++) {
			Bot bot = factory.get();
			//String name = prefix + (i + 1);
			bot.setId(i + 1);
			Future<?> fut = exec.submit(bot);
			bots.add(fut);
		}
	}
	
	private void checkAccounts() {
		systemAccountId = checkAccount(SYSTEM);
		transferAccountId = checkAccount(TRANSFER);
	}
	
	private long checkAccount(long userId) {
		Collection<Account> list = man.getAccountsByUserId(userId);
		Account a = null;
		if(list == null || list.size() == 0) {
			log.debug("Server creates " + userId + " account");
			a = man.createAccount(new Account(userId, "SEK", 2));
		} else {
			a = list.iterator().next();
		}
		return a.getId();
	}


	long getTransferAccountId() {
		return transferAccountId;
	}


	long getSystemAccountId() {
		return systemAccountId;
	}
}
