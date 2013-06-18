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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.mtt.MttAction;
import com.cubeia.firebase.api.routing.ActivatorRouter;
import com.cubeia.firebase.api.util.Arguments;
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.event.MttEvent;
import com.cubeia.firebase.server.util.ThreadPoolProperties;
import com.cubeia.firebase.service.messagebus.ChannelNotFoundException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.util.executor.JmxExecutor;
import com.cubeia.util.threads.SafeRunnable;

public class ThreadedActivatorRouterImpl implements ActivatorRouter {

	private final JmxExecutor threads;
	private final InternalComponentRouter router;
	// private final int gameId;

	public ThreadedActivatorRouterImpl(int gameId, ThreadPoolProperties props, InternalComponentRouter router, boolean forGame) {
		Arguments.notNull(router, "router");
		// this.gameId = gameId;
		threads = new JmxExecutor(props.getCoreSize(), props.getMaxSize(), props.getTimeout(), (forGame ? "game" : "tournament") + "-activator-routers", String.valueOf(gameId));	
		this.router = router;
	}
	
	public void stop() {
		threads.stop();
	}
	
	
	// ---  SERVICE ROUTER --- //
	
	/*public void dispatchToPlayer(int playerId, ServiceAction action) {
		Arguments.notNull(action, "action");
		ClientEvent<GameAction> event = new ClientEvent<GameAction>();
		event.setAction(action);
		event.setPlayerId(playerId);
		Runner run = new Runner(router.getClientEventSender(), event);
		threads.submit(run);
	}*/
	
	/*public void dispatchToPlayers(int[] players, ServiceAction action) {
		Arguments.notNull(action, "action");
		MulticastClientEvent event = new MulticastClientEvent();
		event.checkSetPlayersGlobal(players);
		event.setAction(action);
		Runner run = new Runner(router.getClientEventSender(), event);
		threads.submit(run);
	}*/
	
	/*public void dispatchToService(ServiceDiscriminator disc, ServiceAction action) throws NoSuchRouteException {
		Arguments.notNull(disc, "discriminator");
		Arguments.notNull(action, "action");
		Sender<LocalServiceEvent> send = router.getServiceStackSender(disc);
		if(send == null) throw new NoSuchRouteException();
		else
			try {
				send.dispatch(new LocalServiceEvent(disc, action));
			} catch (ChannelNotFoundException e) {
				/* 
				 * This should not happen here, and if it does we can
				 * safely throw a so such route exception /LJN
				 *
				throw new NoSuchRouteException();
			}
	}*/
	
	public void dispatchToTournament(int mttId, MttAction action) {
		Arguments.notNull(action, "action");
		MttEvent event = new MttEvent();
		event.setAction(action);
		event.setMttId(mttId);
		Runner run = new Runner(router.getMttSender(), event);
		threads.submit(run);
	}
	
	public void dispatchToGame(int gameId, GameAction action) {
		Arguments.notNull(action, "action");
		GameEvent event = new GameEvent();
		event.setAction(action);
		event.setTableId(action.getTableId());
		Runner run = new Runner(router.getGameEventSender(), event);
		threads.submit(run);
	}

	
	// --- PRIVATE CLASSES --- //
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final class Runner extends SafeRunnable {
		
		private final Event event;
		private final Sender sender;
		
		private Runner(Sender sender, Event event) {
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
