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

import java.util.HashMap;
import java.util.Map;

import com.cubeia.firebase.api.action.CleanupPlayerAction;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.game.player.GenericPlayer;
import com.cubeia.firebase.api.game.player.PlayerStatus;
import com.cubeia.firebase.api.game.table.Table;
import com.cubeia.firebase.api.game.table.TableListener;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.test.systest.io.protocol.ForceCleanupPacket;
import com.cubeia.test.systest.io.protocol.PlayerStatusRequestPacket;
import com.cubeia.test.systest.io.protocol.PlayerStatusResponsePacket;

public class PlayerStatusProcessor extends ProcessorBase implements TableListener {

	private static final long serialVersionUID = -717467078305276865L;
	
	private Map<Integer, PlayerStatus> statuses = new HashMap<Integer, PlayerStatus>();
	
	@Override
	protected void doTranslatedHandle(ProtocolObject gamePacket, int playerId, Table table) {
		if (gamePacket instanceof ForceCleanupPacket) {
			ForceCleanupPacket req = (ForceCleanupPacket) gamePacket;
			if(statuses.containsKey(req.pid)) {	
				table.getScheduler().scheduleAction(new CleanupPlayerAction(req.pid, table.getId(), statuses.get(req.pid)), 0);
			}
		} else {
			PlayerStatusRequestPacket req = (PlayerStatusRequestPacket) gamePacket;
			PlayerStatus st = statuses.get(req.pid);
			String answer = (st == null ? "" : st.name());
			PlayerStatusResponsePacket resp = new PlayerStatusResponsePacket();
			resp.pid = req.pid;
			resp.status = answer;
			GameDataAction act = createDataAction(playerId, table.getId(), resp);
			table.getNotifier().notifyPlayer(playerId, act);
		}
	}
	
	@Override
	public void playerJoined(Table table, GenericPlayer player) {
		updateStatus(player);
	}

	@Override
	public void playerLeft(Table table, int playerId) {
		statuses.remove(playerId);
	}
	
	@Override
	public void playerStatusChanged(Table table, int playerId, PlayerStatus status) {
		statuses.put(playerId, status);
	}
	
	@Override
	public void seatReserved(Table table, GenericPlayer player) {
		updateStatus(player);
	}
	
	@Override
	public void watcherJoined(Table table, int playerId) { }
	
	@Override
	public void watcherLeft(Table table, int playerId) { }
	
	
	// --- PRIVATE METHODS --- //
	
	private void updateStatus(GenericPlayer player) {
		statuses.put(player.getPlayerId(), player.getStatus());
	}
}
