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

import org.apache.log4j.Logger;

import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.GameObjectAction;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.InterceptionResponse;
import com.cubeia.firebase.api.game.table.SeatRequest;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableInterceptor;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.test.systest.io.protocol.GeneralResponsePacket;

public class TableListenerProcessor extends ProcessorBase implements TableListener, TableInterceptor {

	private static final long serialVersionUID = -2015036508711945347L;
	
	@Override
	public void handle(GameObjectAction action, Table table) {
		GeneralResponsePacket data = (GeneralResponsePacket) action.getAttachment();
		table.getNotifier().sendToClient(data.pid, super.createDataAction(data.pid, table.getId(), data));
	}
	
	@Override
	public void playerStatusChanged(Table table, int playerId, PlayerStatus status) {
		if(status == PlayerStatus.LEAVING) {
			GeneralResponsePacket pack = new GeneralResponsePacket(playerId);
			GameDataAction action = super.createDataAction(playerId, table.getId(), pack);
			Logger.getLogger(getClass()).info("Notifying player with general response");
			table.getNotifier().notifyAllPlayers(action);
			table.getNotifier().sendToClient(playerId, action);
			GameObjectAction sched = super.createGameObjectAction(table.getId(), pack);
			table.getScheduler().scheduleAction(sched, 500);
		}
	}
	
	@Override
	public InterceptionResponse allowLeave(Table table, int playerId) {
		return new InterceptionResponse(false, 666);
	}
	
	
	
	// --- UNUSED METHODS --- //

	@Override
	public InterceptionResponse allowJoin(Table table, SeatRequest request) {
		return null;
	}

	@Override
	public InterceptionResponse allowReservation(Table table, SeatRequest request) {
		return null;
	}

	@Override
	public void playerJoined(Table table, GenericPlayer player) { }

	@Override
	public void playerLeft(Table table, int playerId) { }

	@Override
	public void watcherJoined(Table table, int playerId) { }

	@Override
	public void watcherLeft(Table table, int playerId) { }

	@Override
	public void seatReserved(Table table, GenericPlayer player) { }

}
