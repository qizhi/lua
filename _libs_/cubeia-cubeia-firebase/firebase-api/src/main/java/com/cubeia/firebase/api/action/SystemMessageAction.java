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

import com.cubeia.firebase.api.action.visitor.GameActionVisitor;

public class SystemMessageAction extends AbstractGameAction {

	private static final long serialVersionUID = 3707176732254048720L;

	private final int type;
	private final int level;
	private final String msg;

	private final int[] playerIds;

	/**
	 * @param type Message type, or -1
	 * @param level Message level, or -1
	 * @param playerIds Specific player ids, or null for all
	 * @param msg Message, or null
	 */
	public SystemMessageAction(int type, int level, int[] playerIds, String msg) {
		super(-1);
		this.type = type;
		this.level = level;
		this.playerIds = playerIds;
		this.msg = msg;
	}
	
	/**
	 * @param type Message type, or -1
	 * @param level Message level, or -1
	 * @param msg Message, or null
	 */
	public SystemMessageAction(int type, int level, String msg) {
		this(type, level, null, msg);
	}
	
	/**
	 * @return The copy of the specific players targeted, or null for all
	 */
	public int[] getPlayerIds() {
		return (playerIds == null ? null : playerIds.clone());
	}
	
	public int getLevel() {
		return level;
	}
	
	/**
	 * @return The system message, may return null
	 */
	public String getMessage() {
		return msg;
	}
	
	public int getType() {
		return type;
	}
	
	public void visit(GameActionVisitor visitor) {
		visitor.visit(this);
	}
}
