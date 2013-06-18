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
package com.cubeia.firebase.api.lobby;

import junit.framework.TestCase;

import com.cubeia.firebase.api.lobby.LobbyPath;
import com.cubeia.firebase.util.Serializer;

public class LobbyPathTestExternalize extends TestCase {

	
	public void testReadWrite() throws Exception {
		LobbyPath path = new LobbyPath(1, "a/b/c");
		
		// Write
		byte[] bs = new Serializer().serialize(path);
		
		// Read
		LobbyPath read = (LobbyPath)new Serializer().deserialize(bs);
		
		assertEquals(path.getArea(), read.getArea());
		assertEquals(path.getDomain(), read.getDomain());
	}
}
