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

import com.cubeia.firebase.api.command.Command;

/**
 * This command is when one or more objects (tables or mtts) emerges 
 * in the system. Currently this command is sent when an object has
 * been added to both messages bus and object cache, but has not yet
 * been added to the system state (lobby).
 * 
 * @author Larsan
 */
public class ObjectEmerged extends Command<int[]> {

	private static final long serialVersionUID = 834673358715037128L;

	public ObjectEmerged(int[] ids) {
		super(Types.OBJECT_EMERGED.ordinal());
		super.setAttachment(ids);
	}
	
	public ObjectEmerged(int id) {
		this(new int[] { id });
	}
}
