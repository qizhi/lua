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
package com.cubeia.firebase.api.action.local;

import java.util.List;

import com.cubeia.firebase.api.action.visitor.LocalActionVisitor;
import com.cubeia.firebase.api.defined.Parameter;

/**
 * Action for waiting lists and join by filters.
 * 
 * @author Fredrik
 *
 */
public class FilteredJoinAction implements LocalAction {

	private int seq = 0;
	
	private int playerId = -1;
	
	/** Lobby tree address, e.g. a/b */
	private String address = "";
	
	List<Parameter<?>> parameters;

	/** ID of the game */
	private final int gameId;
	
	public FilteredJoinAction(int playerId, int gameId) {
		this.playerId = playerId;
		this.gameId = gameId;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPlayerId() {
		return playerId;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<Parameter<?>> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter<?>> parameters) {
		this.parameters = parameters;
	}
	
	

	public int getGameId() {
		return gameId;
	}

	public void visit(LocalActionVisitor visitor) {
		visitor.handle(this);
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	
}
