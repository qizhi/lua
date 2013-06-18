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
package com.cubeia.firebase.game.table;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.util.Serializer;

public class StandardTableDataTestExternalize extends TestCase {

	public void testReadWrite() throws Exception {
		StandardTableData table = new PlainTableData(10);
		
		
		byte[] scheduled = new byte[]{ 1, 2, 3};
		byte[] state = new byte[]{ 4, 5, 6, 7};
		Map<Integer, GenericPlayer> players = createPlayers(10);
		Set<Integer> watchers = createWatchers(4);
		
		
		table.setScheduledActions(scheduled);
		table.setStateData(state);
		table.setPlayers(players);
		table.setWatchingPlayers(watchers);
		
		
		
		// Write
		byte[] bs = new Serializer().serialize(table);
		
		// Read
		StandardTableData read = (StandardTableData)new Serializer().deserialize(bs);
		
		assertEquals(read.getStateData(), read.getStateData());
		assertEquals(read.getScheduledActions(), read.getScheduledActions());
		assertEquals(read.getPlayers(), read.getPlayers());
		assertEquals(read.getWatchingPlayers(), read.getWatchingPlayers());
		
	}
	
	
	private Map<Integer, GenericPlayer> createPlayers(int count) {
		Map<Integer, GenericPlayer> map = new HashMap<Integer, GenericPlayer>();
		for (int i = 0; i < count; i ++) {
			GenericPlayer player = new GenericPlayer(i, "Apan_"+i);
			map.put(player.getPlayerId(), player);
		}
		return map;
	}
	
	
	private Set<Integer> createWatchers(int count) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < count; i ++) {
			set.add(i);
		}
		return set;
	}
}
