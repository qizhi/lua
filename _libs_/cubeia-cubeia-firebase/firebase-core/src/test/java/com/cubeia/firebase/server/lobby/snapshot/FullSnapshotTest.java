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
package com.cubeia.firebase.server.lobby.snapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.api.game.lobby.DefaultTableAttributes;
import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.protocol.Param;
import com.cubeia.firebase.io.protocol.TableSnapshotPacket;
import com.cubeia.firebase.io.protocol.TableUpdatePacket;
import com.cubeia.firebase.mock.MockLobby;
import com.cubeia.firebase.server.lobby.snapshot.DeltaSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.FullSnapshot;
import com.cubeia.firebase.server.lobby.snapshot.NodeChangeDTO;

import junit.framework.TestCase;

public class FullSnapshotTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testNewTable() {
		LobbyPath lobbyPath = new LobbyPath();
		FullSnapshot snapshot = new FullSnapshot(new MockLobby(), lobbyPath);
		DeltaSnapshot delta = new DeltaSnapshot(lobbyPath);
		
		LobbyPath changePath = new LobbyPath(99, "/a");
		Map<Object, Object> changedData = new HashMap<Object, Object>();
		changedData.put(DefaultTableAttributes._SEATED.name(), 2);
		
		Map<Object, Object> allData = new HashMap<Object, Object>();
		populateStaticData(allData);
		
		NodeChangeDTO change = new NodeChangeDTO(changePath, changedData, false, allData);
		
		snapshot.reportChange(change, delta);
		
		Collection<ProtocolObject> fullData = snapshot.getLobbyData();
		Collection<ProtocolObject> deltaFullData = delta.getLobbyData();
		Collection<ProtocolObject> deltaData = delta.deltaPacketsMap.values();
		
