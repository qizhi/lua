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
package com.cubeia.firebase.api.game.player;

import junit.framework.TestCase;

import com.cubeia.firebase.util.Serializer;

public class GenericPlayerTestExternalized extends TestCase {
	
	public void testReadWrite() throws Exception {
		GenericPlayer player = new GenericPlayer(1, "Apan");
		player.setSeatId(10);
		player.setStatus(PlayerStatus.TABLE_LOCAL);
		
		// Write
		byte[] bs = new Serializer().serialize(player);
		
		// Read
		GenericPlayer read = (GenericPlayer)new Serializer().deserialize(bs);
		
		assertEquals(player.getName(), read.getName());
		assertEquals(player.getSeatId(), read.getSeatId());
		assertEquals(player.getPlayerId(), read.getPlayerId());
		assertEquals(player.getStatus(), read.getStatus());
	}
	
	
}
