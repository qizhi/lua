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

/**
 * This interface represents a receiver for cluster commands. You can
 * add and remove listeners for asynchronous fetches.
 * 
 * <p>Channels are used for synchronous communication. The "null" channel
 * is sued for general broadcasts and for asynchronous communication.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
public interface CommandReceiver {

	/*
	 * @param channel Channel id to listen to, null for general broadcast
	 * @param list Listener to add, must not be null
	 */
	public void addCommandListener(String channel, CommandListener list);
	public void addCommandListener(CommandListener list);
	
	/*
	 * @param channel Channel id to listen to, null for general broadcast
	 * @param list Listener to remove, must not be null
	 */
	public void removeCommandListener(String channel, CommandListener list);
	public void removeCommandListener(CommandListener list);
	
}
