/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package com.cubeia.network.example.rps;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class State implements Serializable {

//	public enum Status {RUNNING, WAITING};

	public Map<Integer, PlayToken> playerTokenMap = new HashMap<Integer, PlayToken>();
	public Map<Integer, Long> playerSessionMap = new HashMap<Integer, Long>();
	
//	private Status status = Status.WAITING;

//	public Status getStatus() {
//		return status;
//	}
//
//	public void setStatus(Status status) {
//		this.status = status;
//	}
	
	public void setToken(int playerId, PlayToken token) {
		playerTokenMap.put(playerId, token);
	}
	
	public Map<Integer, PlayToken> getPlayerTokenMap() {
		return Collections.unmodifiableMap(playerTokenMap);
	}
	
	public void setSessionForPlayer(int playerId, long sessionId) {
		playerSessionMap.put(playerId, sessionId);
	}
	
	public Long getSessionForPlayer(int playerId) {
		return playerSessionMap.get(playerId);
	}
	
}
