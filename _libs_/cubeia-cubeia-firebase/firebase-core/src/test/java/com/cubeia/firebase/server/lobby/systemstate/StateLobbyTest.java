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

import java.util.List;

import junit.framework.TestCase;

import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.api.lobby.LobbyPathType;
import com.cubeia.firebase.api.mtt.lobby.DefaultMttAttributes;
import com.cubeia.firebase.api.service.sysstate.SystemStateConstants;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.TableRemovedPacket;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TableUpdatePacket;
import com.cubeia.firebase.io.protocol.TournamentUpdatePacket;
import com.cubeia.firebase.mock.MockClient;
import com.cubeia.firebase.server.lobby.LobbySubscriptionRequest;
import com.cubeia.firebase.server.lobby.LobbyUnsubscriptionRequest;
import com.cubeia.firebase.server.lobby.snapshot.FullSnapshot;
import com.cubeia.firebase.server.service.systemstate.cache.SystemStateCacheHandler;

public class StateLobbyTest extends TestCase {

	private SystemStateCacheHandler cache = new SystemStateCacheHandler("com/cubeia/firebase/systemstate/systemstate-local-test-service.xml");
	private StateLobby lobby;
	
	
	
	/**
	 * Lobby setup:
	 * 
	 * table (99)
	 *   /a
	 *     /1 - table
	 *   /b
	 *     /2 - table
	 *     /3 - table
	 *   /c <empty>
	 *   /d
	 *     /e
	 *       /4 - table
	 *     /f   
	 *       /5 - table
	 *     /g <empty>
	 *     
	 * mtt (99)
	 *   /a
	 *    /1 - mtt
	 * 
	 */
	protected void setUp() throws Exception {
		cache.start();
		cache.updateAttributes(getTableFQN("99/a", 1), SystemStateTestGenerator.createTableAttributes(1, "Table_1"));
		cache.updateAttributes(getTableFQN("99/b", 2), SystemStateTestGenerator.createTableAttributes(2, "Table_2"));
		cache.updateAttributes(getTableFQN("99/b", 3), SystemStateTestGenerator.createTableAttributes(3, "Table_3"));
		
		cache.updateAttributes(getTableFQN("99/d/e", 4), SystemStateTestGenerator.createTableAttributes(4, "Table_4"));
		cache.updateAttributes(getTableFQN("99/d/f", 5), SystemStateTestGenerator.createTableAttributes(5, "Table_5"));
		
		cache.updateAttributes(getTournamentFQN("99/a", 1), SystemStateTestGenerator.createTournamentAttributes(1));
		
		lobby = new StateLobbyAlternative(cache);
		lobby.setBroadcastPeriod(10);
		lobby.start();
		// cache.dumpInfo();
		
		lobby.addPath(SystemStateConstants.TABLE_ROOT_FQN+"99/c");
		lobby.addPath(SystemStateConstants.TABLE_ROOT_FQN+"99/d/g");
		
		Thread.sleep(20);
	}

	
	protected void tearDown() throws Exception {
		cache.stop();
		lobby.stop();
	}

