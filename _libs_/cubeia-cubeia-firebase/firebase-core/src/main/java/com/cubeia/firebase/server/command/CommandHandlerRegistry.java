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
package com.cubeia.firebase.server.command;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.server.node.Node;

/**
 * A command handler registry tied to a specific node type. A registry
 * keepos command handlers for known commands available for its clients.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
public interface CommandHandlerRegistry<N extends Node<?>> {
	
	/**
	 * @param <T> Command generic, must extend command
	 * @param comm Command to find handler for, must not be null
	 * @return A handler for the command, never null
	 * @throws CommandNotRecognizedException
	 */
	public <T extends Command<?>> CommandHandler<T, N> findHandler(T comm) throws CommandNotRecognizedException;
	
}
