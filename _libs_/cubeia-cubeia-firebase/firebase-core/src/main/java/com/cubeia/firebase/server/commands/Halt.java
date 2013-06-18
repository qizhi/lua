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

/*
 * This command is issued by the server if it
 * needs to stop all cluster communication for some
 * resason.
 */

public class Halt extends Command<HaltMessage> {

	private static final long serialVersionUID = -8091697661383620935L;

	public Halt() {
		super(Types.HALT.ordinal());
	}
	
	public Halt(HaltMessage msg) {
		super(Types.HALT.ordinal());
		setAttachment(msg);
	}
}
