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

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.server.conf.ConfigurationException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.config.ClusterConfigProviderContract;
import com.cubeia.firebase.service.random.api.RandomSeedService;
import com.cubeia.firebase.service.random.api.RandomService;
import com.cubeia.firebase.service.random.api.RandomServiceConfig;

/**
 * This is the random service implementation. It honors the 
 * {@link RandomServiceConfig configuration} and is otherwise
 * trivial. 
 * 
 * @author Lars J. Nilsson
 */
public class ServiceImpl implements Service, RandomService {
	
	private Random defaultRandom;
	private RandomSeedService seeder = null;
	private RandomServiceConfig config;
	private ServiceContext context;
	
	/*
	 * Keep track of started state, used for randoms that have been created *before*
	 * start has been called
	 */
	private final AtomicBoolean isStarted = new AtomicBoolean(false); 
	
	/*
	 * Keep a list of randoms that needs to be started and stopped as a part of
	 * this service. 
	 */
	private final List<BackgroundPollingRandom> pollingRandoms = new CopyOnWriteArrayList<BackgroundPollingRandom>();
	
	private final Logger log = Logger.getLogger(getClass());
	
	// --- SERVICE METHODS --- //
	
	public void init(ServiceContext context) throws SystemException {
		this.context = context; 
		createConfig();
		log.info("MT Random Service initiated");
	}

	public void start() { 
		// start all created pollers
		for (BackgroundPollingRandom rand : pollingRandoms) {
			rand.start();
		}
		// remember state
		isStarted.set(true);
		log.info("MT Random Service started");
	}
	
	public void stop() { 
		// start all created pollers
		for (BackgroundPollingRandom rand : pollingRandoms) {
			rand.stop();
		}
		// remember state
		isStarted.set(false);
		log.info("MT Random Service stopped");
	}
	
	public void destroy() { 
		pollingRandoms.clear();
		log.info("MT Random Service destroyed");
	}
	
	
	
	// --- CONTRACT METHODS --- //
	
	@Override
	public synchronized Random getSystemDefaultRandom() {
		if(defaultRandom == null) {
			log.info("MT Random Service creating default Random");
			defaultRandom = createNewRandom();
		}
		return defaultRandom;
	}

	@Override
	public synchronized Random createNewRandom() {
		log.debug("MT Random Service creating Random; Using background polling: " + config.enableBackgroundPolling() + "; Using discarded draw: " + config.enableDiscardedDraw());
		// get seeder
		RandomSeedService seeder = getSeedService();
		// create twister
		MarsenneTwister twister = new MarsenneTwister(seeder);
		// create random
		InternalRandom next = new RandomImpl(twister);
		// check+wrap with background poller
		next = checkCreateBackgroupPoller(next);
		// check+ẃrap with discarding drawer
		next = checkCreateDiscardedDraw(next);
		// return wrap to hade internal interface
		return new RandomWrapper(next);
	}

	
	// --- PRIVATE METHODS --- //
	
	/*
	 * If necessary, create discarding draw random and return
	 */
	private InternalRandom checkCreateDiscardedDraw(InternalRandom next) {
		if(config.enableDiscardedDraw()) {
			next = new DiscardedDrawRandom(next, config.getDiscardedDrawMaxDiscarded());
		}
		return next;
	}

	/*
	 * If necessary, create discarding background poller and return
	 */
	private InternalRandom checkCreateBackgroupPoller(InternalRandom next) {
		if(config.enableBackgroundPolling()) {
			BackgroundPollingRandom poller = new BackgroundPollingRandom(next, config.getBackgroundPollingInterval(), config.getBackgroundPollingMaxDiscarded());
			/*
			 * Add to list, and if we're started already
			 * start the poller immediately
			 */
			this.pollingRandoms.add(poller);
			if(isStarted.get()) {
				poller.start();
			}
			next = poller;
		}
		return next;
	}
	
	private void createConfig() throws SystemException {
		ClusterConfigProviderContract service = context.getParentRegistry().getServiceInstance(ClusterConfigProviderContract.class);
		try {
			config = service.getConfiguration(RandomServiceConfig.class, null);
		} catch (ConfigurationException e) {
			throw new SystemException("failed to read config", e);
		}
	}
	
	private RandomSeedService getSeedService() {
		if(seeder != null) {
			return seeder; // we already have looked one up
		} else {
			seeder = findSeeder(); // search
			return seeder;
		}
	}

	private RandomSeedService findSeeder() {
		RandomSeedService tmp = context.getParentRegistry().getServiceInstance(RandomSeedService.class);
		if(tmp == null) {
			/*
			 * No service deployed, fall back on secure random
			 */
			tmp = new SecureRandomSeed();
		}
		return tmp;
	}
}