		assertEquals(1, fullData.size());
		assertEquals(1, deltaFullData.size());
		assertEquals(0, deltaData.size());
	}
	
	public void testTableMissingID() {
		LobbyPath lobbyPath = new LobbyPath();
		FullSnapshot snapshot = new FullSnapshot(new MockLobby(), lobbyPath);
		DeltaSnapshot delta = new DeltaSnapshot(lobbyPath);
		
		LobbyPath changePath = new LobbyPath(99, "/a");
		Map<Object, Object> changedData = new HashMap<Object, Object>();
		changedData.put(DefaultTableAttributes._SEATED.name(), 2);
		
		Map<Object, Object> allData = new HashMap<Object, Object>();
		
		NodeChangeDTO change = new NodeChangeDTO(changePath, changedData, false, allData);
		
		snapshot.reportChange(change, delta);
		
		Collection<ProtocolObject> fullData = snapshot.getLobbyData();
		Collection<ProtocolObject> deltaFullData = delta.packets.values();
		Collection<ProtocolObject> deltaData = delta.deltaPacketsMap.values();
		
		assertEquals(0, fullData.size());
		assertEquals(0, deltaFullData.size());
		assertEquals(0, deltaData.size());
	}
	
	public void testTablePartialUpdate() {
		System.out.println(" -------- PARTIAL --------- ");
		LobbyPath lobbyPath = new LobbyPath();
		FullSnapshot snapshot = new FullSnapshot(new MockLobby(), lobbyPath);
		DeltaSnapshot delta = new DeltaSnapshot(lobbyPath);
		
		LobbyPath changePath = new LobbyPath(99, "/a");
		Map<Object, Object> changedData = new HashMap<Object, Object>();
		changedData.put(DefaultTableAttributes._ID.name(), 33);
		
		Map<Object, Object> allData = new HashMap<Object, Object>();
		allData.put(DefaultTableAttributes._ID.name(), 33);
		
		NodeChangeDTO change = new NodeChangeDTO(changePath, changedData, false, allData);
		
		snapshot.reportChange(change, delta);
		
		Collection<ProtocolObject> fullData = snapshot.getLobbyData();
		Collection<ProtocolObject> deltaFullData = delta.getLobbyData();
		Collection<ProtocolObject> deltaData = delta.deltaPacketsMap.values();
		
		assertEquals(0, fullData.size());
		assertEquals(0, deltaFullData.size());
		assertEquals(0, deltaData.size());
		
		populateStaticData(allData);
		change = new NodeChangeDTO(changePath, changedData, false, allData);
		snapshot.reportChange(change, delta);
		
		fullData = snapshot.getLobbyData();
		deltaFullData = delta.packets.values();
		deltaData = delta.deltaPacketsMap.values();
		
		assertEquals(1, fullData.size());
		assertEquals(1, deltaFullData.size());
		assertEquals(0, deltaData.size());
	}
	
	
	public void testSecondPartialUpdate() {
		System.out.println(" -------- SECONDARY PARTIAL --------- ");
		LobbyPath lobbyPath = new LobbyPath();
		FullSnapshot snapshot = new FullSnapshot(new MockLobby(), lobbyPath);
		DeltaSnapshot delta = new DeltaSnapshot(lobbyPath);
		
		LobbyPath changePath = new LobbyPath(99, "/a");
		Map<Object, Object> changedData = new HashMap<Object, Object>();
		changedData.put(DefaultTableAttributes._SEATED.name(), 1);
		
		Map<Object, Object> allData = new HashMap<Object, Object>();
		populateStaticData(allData);
		allData.put(DefaultTableAttributes._SEATED.name(), 1);
		
		NodeChangeDTO change = new NodeChangeDTO(changePath, changedData, false, allData);
		try {
			System.out.println("REPORT CHANGE 1");
			snapshot.reportChange(change, delta);
		} catch (Exception e) {
			System.err.println("Excep: "+e);
		}
		Collection<ProtocolObject> fullData = snapshot.getLobbyData();
		Collection<ProtocolObject> deltaFullData = delta.packets.values();
		Collection<ProtocolObject> deltaData = delta.deltaPacketsMap.values();
		
		assertEquals(1, fullData.size());
		assertEquals(1, deltaFullData.size());
		assertEquals(0, deltaData.size());
		
		// Secondary iteration
		delta = new DeltaSnapshot(lobbyPath);
		deltaFullData = delta.getLobbyData();
		deltaData = delta.deltaPacketsMap.values();
		fullData = snapshot.getLobbyData();
		assertEquals(1, fullData.size());
		assertEquals(0, deltaFullData.size());
		assertEquals(0, deltaData.size());
		
		changedData.put(DefaultTableAttributes._SEATED.name(), 3);
		changedData.put("TestKey", 123);
		
		allData = new HashMap<Object, Object>();
		populateStaticData(allData);
		allData.put(DefaultTableAttributes._SEATED.name(), 3);
		
		change = new NodeChangeDTO(changePath, changedData, false, allData);
		try {
			System.out.println("REPORT CHANGE 2");
			snapshot.reportChange(change, delta);
		} catch (Exception e) {
			System.err.println("Excep: "+e);
		}
		fullData = snapshot.getLobbyData();
		deltaFullData = delta.packets.values();
		deltaData = delta.deltaPacketsMap.values();
		
		assertEquals(1, fullData.size());
		assertEquals(0, deltaFullData.size());
		assertEquals(1, deltaData.size());
		
		System.out.println("Lobby:");
		System.out.println("Snapshot: "+fullData);
		System.out.println("Delta Full: "+deltaFullData);
		System.out.println("Delta Delta: "+deltaData);
		
	}
	
	
	public void testUpdateRemoval() {
		String myKey = "kalle";
		
		/*
		 * Setup initial state
		 */
		LobbyPath lobbyPath = new LobbyPath();
		FullSnapshot snapshot = new FullSnapshot(new MockLobby(), lobbyPath);
		DeltaSnapshot delta = new DeltaSnapshot(lobbyPath);
		
		LobbyPath changePath = new LobbyPath(99, "/a");
		Map<Object, Object> changedData = new HashMap<Object, Object>();
		changedData.put(myKey, 1);
		
		Map<Object, Object> allData = new HashMap<Object, Object>();
		populateStaticData(allData);
		allData.put(myKey, 1);
		
		NodeChangeDTO change = new NodeChangeDTO(changePath, changedData, false, allData);
		snapshot.reportChange(change, delta);
		
		Collection<ProtocolObject> fullData = snapshot.getLobbyData();
		Collection<ProtocolObject> deltaFullData = delta.packets.values();
		Collection<ProtocolObject> deltaData = delta.deltaPacketsMap.values();
		
		assertEquals(1, fullData.size());
		assertEquals(1, deltaFullData.size());
		assertEquals(0, deltaData.size());
		
		/*
		 * Test remove single attribute
		 */ 
		delta = new DeltaSnapshot(lobbyPath);
		
		// Removing attribute
		changedData.put(myKey, 1);
		
		// Get all data (regenerate)
		allData = new HashMap<Object, Object>();
		populateStaticData(allData);
		allData.put(myKey, 1);
		
		// Create a removal event and report
		change = new NodeChangeDTO(changePath, changedData, true, allData);
		snapshot.reportChange(change, delta);
		
		// Get data
		fullData = snapshot.getLobbyData();
		deltaFullData = delta.packets.values();
		deltaData = delta.deltaPacketsMap.values();
		TableUpdatePacket p = (TableUpdatePacket)deltaData.iterator().next();
			
		// Check
		assertEquals(1, fullData.size()); // One table
		assertEquals(0, deltaFullData.size()); // No new tables
		assertEquals(1, deltaData.size()); // One delta change
		assertEquals(1, p.removedParams.length); // One removed parameter
		assertEquals(myKey, p.removedParams[0]); // "seated" is removed
		
		/*
		 * Test multiple additions removal, ie remove+add+remove+add
		 */
		delta = new DeltaSnapshot(lobbyPath);
		
		// Removing custom attribute
		changedData.put(myKey, 3);
		
		// Get all data (regenerate)
		allData = new HashMap<Object, Object>();
		populateStaticData(allData);
		allData.put(myKey, 1);
		
		// Create events and report
		change = new NodeChangeDTO(changePath, changedData, true, allData);
		snapshot.reportChange(change, delta);
		change = new NodeChangeDTO(changePath, changedData, false, allData);
		snapshot.reportChange(change, delta);
		change = new NodeChangeDTO(changePath, changedData, true, allData);
		snapshot.reportChange(change, delta);
		change = new NodeChangeDTO(changePath, changedData, false, allData);
		snapshot.reportChange(change, delta);
		
		// Get data
		fullData = snapshot.getLobbyData();
		deltaFullData = delta.packets.values();
		deltaData = delta.deltaPacketsMap.values();
		p = (TableUpdatePacket)deltaData.iterator().next();
			
		// Check
		assertEquals(1, fullData.size()); // One table
		assertEquals(0, deltaFullData.size()); // No new tables
		assertEquals(1, deltaData.size()); // One delta change
		assertEquals(0, p.removedParams.length); // No removed parameter
		//assertEquals(DefaultTableAttributes._SEATED.name(), p.removedParams[0]); // "seated" is removed

	}
	
	public void testUpdateRemoval2() {
		String myKey = "kalle";
		
		/*
		 * Setup initial state
		 */
		LobbyPath lobbyPath = new LobbyPath();
		FullSnapshot snapshot = new FullSnapshot(new MockLobby(), lobbyPath);
		DeltaSnapshot delta = new DeltaSnapshot(lobbyPath);
		
		LobbyPath changePath = new LobbyPath(99, "/a");
		Map<Object, Object> changedData = new HashMap<Object, Object>();
		changedData.put(myKey, 1);
		
		Map<Object, Object> allData = new HashMap<Object, Object>();
		populateStaticData(allData);
		allData.put(myKey, 1);
		
		NodeChangeDTO change = new NodeChangeDTO(changePath, changedData, false, allData);
		snapshot.reportChange(change, delta);
		
		Collection<ProtocolObject> fullData = snapshot.getLobbyData();
		Collection<ProtocolObject> deltaFullData = delta.packets.values();
		Collection<ProtocolObject> deltaData = delta.deltaPacketsMap.values();
		
		assertEquals(1, fullData.size());
		assertEquals(1, deltaFullData.size());
		assertEquals(0, deltaData.size());
		
		/*
		 * Test remove single attribute + check full delta
		 */ 
		
		// Removing attribute
		changedData.put(myKey, 1);
		
		// Get all data (regenerate)
		allData = new HashMap<Object, Object>();
		populateStaticData(allData);
		// allData.put(myKey, 1);
		
		// Create a removal event and report
		change = new NodeChangeDTO(changePath, changedData, true, allData);
		snapshot.reportChange(change, delta);
		
		// Get data
		fullData = snapshot.getLobbyData();
		deltaFullData = delta.packets.values();
		deltaData = delta.deltaPacketsMap.values();
			
		// Check
		assertEquals(1, fullData.size()); // One table
		assertEquals(1, deltaFullData.size()); // One new table
		assertEquals(0, deltaData.size()); // No delta change

		TableSnapshotPacket p = (TableSnapshotPacket)deltaFullData.iterator().next();
		assertNull(searchParams(p.params, myKey));
		
	}
	

	// Return param by name from list, null if not found
	private Param searchParams(List<Param> params, String myKey) {
		for (Param p : params) {
			if(myKey.equals(p.key)) {
				return p; // EARLY RETURN
			}
		}
		return null;
	}

	private void populateStaticData(Map<Object, Object> allData) {
		allData.put(DefaultTableAttributes._ID.name(), 1);
		allData.put(DefaultTableAttributes._NAME.name(), "UnitTest-1");
		allData.put(DefaultTableAttributes._CAPACITY.name(), 10);
		allData.put(DefaultTableAttributes._SEATED.name(), 2);
		allData.put(DefaultTableAttributes._GAMEID.name(), 22);
		allData.put(DefaultTableAttributes._WATCHERS.name(), 0);
	}
	
	

}