	public void testSubscribe() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "a");
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		
		lobby.subscribe(request, client);
		
		// First subscription we get snapshot 
		Thread.sleep(50);
		assertTrue(packets.size()  == 1);
		
		// Flush all packets
		client.clearSentPackets();
		
		// Trigger a table update
		cache.updateAttribute(getTableFQN("99/a", 1), DefaultTableAttributes._SEATED.name(), 5);
		
		Thread.sleep(110);
		
		assertEquals(1, packets.size());
		assertTrue(packets.get(0) instanceof TableUpdatePacket);
		TableUpdatePacket update = (TableUpdatePacket)packets.get(0);
		assertEquals(1, update.tableid);
		assertEquals(5, update.seated);
		
		// Flush all packets
		client.clearSentPackets();
		
		// Trigger a new table update
		cache.updateAttribute(getTableFQN("99/a", 1), "TEST", "SUCCESS");
		Thread.sleep(70);
		assertEquals(1, packets.size());
		assertTrue(packets.get(0) instanceof TableUpdatePacket);
		TableUpdatePacket update2 = (TableUpdatePacket)packets.get(0);
		assertEquals(1, update2.tableid);
		// There's a length header we do not care about
		assertTrue(new String(update2.params.get(0).value).endsWith("SUCCESS"));
		
	}
	
	public void testSubscribeTournament() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(LobbyPathType.MTT, 99, "a", -1);
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		request.setType(LobbyPathType.MTT);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		
		lobby.subscribe(request, client);
		
		// First subscription we get snapshot
		Thread.sleep(40);
		assertTrue(packets.size() == 1);
		// Flush all packets
		client.clearSentPackets();
		
		// Trigger a table update
		cache.updateAttribute(getTournamentFQN("99/a", 1), DefaultMttAttributes.ACTIVE_PLAYERS.name(), 5);
		Thread.sleep(100);
		assertEquals(1, packets.size());
		assertTrue(packets.get(0) instanceof TournamentUpdatePacket);
		TournamentUpdatePacket update = (TournamentUpdatePacket)packets.get(0);
		assertEquals(1, update.mttid);
		assertEquals(1, update.params.size());
		
	}
	
	
	public void testSubscribeDifferentType() throws Exception {
		MockClient client1 = new MockClient(11);
		MockClient client2 = new MockClient(22);
		
		LobbyPath path1 = new LobbyPath(99, "a");
		LobbyPath path2 = new LobbyPath(LobbyPathType.MTT, 99, "a", -1);
		
		LobbySubscriptionRequest request1 = new LobbySubscriptionRequest(client1.getId(), path1);
		LobbySubscriptionRequest request2 = new LobbySubscriptionRequest(client2.getId(), path2);
		request2.setType(LobbyPathType.MTT);
		
		List<ProtocolObject> packets1 = client1.getPacketsSentToClient();
		List<ProtocolObject> packets2 = client2.getPacketsSentToClient();
		
		lobby.subscribe(request1, client1);
		lobby.subscribe(request2, client2);
		
		// First subscription we get snapshot 
		Thread.sleep(40);
		assertTrue(packets1.size() == 1);
		assertTrue(packets2.size() == 1);
		
		// Flush all packets
		client1.clearSentPackets();
		client2.clearSentPackets();
		
		// Trigger a table update
		cache.updateAttribute(getTableFQN("99/a", 1), DefaultTableAttributes._SEATED.name(), 5);
		Thread.sleep(100);
		assertEquals(0, packets2.size());
		assertEquals(1, packets1.size());
		assertTrue(packets1.get(0) instanceof TableUpdatePacket);
		TableUpdatePacket update = (TableUpdatePacket)packets1.get(0);
		assertEquals(1, update.tableid);
		assertEquals(5, update.seated);
		
		// Flush all packets
		client1.clearSentPackets();
		client2.clearSentPackets();
		
		// Trigger a tournament update
		cache.updateAttribute(getTournamentFQN("99/a", 1), DefaultMttAttributes.ACTIVE_PLAYERS.name(), 7);
		Thread.sleep(40);
		assertEquals(0, packets1.size());
		assertEquals(1, packets2.size());
		assertTrue(packets2.get(0) instanceof TournamentUpdatePacket);
		TournamentUpdatePacket update2 = (TournamentUpdatePacket)packets2.get(0);
		assertEquals(1, update2.mttid);
	}
	
	public void testSubscribeOnEmptyNode() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "c");
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		System.out.println("Pre scubscribe: "+packets);
		lobby.subscribe(request, client);
		
		Thread.sleep(70);
		assertEquals(0, packets.size());
		
		// Trigger a table creation
		cache.updateAttributes(getTableFQN("99/c", 10), SystemStateTestGenerator.createTableAttributes(10, "Table_10"));
		Thread.sleep(400);
		
		// First broadcast of new nodes with subscribers we get snapshot + update (its a bug, but nothing serious)
		System.out.println("Post create: "+packets);
		
		assertEquals(1, packets.size());
		assertTrue(packets.get(0) instanceof TableSnapshotPacket);
		TableSnapshotPacket snap = (TableSnapshotPacket)packets.get(0);
		assertEquals(10, snap.tableid);
	}
	
	
	public void testUnsubscribe() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "d");
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		
		lobby.subscribe(request, client);
		
		// First subscription we get snapshot + update (its a bug, but nothing serious)
		Thread.sleep(40);
		assertTrue(packets.size() > 1);
		
		// Flush all packets
		client.clearSentPackets();
		
		// Trigger update on all tables
		cache.updateAttribute(getTableFQN("99/d/e", 4), DefaultTableAttributes._SEATED.name(), 2);
		cache.updateAttribute(getTableFQN("99/d/f", 5), DefaultTableAttributes._SEATED.name(), 2);
		
		Thread.sleep(40);
		assertEquals(2, packets.size());
		TableUpdatePacket update4 = (TableUpdatePacket)packets.get(0);
		assertEquals(2, update4.seated);
		TableUpdatePacket update5 = (TableUpdatePacket)packets.get(1);
		assertEquals(2, update5.seated);
		
		// Flush all packets
		client.clearSentPackets();
		
		// Unsubscribe to one branch
		LobbyPath path2 = new LobbyPath(99, "d/e");
		LobbyUnsubscriptionRequest unsub = new LobbyUnsubscriptionRequest(client.getId(), path2);
		lobby.unsubscribe(unsub, client);
		
		// Trigger update on all tables
		cache.updateAttribute(getTableFQN("99/d/e", 4), DefaultTableAttributes._SEATED.name(), 6);
		cache.updateAttribute(getTableFQN("99/d/f", 5), DefaultTableAttributes._SEATED.name(), 7);
		Thread.sleep(40);
		assertEquals(1, packets.size());
		
		// Flush all packets
		client.clearSentPackets();
		
		// unsubcribe to all
		lobby.unsubscribeAll(client);
		// Trigger update on all tables
		cache.updateAttribute(getTableFQN("99/d/e", 4), DefaultTableAttributes._SEATED.name(), 3);
		cache.updateAttribute(getTableFQN("99/d/f", 5), DefaultTableAttributes._SEATED.name(), 4);
		Thread.sleep(40);
		assertEquals(0, packets.size());
		
	}
	
	
	public void testSubscribeToObjectSnapshot() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "b", 2);
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		
		lobby.subscribeToLobbyObject(request, client);
		Thread.sleep(40);
		assertEquals(1, packets.size());
		assertTrue(packets.get(0) instanceof TableSnapshotPacket);
		TableSnapshotPacket snap = (TableSnapshotPacket)packets.get(0);
		assertEquals(2, snap.tableid);
		
	}
	
	public void testSubscribeToNonExistingObjectt() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "x", 6);
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		
		lobby.subscribeToLobbyObject(request, client);
		Thread.sleep(40);
		
		assertEquals(0, packets.size());
		
	}
	
	public void testSubscribeToObject() throws Exception {
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "b", 2);
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		
		lobby.subscribeToLobbyObject(request, client);
		Thread.sleep(40);
		
		assertEquals(1, packets.size());
		assertTrue(packets.get(0) instanceof TableSnapshotPacket);
		TableSnapshotPacket snap = (TableSnapshotPacket)packets.get(0);
		assertEquals(2, snap.tableid);
		

		// Flush all packets
		client.clearSentPackets();
		
		// Trigger update on tables
		cache.updateAttribute(getTableFQN("99/b", 2), DefaultTableAttributes._SEATED.name(), 4);
		cache.updateAttribute(getTableFQN("99/b", 3), DefaultTableAttributes._SEATED.name(), 6);
		
		Thread.sleep(40);
		assertEquals(1, packets.size());
		assertTrue(packets.get(0) instanceof TableUpdatePacket);
		TableUpdatePacket update = (TableUpdatePacket)packets.get(0);
		assertEquals(4, update.seated);
	}
	
	public void testSubscribeToObject2() throws Exception {
		MockClient client1 = new MockClient(11);
		MockClient client2 = new MockClient(2);
		
		LobbyPath path1 = new LobbyPath(99, "b");
		LobbyPath path2 = new LobbyPath(99, "b", 2);
		
		LobbySubscriptionRequest request1 = new LobbySubscriptionRequest(client1.getId(), path1);
		LobbySubscriptionRequest request2 = new LobbySubscriptionRequest(client2.getId(), path2);
		
		List<ProtocolObject> packets1 = client1.getPacketsSentToClient();
		List<ProtocolObject> packets2 = client2.getPacketsSentToClient();
		
		// First we add a regular subscription for Client 1
		lobby.subscribe(request1, client1);
		Thread.sleep(40);
		assertTrue(packets1.size() > 1);
		
		// Then we add an object subscription for Client 2
		lobby.subscribeToLobbyObject(request2, client2);
		Thread.sleep(40);
		assertEquals(1, packets2.size());
		assertTrue(packets2.iterator().next() instanceof TableSnapshotPacket);
		
		// Flush all packets
		packets1.clear();
		packets2.clear();
		
		// Now add an object specific subscription for Client1, this subscription will be redundant with the current sub
		lobby.subscribeToLobbyObject(request2, client1);
		assertEquals(1, packets1.size());
		assertTrue(packets1.iterator().next() instanceof TableSnapshotPacket);
		packets1.clear();
		
		// Trigger update on table 2
		cache.updateAttribute(getTableFQN("99/b", 2), DefaultTableAttributes._SEATED.name(), 1);
		Thread.sleep(40);
		
		// Verify updates
		assertEquals(1, packets1.size());
		assertTrue(packets1.iterator().next() instanceof TableUpdatePacket);
		TableUpdatePacket update1 = (TableUpdatePacket)packets1.iterator().next();
		assertEquals(2, update1.tableid);
		assertEquals(1, update1.seated);
		assertEquals(1, packets2.size());
		assertTrue(packets2.iterator().next() instanceof TableUpdatePacket);
	
		// Flush all packets
		packets1.clear();
		packets2.clear();
		
		// Trigger update on table 3
		cache.updateAttribute(getTableFQN("99/b", 3), DefaultTableAttributes._SEATED.name(), 2);
		Thread.sleep(40);
		
		// Verify updates
		assertEquals(1, packets1.size());
		assertTrue(packets1.iterator().next() instanceof TableUpdatePacket);
		TableUpdatePacket update2 = (TableUpdatePacket)packets1.iterator().next();
		assertEquals(3, update2.tableid);
		assertEquals(2, update2.seated);
		assertEquals(0, packets2.size());
		
		
		// Flush all packets
		packets1.clear();
		packets2.clear();
		
		// Unsubscribe Client1 on regular lobby
		LobbyUnsubscriptionRequest unsub1 = new LobbyUnsubscriptionRequest(client1.getId(), path1);
		lobby.unsubscribe(unsub1, client1);
		
		// Unsubscribe Client2 on table 2
		LobbyUnsubscriptionRequest unsub2 = new LobbyUnsubscriptionRequest(client1.getId(), path2);
		lobby.unsubscribeToLobbyObject(unsub2, client2);
		
		// Trigger update on table 2
		cache.updateAttribute(getTableFQN("99/b", 2), DefaultTableAttributes._SEATED.name(), 1);
		Thread.sleep(40);
		
		// Verify updates. Client1 should get the same
		assertEquals(1, packets1.size());
		assertTrue(packets1.iterator().next() instanceof TableUpdatePacket);
		TableUpdatePacket update3 = (TableUpdatePacket)packets1.iterator().next();
		assertEquals(2, update3.tableid);
		assertEquals(1, update3.seated);
		assertEquals(0, packets2.size());
		
		// Flush all packets
		packets1.clear();
		packets2.clear();
		
		// Unsubscribe Client1 on everything
		lobby.unsubscribeAll(client1);
		
		// Trigger update on tables
		cache.updateAttribute(getTableFQN("99/b", 2), DefaultTableAttributes._SEATED.name(), 3);
		cache.updateAttribute(getTableFQN("99/b", 3), DefaultTableAttributes._SEATED.name(), 3);
		Thread.sleep(40);
		
		assertEquals(0, packets1.size());
	}
	
	
	public void testRemoveTable() throws Exception {
		cache.updateAttributes(getTableFQN("99/y", 1), SystemStateTestGenerator.createTableAttributes(1, "Table_1"));
		Thread.sleep(50);
		
		MockClient client = new MockClient(11);
		LobbyPath path1 = new LobbyPath(99, "y");
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		
		lobby.subscribe(request, client);
		
		// First subscription we get a snapshot
		Thread.sleep(50);
		assertTrue(packets.size() == 1);
		
		// Flush all packets
		client.clearSentPackets();
		
		// Trigger a table update
		cache.removeNode(getTableFQN("99/y", 1));
		
		Thread.sleep(110);
		
		assertEquals(1, packets.size());
		assertTrue(packets.get(0) instanceof TableRemovedPacket);
		TableRemovedPacket packet = (TableRemovedPacket)packets.get(0);
		assertEquals(1, packet.tableid);
		
		// Flush all packets
		client.clearSentPackets();
		
		// Check so that the full snapshot node is removed
		FullSnapshot fullSnapshot = lobby.getSnapshotGenerator(LobbyPathType.TABLES).getFullSnapshots().get(new LobbyPath(LobbyPathType.TABLES, 99, "y", -1));
		assertNull(fullSnapshot);
	}
	
	public void testRemoveTableConcurrent() throws Exception {
		// Original table
		cache.updateAttributes(getTableFQN("99/x", 1), SystemStateTestGenerator.createTableAttributes(1, "Table_1"));
		Thread.sleep(50);
		
		MockClient client = new MockClient(123);
		LobbyPath path1 = new LobbyPath(99, "x");
		LobbySubscriptionRequest request = new LobbySubscriptionRequest(client.getId(), path1);
		List<ProtocolObject> packets = client.getPacketsSentToClient();
		
		lobby.subscribe(request, client);
		
		// First subscription we get a snapshot
		Thread.sleep(50);
		
		assertTrue(packets.size() == 1);
		
		// Flush all packets
		client.clearSentPackets();
		
		// Remove Table 1 and directly after create Table 2
		cache.removeNode(getTableFQN("99/x", 1));
		cache.updateAttributes(getTableFQN("99/x", 3), SystemStateTestGenerator.createTableAttributes(3, "Table_3"));

		Thread.sleep(110);
		
		assertEquals(2, packets.size());
		// assertTrue(packets.get(0) instanceof TableSnapshotPacket);
		// assertTrue(packets.get(1) instanceof TableRemovedPacket);
		
		// Flush all packets
		client.clearSentPackets();
		
		// Check so that the full snapshot node is correct size
		FullSnapshot fullSnapshot = lobby.getSnapshotGenerator(LobbyPathType.TABLES).getFullSnapshots().get(new LobbyPath(LobbyPathType.TABLES, 99, "x", -1));
		assertEquals(1, fullSnapshot.getLobbyData().size());
	}
	
	public void testGetSnapshot() throws Exception {
		TableSnapshotPacket snapshot = (TableSnapshotPacket) lobby.getSnapshot(LobbyPathType.TABLES, 2);
		assertEquals(2, snapshot.tableid);
		
		snapshot = (TableSnapshotPacket) lobby.getSnapshot(LobbyPathType.TABLES, 4);
		assertEquals(4, snapshot.tableid);		
	}
	
	public void testGetSnapshotForNonExistingObject() throws Exception {
		TableSnapshotPacket snapshot = (TableSnapshotPacket) lobby.getSnapshot(LobbyPathType.TABLES, 2);
		assertEquals(2, snapshot.tableid);
		
		snapshot = (TableSnapshotPacket) lobby.getSnapshot(LobbyPathType.TABLES, 10);
		assertEquals(null, snapshot);		
	}	
	
	private String getTableFQN(String address, int objectId) {
		return SystemStateConstants.TABLE_ROOT_FQN+address+"/"+objectId;
	}
	
	private String getTournamentFQN(String address, int objectId) {
		return SystemStateConstants.TOURNAMENT_ROOT_FQN+address+"/"+objectId;
	}
}
