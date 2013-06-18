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
package com.cubeia.firebase.api.action.service;

import java.util.LinkedList;
import java.util.List;

import com.cubeia.firebase.api.action.AbstractAction;
import com.cubeia.firebase.api.action.Attribute;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

/**
 * Action to transport service data to and from a service.
 */
public class ClientServiceAction extends AbstractAction implements ServiceAction {

	private static final long serialVersionUID = 2226259072850766001L;

	private int playerId = -1;
	private final byte[] data;
	private int seq = -1;
	
	private final List<Attribute> attributes = new LinkedList<Attribute>();
	
	public ClientServiceAction(int playerId, int seq, byte[] data) {
		this.playerId = playerId;
		this.data = data;
		this.seq = seq;
	}	
	
	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	public int getSeq() {
		return seq;
	}
	
	public byte[] getData() {
		return data;
	}

	
	/**
	 * Get the player id this action is from, or directed to. If
	 * this action is broadcast from a service to multiple clients
	 * this will be -1.
	 */
	public int getPlayerId() {
		return playerId;
	}

	/**
	 * Not applicable.
	 * TODO: This is inherently bad object oriented design.
	 * We should revise the Action models.
	 */
	public int getTableId() {
		return -1;
	}

	/**
	 * Not applicable.
	 * This is inherently bad object oriented design.
	 * We should revise the Action models.
	 */
	public boolean isVisited() {
		return false;
	}

	/**
	 * Not applicable.
	 * This is inherently bad object oriented design.
	 * We should revise the Action models.
	 */
	public boolean matches(GameAction action) {
		return false;
	}

	/**
	 * Not applicable.
	 * This is inherently bad object oriented design.
	 * We should revise the Action models.
	 */
	public void setVisited(boolean state) { }

	
	public void visit(GameActionVisitor visitor) { 
		visitor.visit(this);
	}
	
	@Override
	public String toString() {
		return "ClientServiceAction pid["+playerId+"]";
	}
}
