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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import junit.framework.TestCase;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TableUpdatePacket;
import com.cubeia.firebase.io.protocol.TournamentSnapshotPacket;
import com.cubeia.firebase.io.protocol.TournamentUpdatePacket;
import com.cubeia.firebase.mock.MockClient;
import com.cubeia.firebase.server.gateway.client.Client;
import com.cubeia.firebase.server.lobby.LobbySubscriptionRequest;
import com.cubeia.firebase.server.lobby.LobbyUnsubscriptionRequest;
import com.cubeia.firebase.server.lobby.systemstate.ObjectSubscriptions;
import com.cubeia.firebase.server.lobby.systemstate.ObjectSubscriptions.PathContainer;

public class ObjectSubscriptionsTest extends TestCase {

	protected void setUp() throws Exception {
		
	}

	
	public void testAddsubscriptionTablesnapshot() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path = new LobbyPath(99, "a", 1);
		
		ObjectSubscriptions sub = new ObjectSubscriptions();
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path);
		sub.addSubscription(request, client);
		
		TableSnapshotPacket packet = new TableSnapshotPacket();
		packet.tableid = 1;
		packet.name = "Table_ 1";
		packet.capacity = 10;
		packet.address = "a";
		
		Collection<Client> clients = sub.getSubscribers(path, packet);
		
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
	}
	
	public void testRemoveSubscriptionTablesnapshot() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path = new LobbyPath(99, "a", 1);
		
		ObjectSubscriptions sub = new ObjectSubscriptions();
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path);
		sub.addSubscription(request, client);
		
		TableSnapshotPacket packet = new TableSnapshotPacket();
		packet.tableid = 1;
		Collection<Client> clients = sub.getSubscribers(path, packet);
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
		
		LobbyUnsubscriptionRequest unsub = new LobbyUnsubscriptionRequest(client.getId(), path);
		sub.removeSubscription(unsub, client);
		Collection<Client> clients2 = sub.getSubscribers(path, packet);
		assertEquals(0, clients2.size());
		
	}
	
	
	public void testRemoveAll() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "a", 1);
		LobbyPath path2 = new LobbyPath(99, "b", 2);
		
		ObjectSubscriptions sub = new ObjectSubscriptions();
		LobbySubscriptionRequest request1 = new LobbySubscriptionRequest(client.getId(), path1);
		LobbySubscriptionRequest request2 = new LobbySubscriptionRequest(client.getId(), path2);
		sub.addSubscription(request1, client);
		sub.addSubscription(request2, client);
		
		TableSnapshotPacket packet1 = new TableSnapshotPacket();
		packet1.tableid = 1;
		
		TableSnapshotPacket packet2 = new TableSnapshotPacket();
		packet2.tableid = 2;
		
		Collection<Client> clients = sub.getSubscribers(path1, packet1);
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
		
		clients = sub.getSubscribers(path2, packet2);
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
		
		sub.removeAllSubscriptionsForClient(client);
		
		Collection<Client> clients2 = sub.getSubscribers(path1, packet1);
		Collection<Client> clients3 = sub.getSubscribers(path1, packet2);
		assertEquals(0, clients2.size());
		assertEquals(0, clients3.size());
		
		
	}
	
	public void testAddsubscriptionTableupdate() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path = new LobbyPath(99, "a", 1);
		
		ObjectSubscriptions sub = new ObjectSubscriptions();
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path);
		sub.addSubscription(request, client);
		
		TableUpdatePacket packet = new TableUpdatePacket();
		packet.tableid = 1;
		
		Collection<Client> clients = sub.getSubscribers(path, packet);
		
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
	}
	
	public void testAddsubscriptionMtt() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path = new LobbyPath(LobbyPathType.MTT, 99, "a/b/c", 1);
		
		ObjectSubscriptions sub = new ObjectSubscriptions();
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path);
		sub.addSubscription(request, client);
		
		TournamentSnapshotPacket packet = new TournamentSnapshotPacket();
		packet.mttid = 1;
		
		Collection<Client> clients = sub.getSubscribers(path, packet);
		
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
	}
	
	public void testAddsubscriptionMttUpdate() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path = new LobbyPath(LobbyPathType.MTT, 99, "a/b/c", 1);
		
		ObjectSubscriptions sub = new ObjectSubscriptions();
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path);
		sub.addSubscription(request, client);
		
		TournamentUpdatePacket packet = new TournamentUpdatePacket();
		packet.mttid = 1;
		
		Collection<Client> clients = sub.getSubscribers(path, packet);
		
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
	}
	
	
	public void testManysubcribers() throws Exception {
		
		LobbyPath path1 = new LobbyPath(99, "a", 1);
		LobbyPath path2 = new LobbyPath(99, "b", 2);
		LobbyPath path3 = new LobbyPath(LobbyPathType.MTT, 99, "a", 1);
		
		MockClient client1 = new MockClient(11);
		MockClient client2 = new MockClient(22);
		MockClient client3 = new MockClient(33);
		MockClient client4 = new MockClient(44);
		MockClient client5 = new MockClient(55);
		
		ObjectSubscriptions sub = new ObjectSubscriptions();
		
		sub.addSubscription(new LobbySubscriptionRequest(client1.getId(), path1), client1);
		sub.addSubscription(new LobbySubscriptionRequest(client2.getId(), path2), client2);
		sub.addSubscription(new LobbySubscriptionRequest(client3.getId(), path3), client3);
		sub.addSubscription(new LobbySubscriptionRequest(client4.getId(), path1), client4);
		sub.addSubscription(new LobbySubscriptionRequest(client5.getId(), path2), client5);
		
		TableSnapshotPacket packet1 = new TableSnapshotPacket();
		packet1.tableid = 1;

		TableSnapshotPacket packet2 = new TableSnapshotPacket();
		packet2.tableid = 2;
		
		TableUpdatePacket packet3 = new TableUpdatePacket();
		packet3.tableid = 1;
		
		TableUpdatePacket packet4 = new TableUpdatePacket();
		packet4.tableid = 2;
		
		TournamentSnapshotPacket packet5 = new TournamentSnapshotPacket();
		packet5.mttid = 1;

		TournamentUpdatePacket packet6 = new TournamentUpdatePacket();
		packet6.mttid = 1;
		
		Collection<Client> clients1 = sub.getSubscribers(path1, packet1);
		assertEquals(2, clients1.size());
		assertTrue(clients1.contains(client1));
		assertTrue(clients1.contains(client4));
		
		Collection<Client> clients11 = sub.getSubscribers(path1, packet3);
		assertEquals(2, clients11.size());
		assertTrue(clients11.contains(client1));
		assertTrue(clients11.contains(client4));
		
		Collection<Client> clients2 = sub.getSubscribers(path2, packet2);
		assertEquals(2, clients2.size());
		assertTrue(clients2.contains(client2));
		assertTrue(clients2.contains(client5));
		
		// test with wrong packet implementation (but correct object id)
		Collection<Client> clients3 = sub.getSubscribers(path3, packet1);
		assertEquals(0, clients3.size());
		
		Collection<Client> clients4 = sub.getSubscribers(path3, packet5);
		assertEquals(1, clients4.size());
		assertTrue(clients4.contains(client3));
		
	}
	
	public void testRemoveAllMemLeak_Ticket658() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "a", 1);
		LobbyPath path2 = new LobbyPath(99, "a", 2);
		
		ObjectSubscriptions sub = new ObjectSubscriptions();
		LobbySubscriptionRequest request1 = new LobbySubscriptionRequest(client.getId(), path1);
		LobbySubscriptionRequest request2 = new LobbySubscriptionRequest(client.getId(), path2);
		sub.addSubscription(request1, client);
		sub.addSubscription(request2, client);
		
		TableSnapshotPacket packet1 = new TableSnapshotPacket();
		packet1.tableid = 1;
		
		TableSnapshotPacket packet2 = new TableSnapshotPacket();
		packet2.tableid = 2;
		
		Collection<Client> clients = sub.getSubscribers(path1, packet1);
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
		
		clients = sub.getSubscribers(path2, packet2);
		assertEquals(1, clients.size());
		assertEquals(11, clients.iterator().next().getId());
		
		sub.removeObject(path1, 1);
		
		// Inspect internal data structures for memory leaks and what not.
		PathContainer container = sub.getContainer(path1);
		ConcurrentMap<Integer, ConcurrentHashMap<Client, Client>> subMap = container.getSubscriptionMap();
		
		System.out.println("Sub Map: "+subMap);
		
		assertEquals(1, subMap.size());
		assertEquals(1, subMap.get(2).size()); // Object 2 is still alive and have 1 subscriber
	}
	
}
