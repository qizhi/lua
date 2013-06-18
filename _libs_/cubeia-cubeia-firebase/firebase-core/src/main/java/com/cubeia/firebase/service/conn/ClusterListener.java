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

import com.cubeia.firebase.api.util.SocketAddress;

/**
 * A listener interface for cluster member events. These events
 * only contains members addresses.
 * 
 * @author lars.j.nilsson
 * @date 2007 mar 23
 */
public interface ClusterListener {

	/**
	 * @param member Address of member which just appeared, never null
	 */
	public void memberUp(SocketAddress member);
	
	
	/**
	 * @param member Address of member which just disappeared, never null
	 */
	public void memberDown(SocketAddress member);
	
}
