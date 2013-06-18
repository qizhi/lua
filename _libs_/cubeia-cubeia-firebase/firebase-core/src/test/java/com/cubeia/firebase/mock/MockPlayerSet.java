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
package com.cubeia.firebase.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.table.TablePlayerSet;
import com.cubeia.firebase.api.game.table.TableSeatingMap;
import com.cubeia.firebase.api.util.UnmodifiableSet;

public class MockPlayerSet implements TablePlayerSet {

	Map<Integer, GenericPlayer> playerMap = new HashMap<Integer, GenericPlayer>();
	
	public void addPlayer(GenericPlayer player, int seat) {
		playerMap.put(player.getPlayerId(), player);
	}

	public GenericPlayer getPlayer(int playerId) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPlayerCount() {		
		return 0;
	}

	public UnmodifiableSet<GenericPlayer> getPlayers() {
		return new UnmodifiableSet<GenericPlayer>() {

			Collection<GenericPlayer> players = playerMap.values();
			
			public boolean contains(GenericPlayer object) {
				return players.contains(object);
			}

			public Iterator<GenericPlayer> iterator() {
				return players.iterator();
			}
		};
	}

	public TableSeatingMap getSeatingMap() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removePlayer(int playerId) {
		// TODO Auto-generated method stub

	}

    public void seatPlayer(GenericPlayer player, int seat) {
        // TODO Auto-generated method stub
        
    }

    public void unseatPlayer(int playerId) {
        // TODO Auto-generated method stub
        
    }

}
