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
import com.cubeia.firebase.server.instance.SystemLogger;
import com.cubeia.firebase.service.messagebus.EventType;
import com.cubeia.firebase.service.messagebus.MBusException;
import com.cubeia.firebase.service.messagebus.Sender;
import com.cubeia.firebase.service.messagebus.util.WrappingSender;

public final class InternalComponentRouter extends BaseRouter {
	
	private Sender<Event<?>> gameSender;

	public InternalComponentRouter(String id, String name) {
		super(id, name);
	}
	
	@Override
	public void init(RouterContext con) throws SystemException {
		super.init(con);
		//initRouting();
	}

	@Override
	public void destroy() {
		destroyGameSender();
		super.destroy();
	}
	
	public synchronized Sender<Event<?>> getGameEventSender() {
		initGameSender();
		return gameSender;
	}

	private synchronized void destroyGameSender() {
		if(gameSender == null) return;
		gameSender.destroy();
		gameSender = null;
		// gameWrap = null;	
	}
	
	private synchronized void initGameSender() {
		if(gameSender != null) return;
		try {
			gameSender = new WrappingSender<Event<?>>(mbus.createSender(EventType.GAME, getId()));
		} catch (MBusException e) {
			String msg = "Failed to initialize internal service router; Recieved message: " + e.getMessage();
			SystemLogger.error(msg);
			log.fatal(msg, e);
		}
	}
}
