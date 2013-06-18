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
 * Sent by the master if a node for some reason is
 * not accepted in the cluster. Normally this is sent
 * when the node connects the first time instead of
 * the cluster config.
 */

public class Shun extends Command<String> {

	private static final long serialVersionUID = -6717354309966651870L;

	public Shun() {
		super(Types.SHUN.ordinal());
	}
	
	public Shun(String msg) {
		this();
		setAttachment(msg);
	}
}
