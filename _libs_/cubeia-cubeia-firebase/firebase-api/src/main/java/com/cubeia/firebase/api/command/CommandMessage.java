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
package com.cubeia.firebase.api.command;

import com.cubeia.firebase.api.util.SocketAddress;

/**
 * A very simple command container which combines a command with
 * the originating member address.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
@SuppressWarnings("rawtypes")
public final class CommandMessage {

	/**
	 * The received command, never null
	 */
	public final Command command;
	
	/**
	 * The sending member, never null
	 */
	public final SocketAddress sender;
	
	
	/**
	 * @param command The received command, never null
	 * @param sender The sending member, never null
	 */
	public CommandMessage(Command command, SocketAddress sender) {
		this.command = command;
		this.sender = sender;
	}
}
