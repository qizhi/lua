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
package com.cubeia.firebase.server.commands;

public enum Types {

	ACK,
	NAK,
	HANDSHAKE,
	LEAVE,
	CONFIG,
	CONFIG_REQUEST,
	RESUME,
	SHUN,
	HALT,
	PROMOTION,
	PARTITIONING,
	MTT_TABLE_CREATION,
	MTT_TABLE_REMOVAL,
	TABLE_CREATED, 
	TABLE_REMOVED, 
	MTT_CREATED,
	MTT_FINALIZED,
	MTT_REMOVED,
	TABLE_CREATION,
	OBJECT_EMERGED, 
	ACTIVATOR

}
