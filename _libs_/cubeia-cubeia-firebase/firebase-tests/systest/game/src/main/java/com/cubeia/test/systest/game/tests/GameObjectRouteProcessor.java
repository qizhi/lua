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
import com.cubeia.test.systest.io.protocol.ActivatorGameRoutePacket;

public class GameObjectRouteProcessor extends ProcessorBase {

	private static final long serialVersionUID = 4251982175743703787L;

	@Override
	protected void doHandle(Object o, Table table) {
		Integer pid = (Integer) o;
		ActivatorGameRoutePacket p = new ActivatorGameRoutePacket(pid.intValue(), table.getId());
		GameDataAction act = createDataAction(pid, table.getId(), p);
		table.getNotifier().notifyPlayer(pid, act);
	}
}
