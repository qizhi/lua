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

import static java.util.Collections.max;
import static java.util.Collections.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * Utility class for balancing tables.
 *
 */
public class TableBalancer {

	/**
	 * Checks if tables are in balance.
	 * 
	 * @param playersAtTables
	 * @param playersPerTable
	 * @return <code>true</code> if tables are in balance, <code>false</code> otherwise
	 */
	public boolean isBalanced(List<Integer> playersAtTables, int playersPerTable) {
		return max(playersAtTables) - min(playersAtTables) < 2;
	}
	
	/**
	 * Checks if tables are in balance.
	 * 
	 * @param tableToPlayersMap
	 * @param playersPerTable
	 * @return <code>true</code> if tables are in balance, <code>false</code> otherwise
	 */
	public boolean isBalanced(Map<Integer, Collection<Integer>> tableToPlayersMap, int playersPerTable) {
		List<Integer> playersAtTables = new ArrayList<Integer>(tableToPlayersMap.size());
		
		for (Collection<Integer> players : tableToPlayersMap.values()) {
			playersAtTables.add(players.size());
		}
		
		return isBalanced(playersAtTables, playersPerTable);
	}

	/**
	 * Checks if a table should be closed. 
	 * 
	 * @param currentNumberOfTables
	 * @param numberOfPlayers
	 * @param playersPerTable
	 * @return <code>true</code> if a table should be closed, <code>false</code> otherwise.
	 */
    public boolean shouldTableBeClosed(int currentNumberOfTables, int numberOfPlayers, int playersPerTable) {
        return (currentNumberOfTables * playersPerTable) >= (numberOfPlayers + playersPerTable);
    }

    /**
     * Checks if a table should be closed.
     * 
     * @param tableToPlayerMap
     * @param playersPerTable
     * @return <code>true</code> if a table should be closed, <code>false</code> otherwise.
     */
    public boolean shouldTableBeClosed(Map<Integer, Collection<Integer> > tableToPlayerMap, int playersPerTable) {
    	int numberOfTables = tableToPlayerMap.size();
    	int numberOfPlayers = 0;
    	for (Collection<Integer> players : tableToPlayerMap.values()) {
    		numberOfPlayers += players.size();
    	}
    	
    	return shouldTableBeClosed(numberOfTables, numberOfPlayers, playersPerTable);
    }

    /**
     * Returns a map of moves, mapping playerId to the tableId he should be moved to.
     * 
     * @param tableToPlayerMap
     * @param playersPerTable
     * @param currentTableId
     * @return
     */
    public List<Move> calculateBalancing(    	
        Map<Integer, Collection<Integer>> tableToPlayerMap, 
        int playersPerTable,
        int currentTableId) {
    	        
    	// Maps playerId, to tableId to move to.
    	List<Move> moves = new ArrayList<Move>();
    	
    	if (shouldTableBeClosed(tableToPlayerMap, playersPerTable)) {
    		closeTable(tableToPlayerMap, currentTableId, moves);
    	} else if (!isBalanced(tableToPlayerMap, playersPerTable)) {
    		SortedSet<BalancingTable> sortedTables = sortTablesByNumberOfPlayers(tableToPlayerMap, currentTableId);
    		Collection<Integer> current = new ArrayList<Integer>(tableToPlayerMap.get(currentTableId));
    		BalancingTable min = sortedTables.first();
    		while (current.size() - min.size > 1) {
    			Integer playerToMove = current.iterator().next();
    			current.remove(playerToMove);
    			Move move = move(playerToMove, min, sortedTables);
    			moves.add(move);
    			min = sortedTables.first();
    		}
    	}
    	
        return moves;
    }

	public static String dumpTables(Map<Integer, Collection<Integer>> tableToPlayerMap) {
		StringBuilder s = new StringBuilder();
		for (Entry<Integer, Collection<Integer>> e : tableToPlayerMap.entrySet()) {
			s.append(e.getKey() + "->" + e.getValue() + "\n");
		}
		return s.toString();
	}

	/**
     * Creates a move and updates the sorted tables set.
     * 
     * @param playerToMove
     * @param destinationTable
     * @param sortedTables
     * @return the representation of the move
     */
	private Move move(Integer playerToMove, BalancingTable destinationTable, SortedSet<BalancingTable> sortedTables) {
		sortedTables.remove(destinationTable);
		destinationTable.inc();
		
		// Re add the table so that the sorting is correct.
		sortedTables.add(destinationTable);
		return new Move(playerToMove, destinationTable.getTableId());
	}

	/**
	 * Closes a table by moving all players from the table to other tables.
	 * 
	 * @param tableToPlayerMap
	 * @param currentTableId
	 * @param moves
	 */
	private void closeTable(Map<Integer, Collection<Integer>> tableToPlayerMap, int currentTableId, List<Move> moves) {
		SortedSet<BalancingTable> sortedTables = sortTablesByNumberOfPlayers(tableToPlayerMap, currentTableId);
		
		Collection<Integer> playersAtCurrentTable = tableToPlayerMap.get(currentTableId);
		for (Integer playerId : playersAtCurrentTable) {
			moves.add(move(playerId, sortedTables.first(), sortedTables));
		}
	}

	public static String dumpTables(SortedSet<BalancingTable> sortedTables) {
		StringBuilder s = new StringBuilder();
		for (BalancingTable t : sortedTables) {
			s.append("balanced table: " + t + "\n");
		}
		return s.toString();
	}

	/**
	 * Sorts tables by number of players at the tables, in increasing order.
	 * 
	 * @param tableToPlayerMap
	 * @param excludedTableId
	 * @return a {@link SortedSet} of {@link BalancingTable}
	 */
	private SortedSet<BalancingTable> sortTablesByNumberOfPlayers(Map<Integer, Collection<Integer>> tableToPlayerMap, int excludedTableId) {
		SortedSet<BalancingTable> sortedTables = new TreeSet<BalancingTable>();
		
		for (Entry<Integer, Collection<Integer>> table : tableToPlayerMap.entrySet()) {
			if (table.getKey() != excludedTableId) {
				sortedTables.add(new BalancingTable(table.getKey(), table.getValue().size()));
			}
		}
		
		return sortedTables;
	}
	
	/**
	 * Represents a table for balancing purposes.
	 *
	 */
	private static class BalancingTable implements Comparable<BalancingTable> {
		
		private int tableId;
		private int size;
		
		public BalancingTable(int tableId, int size) {
			super();
			this.tableId = tableId;
			this.size = size;
		}

		public int getTableId() {
			return tableId;
		}

		public int getSize() {
			return size;
		}
		
		public void inc() {
			size++;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + tableId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final BalancingTable other = (BalancingTable) obj;
			if (tableId != other.tableId)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "tableId: " + getTableId() + " size: " + getSize();
		}

		public int compareTo(BalancingTable o) {
			if (getSize() == o.getSize()) {
				return tableId - o.getTableId();
			} else {
				return getSize() - o.getSize();
			}
		}
	}	
}