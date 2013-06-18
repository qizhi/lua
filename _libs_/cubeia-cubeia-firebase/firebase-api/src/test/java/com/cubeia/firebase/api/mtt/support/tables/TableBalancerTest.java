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
package com.cubeia.firebase.api.mtt.support.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class TableBalancerTest extends TestCase {

	public void testIsBalanced() {
		TableBalancer t = new TableBalancer();
		int playersPerTable = 6;
		assertFalse(t.isBalanced(Arrays.asList(4, 6), playersPerTable));
		assertTrue(t.isBalanced(Arrays.asList(5, 6), playersPerTable));
		assertTrue(t.isBalanced(Arrays.asList(5, 5, 5, 5, 5, 5, 6), playersPerTable));
		assertFalse(t.isBalanced(Arrays.asList(5, 7, 5, 5, 5, 5, 5, 6), playersPerTable));
	}

	public void testShouldTableBeClosed() {
		TableBalancer t = new TableBalancer();
		assertTrue(t.shouldTableBeClosed(2, 2, 2));
		assertTrue(t.shouldTableBeClosed(3, 8, 4));
		assertTrue(t.shouldTableBeClosed(1, 0, 4));
		assertFalse(t.shouldTableBeClosed(1, 1, 4));
		assertFalse(t.shouldTableBeClosed(2, 5, 4));
		assertFalse(t.shouldTableBeClosed(2, 3, 2));
	}

	public void testCloseTable() {
		TableBalancer t = new TableBalancer();
		int playersPerTable = 6;
		Map<Integer, Collection<Integer>> tableToPlayersMap = new HashMap<Integer, Collection<Integer>>();

		tableToPlayersMap.put(1, toList(1, 2, 3));
		tableToPlayersMap.put(2, toList(4, 5, 6, 7));
		tableToPlayersMap.put(3, toList(8, 9, 10));

		List<Move> moves = t.calculateBalancing(tableToPlayersMap, playersPerTable, 3);

		assertEquals(3, moves.size());
		applyBalancing(3, playersPerTable, moves, tableToPlayersMap);
		assertTrue("Tables should now be balanced", t.isBalanced(tableToPlayersMap, playersPerTable));
	}

	public void testBalanceTables() {
		TableBalancer t = new TableBalancer();
		int playersPerTable = 5;
		Map<Integer, Collection<Integer>> tableToPlayersMap = new HashMap<Integer, Collection<Integer>>();

		tableToPlayersMap.put(1, toList(11, 12, 13));
		tableToPlayersMap.put(2, toList(21, 22, 23, 24, 25));

		List<Move> moves = t.calculateBalancing(tableToPlayersMap, playersPerTable, 2);
		assertFalse("Tables should not be balanced before balancing is applied", t.isBalanced(tableToPlayersMap, playersPerTable));
		applyBalancing(2, playersPerTable, moves, tableToPlayersMap);
		assertTrue("Tables should now be balanced", t.isBalanced(tableToPlayersMap, playersPerTable));
	}

	public void testBalanceMultipleTables() {
		TableBalancer t = new TableBalancer();
		int playersPerTable = 5;
		Map<Integer, Collection<Integer>> tableToPlayersMap = new HashMap<Integer, Collection<Integer>>();

		tableToPlayersMap.put(1, toList(11, 12, 13));
		tableToPlayersMap.put(2, toList(21, 22, 23));
		tableToPlayersMap.put(3, toList(31, 32, 33, 34, 35));
		tableToPlayersMap.put(4, toList(41, 42, 43, 44, 45));
		tableToPlayersMap.put(5, toList(51, 52, 53, 54, 55));

		List<Move> moves = t.calculateBalancing(tableToPlayersMap, playersPerTable, 4);
		assertEquals(1, moves.size());
		assertFalse("Tables should not be balanced before balancing is applied", t.isBalanced(tableToPlayersMap, playersPerTable));
		applyBalancing(4, playersPerTable, moves, tableToPlayersMap);
		assertFalse("Tables should still not be balanced", t.isBalanced(tableToPlayersMap, playersPerTable));

		moves = t.calculateBalancing(tableToPlayersMap, playersPerTable, 5);
		applyBalancing(5, playersPerTable, moves, tableToPlayersMap);
		assertTrue("Tables should now be balanced", t.isBalanced(tableToPlayersMap, playersPerTable));
	}

	public void testCloseTableWhenOnlyOneOtherTableNotFull() {
		TableBalancer t = new TableBalancer();
		int playersPerTable = 6;
		Map<Integer, Collection<Integer>> tableToPlayersMap = new HashMap<Integer, Collection<Integer>>();

		tableToPlayersMap.put(17, toList(15, 41, 38, 16, 24, 39));
		tableToPlayersMap.put(18, toList(17, 32, 26, 43, 29, 25));
		tableToPlayersMap.put(19, toList(30, 28, 12));
		tableToPlayersMap.put(20, toList(34, 13, 31));
		tableToPlayersMap.put(21, toList(19, 40, 21, 27, 22, 98));

		List<Move> moves = t.calculateBalancing(tableToPlayersMap, playersPerTable, 19);
		applyBalancing(19, playersPerTable, moves, tableToPlayersMap);
		assertFalse(tableToPlayersMap.containsKey(19));

		for (Collection<Integer> players : tableToPlayersMap.values()) {
			assertTrue(players.size() <= playersPerTable);
		}
		assertTrue("Tables should now be balanced", t.isBalanced(tableToPlayersMap, playersPerTable));
	}

	public void testBalancingToMultipleTables() {
		TableBalancer t = new TableBalancer();
		int playersPerTable = 10;
		Map<Integer, Collection<Integer>> tableToPlayersMap = new HashMap<Integer, Collection<Integer>>();

		tableToPlayersMap.put(1, toList(11, 12, 13, 14, 15, 16, 17));
		tableToPlayersMap.put(2, toList(21, 22, 23, 24, 25, 26, 27));
		tableToPlayersMap.put(3, toList(31, 32, 33, 34, 35, 36, 37, 38, 39, 310));

		List<Move> moves = t.calculateBalancing(tableToPlayersMap, playersPerTable, 3);
		applyBalancing(3, playersPerTable, moves, tableToPlayersMap);
		System.err.println(TableBalancer.dumpTables(tableToPlayersMap));

		for (Collection<Integer> players : tableToPlayersMap.values()) {
			assertTrue(players.size() <= playersPerTable);
		}
		assertTrue("Tables should now be balanced", t.isBalanced(tableToPlayersMap, playersPerTable));
	}

	private List<Integer> toList(Integer... values) {
		return new ArrayList<Integer>(Arrays.asList(values));
	}

	private void applyBalancing(int fromTableId, int capacity, List<Move> moves, Map<Integer, Collection<Integer>> tableToPlayersMap) {
		for (Move move : moves) {
			Integer playerId = move.getPlayerId();
			Integer tableId = move.getDestinationTableId();
			System.err.println("moving player " + playerId + " to table " + tableId);

			tableToPlayersMap.get(fromTableId).remove(playerId);
			if (tableToPlayersMap.get(tableId).size() >= capacity) {
				fail("Tried to move player " + playerId + " from table " + fromTableId + " to table " + tableId
						+ " but that table does not have any empty seats.");
			}
			tableToPlayersMap.get(tableId).add(playerId);
		}
		// Remove table if empty.
		Collection<Integer> remaining = tableToPlayersMap.get(fromTableId);
		if (remaining.isEmpty()) {
			tableToPlayersMap.remove(fromTableId);
		}
	}
}
