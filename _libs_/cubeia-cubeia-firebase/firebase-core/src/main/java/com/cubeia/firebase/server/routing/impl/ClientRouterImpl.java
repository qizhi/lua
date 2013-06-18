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
import com.cubeia.firebase.server.event.Event;
import com.cubeia.firebase.server.event.GameEvent;
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.server.routing.ClientNodeRouter;
import com.cubeia.firebase.service.messagebus.ChannelEvent;
import com.cubeia.firebase.service.messagebus.EventListener;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.OrphanEventListener;
import com.cubeia.firebase.service.messagebus.Receiver;
import com.cubeia.firebase.service.messagebus.RouterEvent;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;

public class ClientRouterImpl extends BaseRouter implements ClientNodeRouter {

	//private Fetcher<ChannelEvent> chatFetcher;
	//private Fetcher<ChannelEvent> clientFetcher;
	//private TrivialFetcherWrap<ChannelEvent> clientWrap;
	private Receiver<ChannelEvent> chatReceiver;
	private Receiver<ChannelEvent> receiver;
	//private Fetcher<RouterEvent> chatWrap;
	private Sender<Event<?>> gameSender;
	private WrappedSender<GameEvent> gameWrap;

	public ClientRouterImpl(String id, String name) {
		super(id, name);
	}
	
	@Override
	public void init(RouterContext con) throws SystemException {
		super.init(con);
		//initRouting();
	}

	@Override
	public void destroy() {
		destroyChatFetcher();
		destroyClientReceiver();
		destroyGameSender();
		super.destroy();
	}
	
	public Receiver<RouterEvent> getChatTopicReceiver() {
		checkRouting();
		return new Receiver<RouterEvent>() {
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void addEventListener(EventListener list) {
				chatReceiver.addEventListener(list);
			}
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void removeEventListener(EventListener list) {
				chatReceiver.removeEventListener(list);
			}
			
			public void setOrphanEventListener(OrphanEventListener<ChannelEvent> list) {
				chatReceiver.setOrphanEventListener(list);
			}
			
			public void destroy() { }
		};
	}
	
	public Receiver<RouterEvent> getClientEventReceiver() {
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

	public WrappingSender<GameEvent> getGameEventSender() {
		checkRouting();
		return new WrappingSender<GameEvent>(gameWrap);
	}

	
	// --- PRIVATE METHODS --- //
	
	private synchronized void checkRouting() {
		try {
			initChatFetcher();
			initClientReceiver();
			initGameSender();
		} catch (MBusException e) {
			String msg = "Failed to initialize client router; Recieved message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		}
	}

	private void initClientReceiver() throws MBusException {
		if(receiver != null) return;
		receiver = mbus.createReceiver(EventType.CLIENT, getId(), getId());	
	}
	
	private void destroyClientReceiver() {
		if(receiver == null) return;
		receiver.destroy();
		receiver = null;
	}

	private void initGameSender() throws MBusException {
		if(gameSender != null) return;
		gameSender = mbus.createSender(EventType.GAME, getId());
		gameWrap = new WrappedSender<GameEvent>(gameSender);
		haltables.add(gameWrap);
	}

	/*private void initClientFetcher() throws MBusException {
		if(clientFetcher != null) return;
		clientFetcher = mbus.createFetcher(EventType.CLIENT, Constants.GAME_TOPIC_NAME, getId());
		clientWrap = new TrivialFetcherWrap<ChannelEvent>(clientFetcher);
	}*/

	private void initChatFetcher() throws MBusException {
		if(chatReceiver != null) return;
		chatReceiver = mbus.createReceiver(EventType.CHAT, getId(), getId());	
		
		/*if(chatFetcher != null) return;
		chatFetcher = mbus.createFetcher(EventType.CHAT, Constants.CHAT_TOPIC_NAME, getId());
		chatWrap = new TrivialFetcherWrap<ChannelEvent>(chatFetcher);*/
	}
	
	private void destroyChatFetcher() {
		if(chatReceiver == null) return;
		chatReceiver.destroy();
		chatReceiver = null;
		/*if(chatFetcher == null) return;
		chatFetcher.destroy();
		chatFetcher = null;
		chatWrap = null;*/
	}
	
	/*private void destroyClientFetcher() {
		if(clientFetcher == null) return;
		clientFetcher.destroy();
		clientFetcher = null;
		clientWrap = null;
	}*/
	
	private void destroyGameSender() {
		if(gameSender == null) return;
		gameSender.destroy();
		gameSender = null;
		// gameWrap = null;
	}
}
