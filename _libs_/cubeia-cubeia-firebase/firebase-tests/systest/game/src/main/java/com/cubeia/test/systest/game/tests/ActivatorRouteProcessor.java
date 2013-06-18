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

import java.util.List;

import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.test.systest.io.protocol.ActivatorTestGameResponsePacket;
import com.cubeia.test.systest.io.protocol.ActivatorTestRequestPacket;

public class ActivatorRouteProcessor extends ProcessorBase {

	private static final long serialVersionUID = -373051235514420424L;

	@Override
	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table) {
		this.doTranslatedHandle(gamePacket, playerId, table, null);
	}
	
	@Override
	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table, List<Attribute> atts) {
		ActivatorTestRequestPacket req = (ActivatorTestRequestPacket) gamePacket;
		ActivatorTestGameResponsePacket resp = new ActivatorTestGameResponsePacket(req.tableId);
		GameDataAction act = createDataAction(req.clientId, table.getId(), resp);
		if(atts != null) {
			act.getAttributes().addAll(atts);
		}
		table.getNotifier().sendToClient(req.clientId, act);
	}
}
