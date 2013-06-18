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

import java.lang.reflect.InvocationTargetException;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.command.CommandException;
import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.util.SocketAddress;
import com.cubeia.firebase.server.node.Node;

/**
 * A command handler is an object which knows how to handle commands of
 * a specific type, for a specific node. 
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
@SuppressWarnings({ "rawtypes" })
public interface CommandHandler<T extends Command, N extends Node<?>> extends Initializable<CommandHandlerContext<N>> {

	/**
	 * @param sender Cluster address of the sender, never null
	 * @param com Command to handle, never null
	 * @return An optional return value, may be null
	 * @throws InvocationTargetException 
	 * @throws CommandException
	 */
	public Object handle(SocketAddress sender, T com) throws InvocationTargetException, CommandException;
	
}
