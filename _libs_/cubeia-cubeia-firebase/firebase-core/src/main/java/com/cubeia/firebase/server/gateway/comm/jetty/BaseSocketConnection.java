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
package com.cubeia.firebase.server.gateway.comm.jetty;

import static com.cubeia.firebase.server.gateway.comm.jetty.JsonUtil.pack;
import static com.cubeia.firebase.server.gateway.comm.jetty.JsonUtil.unpack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.apache.log4j.Logger;

import com.cubeia.firebase.io.ProtocolObject;

public abstract class BaseSocketConnection {

	private final AsyncSession session;
	private final Logger log = Logger.getLogger(getClass());
	private final InetSocketAddress address;
	private final boolean preferSingleSend;
	
	public BaseSocketConnection(AsyncSession session, InetSocketAddress address, boolean preferSingleSend) {
		this.session = session;
		this.address = address;
		this.preferSingleSend = preferSingleSend;
	}
	
	public abstract void doSend(String msg) throws IOException;
	
	public abstract void forceClose();

	public void doOpen() {
		if(log.isTraceEnabled()) {
			log.trace("OPEN (" + address +")");
		}
		session.setAsyncSessionListener(new AsyncSessionListener() {
			
			@Override
			public void close() {
				if(log.isTraceEnabled()) {
					log.trace("FORCE CLOSE (" + address +")");
				}
				forceClose();
			}
			
			@Override
			public void onMessage(List<ProtocolObject> list) {
				if(preferSingleSend) {
					for (ProtocolObject o : list) {
						String json = pack(o);
						if(json != null) { 
							send(json);
						}
					}
				} else {
					String json = pack(list);
					if(json != null) { 
						send(json);
					}
				}
			}

			private void send(String json) {
				if(log.isTraceEnabled()) {
					log.trace("DISPATCH (" + address +"): " + json);
				}
				try {
					doSend(json);
				} catch (IOException e) {
					log.error("failed to send message", e);
				}
			}
		});
	}
	
	public void doMessage(String data) {
		if(data.length() > 0) {
			if(log.isTraceEnabled()) {
				log.trace("RECEIVED (" + address +"): " + data);
			}
			unpack(data, session);
		}
	}

	public void doClose() {
		session.setAsyncSessionListener(null);
		if(log.isTraceEnabled()) {
			log.trace("CLOSE (" + address +")");
		}
		session.disconnected();
	}
}
