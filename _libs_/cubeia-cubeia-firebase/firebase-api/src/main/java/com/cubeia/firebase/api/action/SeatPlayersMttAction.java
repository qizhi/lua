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
package com.cubeia.firebase.api.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

public class SeatPlayersMttAction extends AbstractGameMttAction {
	
	private static final long serialVersionUID = 1L;

	private List<PlayerContainer> players;

	public static class PlayerContainer implements Serializable {
		
		private static final long serialVersionUID = 5394692425882336846L;
		
		private int playerId;
		private String nick;
		private int seatId;
		private Serializable playerData;

		/**
		 * Empty constructor, needed for reflection.
		 * 
		 */
		@SuppressWarnings("unused")
		private PlayerContainer() {
		}
		
		public PlayerContainer(int playerId, String nick, int seatId, Serializable playerData) {
			super();
			this.playerId = playerId;
			this.nick = nick;
			this.seatId = seatId;
			this.playerData = playerData;
		}

		public int getPlayerId() {
			return playerId;
		}

		public String getNick() {
			return nick;
		}

		public int getSeatId() {
			return seatId;
		}

		@Override
		public String toString() {
			return "SeatPlayersMttAction pid[" + playerId + "] seat[" + seatId + "]";
		}

		public Serializable getPlayerData() {
			return playerData;
		}
		
		public void setPlayerData(Serializable playerData) {
			this.playerData = playerData;
		}
	}

	public SeatPlayersMttAction(int mttId, int tableId) {
		super(mttId, tableId);
		players = new ArrayList<PlayerContainer>();
	}

	public void addPlayer(int playerId, String nick, int seatId) {
		addPlayer(playerId, nick, seatId, null);
	}
	
	public void addPlayer(int playerId, String nick, int seatId, Serializable playerData) {
		players.add(new PlayerContainer(playerId, nick, seatId, playerData));
	}		
	
	public void addPlayer(PlayerContainer container) {
		players.add(container);
	}	

	public List<PlayerContainer> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "SeatPlayersMttAction mttid[" + getMttId() + "] tid[" + getTableId() + "] players[" + players + "]";
	}
}
