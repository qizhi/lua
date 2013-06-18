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

import static com.cubeia.firebase.util.Classes.verifyServerClassLoaderInContext;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.Action;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.NoSuchRouteException;
import com.cubeia.firebase.api.service.ServiceDiscriminator;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.event.ClientEvent;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.LocalServiceEvent;
import com.cubeia.firebase.server.event.MulticastClientEvent;
import com.cubeia.firebase.server.event.ServiceGameEvent;
import com.cubeia.firebase.server.event.ServiceMttEvent;
import com.cubeia.firebase.server.util.ThreadPoolProperties;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.util.executor.JmxExecutor;
import com.cubeia.util.threads.SafeRunnable;

public class ThreadedServiceRouterImpl implements ServiceRouter {

	private final JmxExecutor threads;
	private final String serviceId;

	private final InternalComponentRouter router;

	public ThreadedServiceRouterImpl(String serviceId, ThreadPoolProperties props, InternalComponentRouter router) {
		Arguments.notNull(serviceId, "serviceId");
		Arguments.notNull(router, "router");
		this.serviceId = serviceId;
		threads = new JmxExecutor(props.getCoreSize(), props.getMaxSize(), props.getTimeout(), "service-routers", serviceId);	
		this.router = router;
	}
	
	public void stop() {
		threads.stop();
	}
	
	
	// ---  SERVICE ROUTER --- //
	
	public void dispatchToGameActivator(int gameId, ActivatorAction<?> action) {
		Arguments.notNull(action, "action");
		ActionRunner run = new ActionRunner(router.getGameActivatorSender(gameId), action);
		threads.submit(run);
	}
	
	public void dispatchToTournamentActivator(int mttId, ActivatorAction<?> action) {
		Arguments.notNull(action, "action");
		ActionRunner run = new ActionRunner(router.getMttActivatorSender(mttId), action);
		threads.submit(run);
	}
	
	public void dispatchToPlayer(int playerId, ServiceAction action) {
		Arguments.notNull(action, "action");
		ClientEvent<GameAction> event = new ClientEvent<GameAction>();
		event.setAction(action);
		event.setPlayerId(playerId);
		Runner run = new Runner(router.getClientEventSender(), event);
		threads.submit(run);
	}
	
	public void dispatchToPlayers(int[] players, ServiceAction action) {
		Arguments.notNull(action, "action");
		MulticastClientEvent event = new MulticastClientEvent();
		event.checkSetPlayersGlobal(players);
		event.setAction(action);
		Runner run = new Runner(router.getClientEventSender(), event);
		threads.submit(run);
	}
	
	public void dispatchToPlayer(int playerId, GameAction action) {
		Arguments.notNull(action, "action");
		ClientEvent<GameAction> event = new ClientEvent<GameAction>();
		event.setAction(action);
		event.setPlayerId(playerId);
		Runner run = new Runner(router.getClientEventSender(), event);
		threads.submit(run);
	}
	
	public void dispatchToPlayer(int playerId, Collection<? extends GameAction> actions) {
		Arguments.notNull(actions, "actions");
		for (GameAction action : actions) {
			dispatchToPlayer(playerId, action);
		}
	}
	
	public void dispatchToService(ServiceDiscriminator disc, ServiceAction action) throws NoSuchRouteException {
		Arguments.notNull(disc, "discriminator");
		Arguments.notNull(action, "action");
		verifyServerClassLoaderInContext();
		Sender<LocalServiceEvent> send = router.getServiceStackSender(disc);
		if(send == null) throw new NoSuchRouteException();
		else
			try {
				send.dispatch(new LocalServiceEvent(disc, action));
			} catch (ChannelNotFoundException e) {
				/* 
				 * This should not happen here, and if it does we can
				 * safely throw a so such route exception /LJN
				 */
				throw new NoSuchRouteException();
			}
	}
	
	public void dispatchToTournament(int mttId, MttAction action) {
		Arguments.notNull(action, "action");
		ServiceMttEvent event = new ServiceMttEvent();
		event.setAction(action);
		event.setServiceId(serviceId);
		event.setMttId(mttId);
		Runner run = new Runner(router.getMttSender(), event);
		threads.submit(run);
	}
	
	public void dispatchToGame(int gameId, GameAction action) {
		Arguments.notNull(action, "action");
		ServiceGameEvent event = new ServiceGameEvent();
		event.setAction(action);
		event.setServiceId(serviceId);
		event.setTableId(action.getTableId());
		Runner run = new Runner(router.getGameEventSender(), event);
		threads.submit(run);
	}

	
	// --- PRIVATE CLASSES --- //

	@SuppressWarnings("rawtypes")
	private static final class Runner extends SafeRunnable {
		
		private final Event event;
		private final Sender sender;
		
		private Runner(Sender sender, Event event) {
			verifyServerClassLoaderInContext();
			this.sender = sender;
			this.event = event;
		}
		
		@SuppressWarnings("unchecked")
		public void innerRun() {
			try {
				sender.dispatch(event);
			} catch (ChannelNotFoundException e) {
				Logger.getLogger(getClass()).error("Could not find route for event", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static final class ActionRunner extends SafeRunnable {
		
		private final Action event;
		
		@SuppressWarnings("rawtypes")
		private final Sender sender;
		
		@SuppressWarnings("rawtypes")
		private ActionRunner(Sender sender, Action event) {
			this.sender = sender;
			this.event = event;
		}
		
		public void innerRun() {
			try {
				sender.dispatch(event);
			} catch (ChannelNotFoundException e) {
				Logger.getLogger(getClass()).error("Could not find route for event", e);
			}
		}
	}
}
