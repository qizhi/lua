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
package com.cubeia.firebase.server.routing.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.Logger;
import org.jboss.cache.util.concurrent.ConcurrentHashSet;

import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.server.Haltable;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.api.service.ServiceDiscriminator;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.activation.ActivatorCommand;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.LocalServiceEvent;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.server.instance.InternalComponentAccess;
import com.cubeia.firebase.server.routing.SystemRouter;
import com.cubeia.firebase.server.service.InternalServiceRegistry;
import com.cubeia.firebase.service.conn.ClusterConnection;
import com.cubeia.firebase.service.conn.ClusterException;
import com.cubeia.firebase.service.conn.ConnectionServiceContract;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusContract;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;
import com.cubeia.firebase.util.FirebaseLockFactory;

public abstract class BaseRouter implements SystemRouter, Initializable<RouterContext> {
	
	protected final Logger log = Logger.getLogger(getClass());
	
	protected MBusContract mbus;
	protected RouterContext con;

	private final String id;
	private final String name;
	
	private Sender<Event<?>> clientSender;
	private WrappedSender<ClientEvent<?>> wrappedClient;
	
	private Sender<Event<?>> mttSender;
	private WrappedSender<MttEvent> wrappedMtt;
	
	/*
	 * Trac #562: Using fair locks + #581
	 */
	private final ReadWriteLock senderLock = FirebaseLockFactory.createLock();
	private final Map<String, ThreadedServiceSender> serviceSenders = new TreeMap<String, ThreadedServiceSender>();
	private final AtomicBoolean isHalted = new AtomicBoolean();
	// private WrappedSender wrapper;
	
	protected final Set<Haltable> haltables = new ConcurrentHashSet<Haltable>();

	public BaseRouter(String id, String name) {
		Arguments.notNull(id, "id");
		Arguments.notNull(name, "name");
		this.id = id;
		this.name = name;
	}
	
	public void destroy() {
		destroyClient();
		destroySenders();
		this.con = null;
	}

	public void init(RouterContext con) throws SystemException {
		this.con = con;
		mbus = con.getMessageBus();
		//setupClient();
	}
	
	public void halt() {
		isHalted.set(true);
		for (Haltable h : haltables) {
			h.halt();
		}
	}
	
	public boolean isHalted() {
		return isHalted.get();
	}
	
	public void resume() {
		isHalted.set(true);
		for (Haltable h : haltables) {
			h.resume();
		}
	}

	/*public Sender<Event> getChatTopicSender() {
		checkSetup();
		return chatSender;
	}*/
	
	public WrappingSender<ClientEvent<?>> getClientEventSender() {
		checkSetup();
		return new WrappingSender<ClientEvent<?>>(wrappedClient);
	}
	
	public WrappingSender<MttEvent> getMttSender() {
		checkMttSetup();
		return new WrappingSender<MttEvent>(wrappedMtt);
	}
	
	public Sender<ActivatorAction<?>> getGameActivatorSender(int gameId) {
		return new ActivatorSender(gameId, true);
	}
	
	public Sender<ActivatorAction<?>> getMttActivatorSender(int mttId) {
		return new ActivatorSender(mttId, false);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public Sender<LocalServiceEvent> getServiceStackSender(ServiceDiscriminator disc) {
		Arguments.notNull(disc, "disc");
		String id = findServiceId(disc);
		if(id == null) return null; // SERVICE NOT FOUND
		ThreadedServiceSender service = checkSenderCreated(id);
		if(service != null) return service;
		else {
			senderLock.writeLock().lock();
			try {
				return tryCreateServiceSender(id);
			} finally {
				senderLock.writeLock().unlock();
			}
		}
	}
	


	// --- PRIVATE METHODS --- //
	
	private void destroySenders() {
		for (ThreadedServiceSender s : serviceSenders.values()) {
			s.stop();
		}
	}
	
	private ThreadedServiceSender checkSenderCreated(String id) {
		senderLock.readLock().lock();
		try {
			return serviceSenders.get(id);
		} finally {
			senderLock.readLock().unlock();
		}
	}
	
	// lock elsewhere
	private Sender<LocalServiceEvent> tryCreateServiceSender(String id) {
		RoutableService service = getRoutableService(id);
		if(service == null) return null; // NOT ROUTABLE
		ThreadedServiceSender sender = new ThreadedServiceSender(id, con, service);
		serviceSenders.put(id, sender);
		return sender;
	}
	
	private RoutableService getRoutableService(String id) {
		InternalServiceRegistry reg = InternalComponentAccess.getRegistry();
		return reg.getRoutableService(id);
	}
	
	private String findServiceId(ServiceDiscriminator disc) {
		if(!disc.isConstraintContract) return disc.constraint;
		else {
			InternalServiceRegistry reg = InternalComponentAccess.getRegistry();
			List<String> ids = reg.getServiceIdsForContract(disc.constraint);
			if(ids != null && ids.size() > 0) return ids.get(0);
			else return null;
		}
	}

	private void destroyClient() {
		/*if(chatSender != null) {
			chatSender.destroy();
		}*/
		if(clientSender != null) {
			clientSender.destroy();
		}
		if(mttSender != null) {
			mttSender.destroy();
		}
	}
	
	private synchronized void checkSetup() {
		if(clientSender != null) return; 
		try {
			// chatSender = mbus.createSender(EventType.CHAT, getId());
			clientSender = mbus.createSender(EventType.CLIENT, getId());
			wrappedClient = new WrappedSender<ClientEvent<?>>(clientSender);
			haltables.add(wrappedClient);
		} catch (MBusException e) {
			log.fatal(e, e);
		}
	}
	
	private synchronized void checkMttSetup() {
		if(mttSender != null) return; 
		try {
			// chatSender = mbus.createSender(EventType.CHAT, getId());
			mttSender = mbus.createSender(EventType.MTT, getId());
			wrappedMtt = new WrappedSender<MttEvent>(mttSender);
			haltables.add(wrappedMtt);
		} catch (MBusException e) {
			log.fatal(e, e);
		}
	}
	
	// --- PRIVATE CLASSES --- //
	
	private class ActivatorSender implements Sender<ActivatorAction<?>> {
		
		private final int id;
		private final boolean isGame;
 
		private ActivatorSender(int id, boolean isGame) {
			this.id = id;
			this.isGame = isGame;
		}
		
		public String getOwnerId() {
			return null;
		}
	
		public void dispatch(ActivatorAction<?> event) throws ChannelNotFoundException {
			ConnectionServiceContract contract = con.getServices().getServiceInstance(ConnectionServiceContract.class);
			ClusterConnection conn = contract.getSharedConnection();
			try {
				ActivatorCommand comm = new ActivatorCommand(id, event, isGame);
				conn.getCommandDispatcher().dispatch(comm);
			} catch (IOException e) {
				log.error("Failed to dispatch activator action", e);
			} catch (ClusterException e) {
				log.error("Failed to dispatch activator action", e);
			}
		}
	
		public void destroy() { }
		
	}
}
