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
package com.cubeia.firebase.service.conn;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.api.util.SocketAddress;

/**
 * Interface for an object which knows how to send commands
 * other a communication channel. All "dispatch" methods sends
 * asynchronously and all "send" methods sends synchronously.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
public interface CommandDispatcher {

	/**
	 * @param com Command to send asynchronously, must not be null
	 * @throws ClusterException
	 */
	public void dispatch(Command<?> com) throws ClusterException;
	
	
	/**
	 * @param com Command to send asynchronously, must not be null
	 * @param recipient Single-cast receiver, null for multicast
	 * @throws ClusterException
	 */
	public void dispatch(Command<?> com, SocketAddress recipient) throws ClusterException;
	
	
	/*
	 * @param com Command to send asynchronously, must not be null
	 * @param recipients Single-cast receivers, null for multicast
	 * @throws ClusterException
	 */
	// public void dispatch(Command com, ClusterParticipant[] recipients) throws ClusterException;
	
	
	/**
	 * @param Channel id to send to, null for general broadcast
	 * @param com Command to send synchronously, must not be null
	 * @return An array of responses, never null
	 * @throws ClusterException
	 */
	public CommandResponse[] send(String channel, Command<?> com) throws ClusterException;
	
	
	/**
	 * @param Channel id to send to, null for general broadcast
	 * @param com Command to send synchronously, must not be null
	 * @param timeout Millis to wait for response, or -1 for default
	 * @return An array of responses, never null
	 * @throws ClusterException
	 */
	public CommandResponse[] send(String channel, Command<?> com, long timeout) throws ClusterException;
	
	
	/**
	 * @param Channel id to send to, null for general broadcast
	 * @param com Command to send synchronously, must not be null
	 * @param recipient Single-cast receiver, must not be null
	 * @return A command response, never null
	 * @throws ClusterException
	 */
	public CommandResponse send(String channel, Command<?> com, SocketAddress recipient) throws ClusterException;
	
	
	/**
	 * @param Channel id to send to, null for general broadcast
	 * @param com Command to send synchronously, must not be null
	 * @param recipient Single-cast receiver, must not be null
	 * @param timeout Millis to wait for response, or -1 for default
	 * @return A command response, never null
	 * @throws ClusterException
	 */
	public CommandResponse send(String channel, Command<?> com, SocketAddress recipient, long timeout) throws ClusterException;
	
	/*
	 * @param Channel id to send to, null for general broadcast
	 * @param com Command to send synchronously, must not be null
	 * @param recipients Single-cast receivers, null for multicast
	 * @throws ClusterException
	 */
	// public CommandResponse[] send(String channel, Command com, ClusterParticipant[] recipients) throws ClusterException;
	
}
