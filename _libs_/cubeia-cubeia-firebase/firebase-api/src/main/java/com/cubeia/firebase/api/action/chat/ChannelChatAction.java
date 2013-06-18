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
package com.cubeia.firebase.api.action.chat;

import com.cubeia.firebase.api.action.AbstractGameAction;
import com.cubeia.firebase.api.action.GameAction;
import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

/**
 * Someone has said something in a chat channel.
 * 
 * @author Fredrik
 */
public class ChannelChatAction extends AbstractGameAction {

	/** Version */
	private static final long serialVersionUID = 1L;
	
	/** Player. Defaults to 0 (public announcment) */
	private int playerid = 0;
	
	/**
	 * I figured that it would be less resource consuming 
	 * (and certainly easier to measure consumption) to
	 * keep the nick of the player in the action rather then
	 * make a lookup or keep nicks cached in systemstate (or something else).
	 * 
	 * However, this will increase internal bandwidth used for chatting 
	 * in chat channels. So... evaulate I guess. If the internal
	 * bandwidth is a bottleneck, then removing this is a potential 
	 * bandwidth optimization.
	 */
	private String nick = "";
	
	private int channelid = -1;
	
	private String message;
	
	/** Recipient. Defaults to 0 (public msg) */
	private int targetid = 0;
	
	/**
	 * Hmm.. GameAction requires a tableid.
	 * However, we are not interested in tableid,
	 * and I will not mix tableids with channelids
	 * so this will most probably be -1 forever
	 * and ever ever...
	 * 
	 * @param tableId
	 */
	public ChannelChatAction(int tableId) {
		super(tableId);
	}
	
	/**
	 * @return the nick
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * @param nick the nick to set
	 */
	public void setNick(String nick) {
		this.nick = nick;
	}

	/**
	 * @return the targetid
	 */
	public int getTargetid() {
		return targetid;
	}

	/**
	 * @param targetid the targetid to set
	 */
	public void setTargetid(int targetid) {
		this.targetid = targetid;
	}

	/**
	 * @return the channelid
	 */
	public int getChannelid() {
		return channelid;
	}
	/**
	 * @param channelid the channelid to set
	 */
	public void setChannelid(int channelid) {
		this.channelid = channelid;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the playerid
	 */
	public int getPlayerid() {
		return playerid;
	}
	/**
	 * @param playerid the playerid to set
	 */
	public void setPlayerid(int playerid) {
		this.playerid = playerid;
	}
	
	/**
	 * TODO: Implement?
	 */
	public boolean matches(GameAction action) {
		return false;
	}
	
	/**
	 * We don't really need to handle chat actions in
	 * a GameActionVisitor. The ChannelChat should never reach
	 * there anyway. But we need this for the translation to packets.
	 * 
	 * TODO: Think about enabling different actions to propagate through
	 * the system.
	 */
	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}
	
	
}
