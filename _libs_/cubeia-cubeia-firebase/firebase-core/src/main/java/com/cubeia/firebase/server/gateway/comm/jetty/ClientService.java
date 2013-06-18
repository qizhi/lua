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

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractService;

import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.server.node.ClientNodeContext;

public class ClientService extends AbstractService {

	public static final String CHANNEL = "/service/client";
	public static final String REMOTE_ATTR = "remoteAddress";
	public static final String SESSION_ATTR = "asyncSession";

	private final Logger log = Logger.getLogger(getClass());
	
	private final ClientNodeContext context;

	public ClientService(BayeuxServer server, ClientNodeContext context) {
		super(server, "clientService");
		addService(CHANNEL, "process");
		this.context = context;
	}
		
	public void process(ServerSession remote, Message msg) {
		ProtocolObject p = (ProtocolObject) msg.getData();
		log.debug("Incoming cometd message: " + p);
		AsyncSession ses = checkGetAsyncSession(remote);
		ses.handleIncoming(Collections.singletonList(p));
		// unpack(json, ses);
	}

	private AsyncSession checkGetAsyncSession(ServerSession remote) {
		AsyncSession ses = (AsyncSession) remote.getAttribute(SESSION_ATTR);
		if (ses == null) {
			final String clientId = remote.getId();
			InetSocketAddress a = (InetSocketAddress) remote.getAttribute(REMOTE_ATTR);
			ses = new JettyAsyncClient(context, a);
			remote.setAttribute(SESSION_ATTR, ses);
			ses.setAsyncSessionListener(new AsyncSessionListener() {
				
				@Override
				public void close() {
					getBayeux().getSession(clientId).disconnect();
				}
				
				@Override
				public void onMessage(List<ProtocolObject> list) {
					for (ProtocolObject o : list) {
						// String json = pack(o);
						getBayeux().getSession(clientId).deliver(getServerSession(), CHANNEL, o, null);
						log.debug("Outgoing cometd message: " + o);
					}
				}
			});
		}
		return ses;
	}
}
