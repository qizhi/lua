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
package com.cubeia.firebase.server.lobby.systemstate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.mock.MockClient;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.LobbySubscriptionRequest;
import com.cubeia.firebase.server.lobby.systemstate.ObjectSubscriptions;

public class ConcurrentObjectSubscriptionsTest extends TestCase {

	private ObjectSubscriptions sub = new ObjectSubscriptions();
	
	private ExecutorService executor = Executors.newFixedThreadPool(10);
	
	public void testAddsubscriptionTablesnapshot() throws Exception {
		LobbyPath path = new LobbyPath(99, "a", 1);
		List<Client> clients = createClients(400);
		
		for (Client client : clients) {
			LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path);
			executor.execute(new subscriptionTask(request, client));
		}
		
		// Wait for execution
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);
		
		
		TableSnapshotPacket packet = new TableSnapshotPacket();
		packet.tableid = 1;
		packet.name = "Table_ 1";
		packet.capacity = 10;
		packet.address = "a";
		
		Collection<Client> subs = sub.getSubscribers(path, packet);
		assertEquals(clients.size(), subs.size());
	}
	
	private List<Client> createClients(int count) {
		List<Client> clients = new ArrayList<Client>();
		for (int i = 0; i < count; i++) {
			Client client = new MockClient(i);
			clients.add(client);
		}
		return clients;
	}
	
	
	
	private class subscriptionTask implements Runnable {
		private final LobbySubscriptionRequest request;
		private final Client client;

		public subscriptionTask(LobbySubscriptionRequest request, Client client) {
			this.request = request;
			this.client = client;
		}

		public void run() {
			try {
				sub.addSubscription(request, client);
			} catch (Throwable th) {
				fail(th.toString());
				th.printStackTrace();
			}
			
		}
		
	}
	
	
	
}
