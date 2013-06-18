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
import com.cubeia.firebase.server.master.ClusterParticipant;

/*
 * Sent from a node to the entire cluster when it
 * first connects.
 */
public class Handshake extends Command<ClusterParticipant> {

	private static final long serialVersionUID = -7485134896355507306L;

	public Handshake() {
		super(Types.HANDSHAKE.ordinal());
	}
	
	public Handshake(ClusterParticipant data) {
		super(Types.HANDSHAKE.ordinal());
		setAttachment(data);
	}
	
	public String toString() {
		return getAttachment().toString();
	}
}
