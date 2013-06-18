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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.SystemMessageAction;
import com.cubeia.firebase.api.game.GameNotifier;

public class MockNotifier implements GameNotifier {

	/**
	 * We will store all actions generated here
	 */
	public List<GameAction> actions = new ArrayList<GameAction>();
	
	public void broadcast(SystemMessageAction msg) {
		actions.add(msg);
	}
	
	public void clear() {
		actions = new ArrayList<GameAction>();
	}
	
	public void notifyAllPlayers(GameAction action) {
		actions.add(action);
	}

	public void notifyAllPlayers(GameAction action, boolean watchers) {
		actions.add(action);
	}

	public void notifyAllPlayers(Collection<? extends GameAction> action) {
		actions.addAll(action);
	}

	public void notifyAllPlayers(Collection<? extends GameAction> action, boolean watchers) {
		actions.addAll(action);
	}

	public void notifyAllPlayersExceptOne(GameAction action, int playerid) {
		actions.add(action);
	}

	public void notifyAllPlayersExceptOne(GameAction action, int playerid, boolean watchers) {
		actions.add(action);
	}

	public void notifyAllPlayersExceptOne(Collection<? extends GameAction> action, int playerid) {
		actions.addAll(action);
	}

	public void notifyAllPlayersExceptOne(Collection<? extends GameAction> action, int playerid, boolean watchers) {
		actions.addAll(action);
	}

	public void notifyPlayer(int playerid, GameAction action) {
		actions.add(action);
	}

	public void notifyPlayer(int playerid, Collection<? extends GameAction> action) {
		actions.addAll(action);
	}

	public void sendToClient(int playerid, GameAction action) {
		actions.add(action);
	}

	public void sendToClient(int playerId, Collection<? extends GameAction> action) {
		actions.addAll(action);
	}
	
	
}
