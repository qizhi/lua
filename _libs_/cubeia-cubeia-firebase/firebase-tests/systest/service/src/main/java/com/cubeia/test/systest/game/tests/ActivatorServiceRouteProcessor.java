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
import com.cubeia.firebase.api.routing.ActivatorAction;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.test.systest.io.protocol.ActivatorTestRequestPacket;

public class ActivatorServiceRouteProcessor extends ProcessorBase {
	
	private final int GAME_ID = 98;
	private final int MTT_ID = 998;

	public void doTest(ServiceContext con, ServiceRouter router, ProtocolObject obj, List<Attribute> attributes) {
		ActivatorTestRequestPacket req = (ActivatorTestRequestPacket)obj;
		router.dispatchToGameActivator(GAME_ID, new ActivatorAction<byte[]>(toBytes(req), attributes));
		router.dispatchToTournamentActivator(MTT_ID, new ActivatorAction<byte[]>(toBytes(req), attributes));
	}
}
