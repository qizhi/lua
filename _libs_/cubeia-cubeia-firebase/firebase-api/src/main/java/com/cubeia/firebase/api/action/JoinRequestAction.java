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

import java.util.Collections;
import java.util.List;

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;
import com.cubeia.firebase.api.common.Attribute;


public class JoinRequestAction extends AbstractPlayerAction {

	private static final long serialVersionUID = 4267305416199490968L;
	
	private int seatId; 
	
	private String nick;
	
	/** @Deprecated */
	private String location;
	
	private List<Attribute> parameters;
	
	/**
	 * @Deprecated Location is not used anymore.
	 * 
	 * @param playerId
	 * @param tableId
	 * @param seatId
	 * @param nick
	 * @param location
	 */
	public JoinRequestAction(int playerId, int tableId, int seatId, String nick, String location) {
		super(playerId, tableId);
		this.seatId = seatId;
		this.nick = nick;
		this.location = location;
	}
	
	public JoinRequestAction(int playerId, int tableId, int seatId, String nick) {
		super(playerId, tableId);
		this.seatId = seatId;
		this.nick = nick;
	}
	
	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}	

	public int getSeatId() {
		return seatId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    public String toString() {
		return "JoinRequest: playerId: " + getPlayerId() + " table: "+getTableId()+" seatId: " + seatId+" nick: "+nick+" Attributes :"+parameters;
	}


	/**
	 * @Deprecated Location is not used anymore.
	 * @return Returns the location.
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @Deprecated Location is not used anymore.
	 * @param location The location to set.
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return Returns the nick.
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * @param nick The nick to set.
	 */
	public void setNick(String nick) {
		this.nick = nick;
	}

	public void setSeatId(int seatId) {
		this.seatId = seatId;
	}

	/**
	 * Returns a list of supplied attribute parameters
	 * to be used with the join request.
	 * 
	 * @return a list of attributes, never null.
	 */
	@SuppressWarnings("unchecked")
	public List<Attribute> getParameters() {
		if (parameters == null) {
			return Collections.EMPTY_LIST;
		} else {
			return parameters;
		}
	}

	public void setParameters(List<Attribute> parameters) {
		this.parameters = parameters;
	}

	
}
