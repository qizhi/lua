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

import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.server.routing.MttNodeRouter;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.RouterEvent;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;

public class MttRouterImpl extends BaseRouter implements MttNodeRouter {

	// private Fetcher<ChannelEvent> fetcher;
	// private TrivialFetcherWrap<ChannelEvent> wrap;
	private Sender<Event<?>> gameSender;
	private WrappedSender<GameEvent> wrapSender;
	private Receiver<ChannelEvent> receiver;
	
	public MttRouterImpl(String id, String name) {
		super(id, name);
	}
	
	@Override
	public void destroy() {
		destroyRouting();
		super.destroy();
	}

	public WrappingSender<GameEvent> getGameEventSender() {
		checkRouting();
		return new WrappingSender<GameEvent>(wrapSender);
	}

	public Partition getLocalPartition() {
		// TODO: The partition should be created by the mbus?
		return new Partition(EventType.MTT, getId(), "Partition " + getId(), null, con.getServerId());
	}
	
	public Receiver<RouterEvent> getMttEventReceiver() {
		checkRouting();
		return new Receiver<RouterEvent>() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void addEventListener(EventListener list) {
				receiver.addEventListener(list);
			}
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void removeEventListener(EventListener list) {
				receiver.removeEventListener(list);
			}
			
			public void setOrphanEventListener(OrphanEventListener<ChannelEvent> list) {
				receiver.setOrphanEventListener(list);
			}
			
			public void destroy() { }
		};
	}

	
	// --- PRIVATE METHODS --- //
	
	private synchronized void checkRouting() {
		try {
			initMttFetcher();
			initGameSender();
		} catch (MBusException e) {
			String msg = "Failed to initialize client router; Recieved message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		}
	}
	
	private void initGameSender() throws MBusException {
		if(gameSender != null) return;
		gameSender = mbus.createSender(EventType.GAME, getId());
		wrapSender = new WrappedSender<GameEvent>(gameSender);
		haltables.add(wrapSender);
	}
	
	private void initMttFetcher() throws MBusException {
		if(receiver != null) return;
		receiver = mbus.createReceiver(EventType.MTT, getId(), getId());
		
		/*if(fetcher != null) return;
		fetcher = mbus.createFetcher(EventType.MTT, getId(), getId());
		wrap = new TrivialFetcherWrap<ChannelEvent>(fetcher);*/
	}

	private void destroyRouting() {
		/*if(fetcher != null) {
			fetcher.destroy();
			fetcher = null;
		}*/
		if(receiver != null) {
			receiver.destroy();
		}
		receiver = null;
		if(gameSender != null) {
			gameSender.destroy();
		}
		gameSender = null;
		// wrap = null;
	}
}
