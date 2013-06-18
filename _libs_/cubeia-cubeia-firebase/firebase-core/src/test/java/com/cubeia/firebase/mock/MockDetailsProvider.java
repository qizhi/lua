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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cubeia.firebase.api.game.table.ExtendedDetailsProvider;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.protocol.Param;

public class MockDetailsProvider implements ExtendedDetailsProvider {

	public Map<Integer, List<Param>> details = new HashMap<Integer, List<Param>>();
	
	public List<Param> getExtendedDetails(Table table, int playerId, boolean fromLobby) {
		return details.get(playerId);
	}

}
