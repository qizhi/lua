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
package com.cubeia.firebase.game.table.comm;

import com.cubeia.firebase.api.command.Command;
import com.cubeia.firebase.server.commands.Types;

public class TableCreationRequest extends Command<CreationRequestData> {

	private static final long serialVersionUID = 6540332780009527151L;

	private final int gameId;
	
	private int seq = -1;
	
	public TableCreationRequest(int gameId, CreationRequestData data) {
		super(Types.TABLE_CREATION.ordinal());
		super.setAttachment(data);
		this.gameId = gameId;
	}
	
	public int getGameId() {
		return gameId;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int getSeq() {
		return seq;
	}
	
}