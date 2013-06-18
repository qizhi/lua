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
package com.cubeia.firebase.api.plugin.clientlogout;

public enum ClientClosedReason {
	
	/**
	 * The client has requested a logout.
	 * 
	 * This notification will always take place on the local client node.
	 */
	LOGOUT,
	
	/**
	 * A client has disconnected but we are keeping the
	 * session while waiting for a reconnect.
	 * 
	 * This notification will always take place on the local client node.
	 */
	WAIT_RECONNECT,
	
	/**
	 * The client was disconnected and did not reconnect
	 * within the reaper timeout. The session will now be removed from the system
	 * and should the client reconnect it will be treated as a new login. 
	 * 
	 * This notification will always take place on the client coordinator node.
	 */
	DISCONNECT_TIMEOUT,
	
	/**
	 * The client was kicked by an administrator or by server due to illegal
	 * data sent to the client. Check the CLIENTS log for more information.
	 * 
	 * This notification will always take place on the local client node.
	 */
	KICKED,
	
	/**
	 * The client has reconnected to another node and the local session will be closed.
	 * The client is still logged in to the system but not on this server anymore.
	 * 
	 * This notification will always take place on the local client node.
	 */
	MOVED
}
