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

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.server.routing.GameNodeRouter;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;
import com.cubeia.firebase.service.messagebus.Partition;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.RouterEvent;

public class GameRouterImpl extends BaseRouter implements GameNodeRouter {

	// private Fetcher<ChannelEvent> fetcher;
	private Receiver<ChannelEvent> receiver;
	// private TrivialFetcherWrap<ChannelEvent> wrap;

	public GameRouterImpl(String id, String name) {
		super(id, name);
	}

	@Override
	public void init(RouterContext con) throws SystemException {
		super.init(con);
		//initRouting();
	}
	
	@Override
	public void destroy() {
		destroyRouting();
		super.destroy();
	}
	
	public Receiver<RouterEvent> getGameEventReceiver() {
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

	public Partition getLocalPartition() {
		// TODO: The partition should be created by the mbus?
		return new Partition(EventType.GAME, getId(), "Partition " + getId(), null, con.getServerId());
	}
	
	
	// --- PRIVATE METHODS --- //
	
	private synchronized void checkRouting() {
		if(receiver != null) return;
		try {
			receiver = mbus.createReceiver(EventType.GAME, getId(), getId());
			// wrap = new TrivialReceiverWrap<ChannelEvent>(receiver);
		} catch (MBusException e) {
			log.fatal("Failed to create game event receiver.", e);
		}
		
		/*if(fetcher != null) return;
		try {
			fetcher = mbus.createFetcher(EventType.GAME, getId(), getId());
			wrap = new TrivialFetcherWrap<ChannelEvent>(fetcher);
		} catch (MBusException e) {
			log.fatal(e);
		}*/
	}

	private void destroyRouting() {
		if(receiver != null) {
			receiver.destroy();
		}
		receiver = null;
		// wrap = null;
		
		/*if(fetcher != null) {
			fetcher.destroy();
			fetcher = null;
		}
		wrap = null;*/
	}
}
