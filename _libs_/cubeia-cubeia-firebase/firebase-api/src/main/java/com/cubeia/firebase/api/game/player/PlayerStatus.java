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
package com.cubeia.firebase.api.game.player;

/** 
 * <p>The defined statuses are:</br>
 * 
 * <ul>
 * 	<li><b>CONNECTED</b> - A player is connected and all is well.</li>
 * 	<li><b>WAITING_RECONNECT</b> - A player is disconnected but we are waiting for a reconnect.</li>
 * 	<li><b>DISCONNECTED</b> - A player is disconnected and should be removed.</li>
 * 	<li><b>LEAVING</b> - The player has forcibly left.</li>
 *  <li><b>TABLE_LOCAL</b> - This player is localized on the table, e.g. an ai-implementation.</li>
 *  <li><b>RESERVATION</b> - This player has a reservation only.</li>
 * </ul>
 */
public enum PlayerStatus {
	CONNECTED,
	WAITING_REJOIN,
	DISCONNECTED,
	LEAVING,
	TABLE_LOCAL,
	RESERVATION
}
