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
package com.cubeia.firebase.game.table.trans;

import java.util.Set;

import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableSeatingMap;
import com.cubeia.firebase.game.table.FirebaseTable;
import com.cubeia.firebase.game.table.InternalMetaData;
import com.cubeia.firebase.game.table.TableData;
import com.cubeia.firebase.game.table.TableFactory;

import junit.framework.TestCase;

public class StandardTableTest extends TestCase {

	private TableFactory<FirebaseTable> factory;

	protected void setUp() throws Exception {
		factory = new TransactionalTableFactory(null, 10240);
	}

	
	public void testChangeWatcher() throws Exception {
		TableData raw = createTableData();
		Set<Integer> rawSet = raw.getWatchingPlayers();
		addWatchers(rawSet, 2, 5, 6);
		FirebaseTable tab = factory.createTable(raw);
		tab.begin(10);
		tab.getWatcherSet().addWatcher(8);
		tab.getWatcherSet().removeWatcher(2);
		assertTrue(rawSet.contains(2));
		assertTrue(rawSet.contains(5));
		assertTrue(rawSet.contains(6));
		assertFalse(rawSet.contains(8));
		tab.commit();
		assertFalse(rawSet.contains(2));
		assertTrue(rawSet.contains(5));
		assertTrue(rawSet.contains(6));
		assertTrue(rawSet.contains(8));
	}
	
	public void testSeatPlayer() throws Exception {
		TableData raw = createTableData();
		FirebaseTable tab = factory.createTable(raw);
		tab.begin(10);
		tab.getPlayerSet().addPlayer(newPlayer(5, "kalle"), 1);
		tab.getPlayerSet().addPlayer(newPlayer(6, "olle"), 2);
		tab.commit();
		assertEquals(4, raw.getSeats().size());
		assertNull(raw.getSeats().get(0).getPlayer());
		assertNotNull(raw.getSeats().get(1).getPlayer());
		assertNotNull(raw.getSeats().get(2).getPlayer());
		assertNull(raw.getSeats().get(3).getPlayer());
		assertEquals(2, raw.getPlayers().size());
		tab = factory.createTable(raw);
		tab.begin(10);
		tab.getPlayerSet().addPlayer(newPlayer(7, "bertil"), 0);
		tab.commit();
		assertEquals(4, raw.getSeats().size());
		assertNotNull(raw.getSeats().get(0).getPlayer());
		assertNotNull(raw.getSeats().get(1).getPlayer());
		assertNotNull(raw.getSeats().get(2).getPlayer());
		assertNull(raw.getSeats().get(3).getPlayer());
		assertEquals(3, raw.getPlayers().size());
		tab = factory.createTable(raw);
		tab.begin(10);
		TablePlayerSet set = tab.getPlayerSet();
		super.assertEquals(3, set.getPlayerCount());
		TableSeatingMap map = set.getSeatingMap();
		super.assertEquals(3, map.countSeatedPlayers());
		super.assertEquals(3, map.getFirstVacantSeat());
		super.assertEquals(4, map.getNumberOfSeats());
		super.assertTrue(map.hasVacantSeats());
		set.removePlayer(5);
		super.assertEquals(2, set.getPlayerCount());
		super.assertEquals(2, map.countSeatedPlayers());
		super.assertEquals(1, map.getFirstVacantSeat());
		super.assertEquals(4, map.getNumberOfSeats());
		super.assertTrue(map.hasVacantSeats());
		assertNotNull(raw.getSeats().get(1).getPlayer());
		tab.commit();
		assertNull(raw.getSeats().get(1).getPlayer());
	}


	// --- PRIVATE METHDOS -- //
	
	private GenericPlayer newPlayer(int id, String name) {
		return new GenericPlayer(id, name);
	}


	private void addWatchers(Set<Integer> rawSet, int...i) {
		for (int id : i) {
			rawSet.add(id);
		}
	}

	private TableData createTableData() {
		InternalMetaData meta = new InternalMetaData();
		meta.setName("Kalle");
		meta.setGameId(1);
		meta.setTableId(2);
		return factory.createTableData(meta, 4);
	}
}
