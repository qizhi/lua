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
package com.cubeia.test.systest.game.tests;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;

public class TablePingProcessor extends ProcessorBase {

	private static final long serialVersionUID = -7606684121393930377L;

	@Override
	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table) {
		System.out.println("KKKKK: " + playerId);
		GameDataAction act = createDataAction(playerId, table.getId(), gamePacket);
		table.getNotifier().notifyAllPlayers(act);
	}
}
